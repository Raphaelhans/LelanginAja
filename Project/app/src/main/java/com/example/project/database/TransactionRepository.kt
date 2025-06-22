package com.example.project.database

import android.util.Log
import com.example.project.database.dataclass.Products
import com.example.project.database.dataclass.Ratings
import com.example.project.database.dataclass.Transactions
import com.example.project.database.dataclass.Users
import com.example.project.ui.transaction.TransactionItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class TransactionRepository(private val firestore: FirebaseFirestore) {

    suspend fun fetchTransactionItemsByPaymentId(paymentId: String): List<TransactionItem> {
        return try {
            val snapshot = firestore.collection("Payments")
                .whereEqualTo("transaction_id", paymentId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(TransactionItem::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllUsersFirestore(): List<Users> {
        return try {
            firestore.collection("Users").get().await().documents.mapNotNull { document ->
                document.toObject(Users::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllProductsFirestore(): List<Products> {
        return try {
            firestore.collection("Products").get().await().documents.mapNotNull { document ->
                document.toObject(Products::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllTransactionsFirestore(): List<Transactions> {
        return try {
            val snapshot = firestore.collection("Transaksi").get().await()
            snapshot.documents.mapNotNull { document ->
                try {
                    val buyerIdString = document.getString("buyer_id")
                    val sellerIdString = document.getString("seller_id")

                    val buyerIdInt = buyerIdString?.toIntOrNull()
                    val sellerIdInt = sellerIdString?.toIntOrNull()

                    if (buyerIdInt == null) {
                        return@mapNotNull null
                    }
                    if (sellerIdInt == null) {
                        return@mapNotNull null
                    }

                    Transactions(
                        transaksiId = document.getString("transaksiId") ?: "",
                        produk_id = document.getString("produk_id") ?: "",
                        buyer_id = buyerIdInt,
                        seller_id = sellerIdInt,
                        bidAmount = document.getLong("bidAmount")?.toDouble() ?: 0.0,
                        time_bid = document.getString("time_bid") ?: "",
                        status = document.getString("status") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllRatingsFirestore(): List<Ratings> {
        return try {
            firestore.collection("Ratings").get().await().documents.mapNotNull {
                it.toObject(Ratings::class.java)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

}