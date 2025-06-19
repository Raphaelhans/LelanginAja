package com.example.project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.database.dataclass.Payment
import com.example.project.database.dataclass.Products
import com.example.project.database.dataclass.Users
import com.example.project.database.dataclass.Withdraws
import com.example.project.ui.transaction.TransactionItem
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class HistoryViewModel: ViewModel() {
    private val db = Firebase.firestore
    private val client = OkHttpClient()

    private val _currUser = MutableLiveData<Users?>()
    val currUser: LiveData<Users?> get() = _currUser

    private val _transactions = MutableLiveData<List<TransactionItem>>()
    val transactions: LiveData<List<TransactionItem>> = _transactions

    private val _currItems = MutableLiveData<Products?>()
    val currItems: LiveData<Products?> = _currItems

    private val _historyList = MutableLiveData<List<TransactionItem>>()
    val historyList: LiveData<List<TransactionItem>> get() = _historyList

    private val _resresponse = MutableLiveData<String>()
    val resresponse: LiveData<String> get() = _resresponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getCurrUserHistory(userEmail: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val payments = fetchPaymentsByEmail(userEmail)
                val withdraws = fetchWithdrawsByUser(userEmail)
                val bids = fetchBidsByUser(userEmail)
                val topups = fetchTopupsByUser(userEmail)

                val paymentItems = payments.map { payment ->
                    val relatedTransactionItems = fetchTransactionItemsByPaymentId(payment.transaction_id ?: "")
                    val produkIds = relatedTransactionItems.map { it.itemId }.joinToString(", ")
                    val itemNames = relatedTransactionItems.map { it.itemName }.joinToString(", ")

                    TransactionItem(
                        id = payment.transaction_id ?: "",
                        type = "Payment",
                        itemName = if (itemNames.isNotEmpty()) itemNames else "Payment Transaction",
                        itemId = if (produkIds.isNotEmpty()) produkIds else "",
                        date = payment.date ?: "",
                        status = payment.transaction_status ?: "Tidak diketahui",
                        info = "${payment.payment_type ?: "-"} • ${payment.transaction_status ?: "Tidak diketahui"}",
                        amount = payment.amount ?: 0,
                        itemImageResId = com.example.project.R.drawable.card,
                        typeIconResId = R.drawable.leftnew,
                        lastBid = 0,
                    )
                }

                val withdrawItems = withdraws.map {
                    TransactionItem(
                        id = it.id ?: "",
                        type = "Withdraw",
                        itemName = "Withdraw to ${it.bank ?: "-"}",
                        itemId = "",
                        date = it.date ?: "",
                        status = "",
                        info = "${it.bank ?: "-"} • Success",
                        amount = it.amountWd,
                        itemImageResId = com.example.project.R.drawable.transaction,
                        typeIconResId = R.drawable.leftnew,
                        lastBid = it.amountWd,
                    )
                }

                val bidItems = bids.map { bidDoc ->
                    val bidAmount = bidDoc.getLong("bidAmount")?.toInt() ?: 0
                    val bidTimeAwal = bidDoc.get("bidTime")
                    val bidTimeMillis: Long = when (bidTimeAwal) {
                        is Number -> bidTimeAwal.toLong()
                        is String -> bidTimeAwal.toLongOrNull() ?: System.currentTimeMillis()
                        else -> System.currentTimeMillis()
                    }

                    val productId = bidDoc.getString("produk_id") ?: ""
                    val status = bidDoc.getString("status") ?: "Pending"
                    val transaksiId = bidDoc.getString("transaksiId") ?: ""

                    val bidDate = convertTimestampToFormattedDate(bidTimeMillis)
                    val productName = getProductNameById(productId)

                    TransactionItem(
                        id = transaksiId,
                        type = "Bid",
                        itemName = productName,
                        itemId = productId,
                        date = bidDate,
                        status = status,
                        info = "Bid • $status",
                        amount = bidAmount,
                        itemImageResId = com.example.project.R.drawable.auction,
                        typeIconResId = com.example.project.R.drawable.auction,
                        lastBid = bidAmount
                    )
                }

                val topupItems = topups.map { topup ->
                    TransactionItem(
                        id = topup.transaction_id,
                        type = "Topup",
                        itemName = "Top Up Saldo",
                        itemId = "",
                        date = topup.date ?: "",
                        status = topup.transaction_status ?: "Success",
                        info = "${topup.payment_type ?: "-"} • ${topup.transaction_status ?: "Success"}",
                        amount = topup.amount ?: 0,
                        itemImageResId = R.drawable.add,
                        typeIconResId = R.drawable.add,
                        lastBid = 0
                    )
                }

                val combinedHistory = (paymentItems + withdrawItems + bidItems + topupItems)
                    .sortedByDescending { parseDate(it.date) }

                _historyList.postValue(combinedHistory)

            } catch (e: Exception) {
                Log.e("UserViewModel", "Error fetching history: ${e.message}", e)
                _historyList.postValue(emptyList())
                _resresponse.postValue("Gagal memuat riwayat: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun convertTimestampToFormattedDate(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        return localDateTime.format(formatter)
    }

    suspend fun fetchTransactionItemsByPaymentId(paymentId: String): List<TransactionItem> {
        return try {
            val snapshot = db.collection("Payments")
                .whereEqualTo("transaction_id", paymentId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(TransactionItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error fetching transaction items: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchPaymentsByEmail(email: String): List<Payment> {
        return try {
            val snapshot = Firebase.firestore
                .collection("payments")
                .whereEqualTo("emailUser", email)
                .get()
                .await()
            Log.e("payments", snapshot.toString())

            return snapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(transaction_id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error fetching payments: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchWithdrawsByUser(email: String): List<Withdraws> {
        return try {
            val userSnapshot = db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .await()
            Log.e("user-wd", userSnapshot.toString())

            val userId = userSnapshot.documents.firstOrNull()?.getString("user_id") ?: return emptyList()

            val withdrawSnapshot = db.collection("Withdraws")
                .whereEqualTo("user_id", userId)
                .get()
                .await()
            Log.e("Withdraw", withdrawSnapshot.toString())

            withdrawSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Withdraws::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error fetching withdraws: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchBidsByUser(email: String): List<com.google.firebase.firestore.DocumentSnapshot> {
        return try {
            val userSnapshot = db.collection("Users")
                .whereEqualTo("email", email)
                .get()
                .await()

            val userId = userSnapshot.documents.firstOrNull()?.get("user_id")?.toString() ?: return emptyList()

            val bidSnapshot = db.collection("Transaksi")
                .whereEqualTo("buyer_id", userId)
                .get()
                .await()

            bidSnapshot.documents
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error fetching bids: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun fetchTopupsByUser(email: String): List<Payment> {
        return try {
            val snapshot = db.collection("payments")
                .whereEqualTo("emailUser", email)
                .whereEqualTo("name", "Top Up Balance")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Payment::class.java)?.copy(transaction_id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error fetching topups: ${e.message}", e)
            emptyList()
        }
    }

    fun getProductIdsFromPayment(paymentId: String, transactionItems: List<TransactionItem>): List<String> {
        return transactionItems
            .filter { it.id == paymentId || it.itemId == paymentId }
            .map { it.itemId }
            .filter { it.isNotEmpty() }
    }

    suspend fun getProductNameById(productId: String): String {
        return try {
            if (productId.isEmpty()) return "Produk tidak diketahui"

            val productDoc = db.collection("Products")
                .document(productId)
                .get()
                .await()

            val product = productDoc.toObject(Products::class.java)
            product?.name ?: "Produk tidak ditemukan"
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error fetching product name: ${e.message}", e)
            "Error memuat produk"
        }
    }

    private fun parseDate(dateString: String): Date {
        return try {
            val isoFormatter = DateTimeFormatter.ISO_DATE_TIME
            val localDateTime = LocalDateTime.parse(dateString, isoFormatter)
            Date.from(localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant())
        } catch (e: Exception) {
            try {
                val indonesianFormat = SimpleDateFormat("dd MMMM, HH:mm", Locale("id", "ID"))
                indonesianFormat.parse(dateString) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }
    }
}

