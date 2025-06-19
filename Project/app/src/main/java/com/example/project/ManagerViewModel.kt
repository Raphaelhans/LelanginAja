package com.example.project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.database.dataclass.Categories
import com.example.project.database.dataclass.Staff
import com.example.project.database.dataclass.Transaction
import com.example.project.database.dataclass.Products
import com.example.project.database.dataclass.Users
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.concurrent.Executors

class ManagerViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val executor = Executors.newSingleThreadExecutor()

    private val _successfulAuctions = MutableLiveData<String>()
    val successfulAuctions: LiveData<String> get() = _successfulAuctions

    private val _topBuyers = MutableLiveData<String>()
    val topBuyers: LiveData<String> get() = _topBuyers

    private val _topSellers = MutableLiveData<String>()
    val topSellers: LiveData<String> get() = _topSellers

    private val _popularCategories = MutableLiveData<String>()
    val popularCategories: LiveData<String> get() = _popularCategories

    private val _topCities = MutableLiveData<String>()
    val topCities: LiveData<String> get() = _topCities

    private val _staffList = MutableLiveData<List<Staff>>()
    val staffList: LiveData<List<Staff>> get() = _staffList

    private val _staffDetail = MutableLiveData<Staff?>()
    val staffDetail: LiveData<Staff?> get() = _staffDetail

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _addStaffResult = MutableLiveData<Result<Unit>>()
    val addStaffResult: LiveData<Result<Unit>> get() = _addStaffResult

    fun fetchReportData() {
        executor.execute {
            db.collection("Transactions")
                .get()
                .addOnSuccessListener { transactionSnapshot ->
                    val transactions = transactionSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)?.copy(transaksiId = doc.id)
                    }

                    db.collection("Products")
                        .get()
                        .addOnSuccessListener { productSnapshot ->
                            val products = productSnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Products::class.java)?.copy(items_id = doc.id)
                            }

                            db.collection("Users")
                                .get()
                                .addOnSuccessListener { userSnapshot ->
                                    val users = userSnapshot.documents.mapNotNull { doc ->
                                        doc.toObject(Users::class.java)
                                    }

                                    db.collection("Categories")
                                        .get()
                                        .addOnSuccessListener { categorySnapshot ->
                                            val categories = categorySnapshot.documents.mapNotNull { doc ->
                                                doc.toObject(Categories::class.java)?.copy(id = doc.id)
                                            }
                                            computeAndDisplayReport(transactions, products, users, categories)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore Error", "Error fetching categories: ${e.message}", e)
                                            _errorMessage.postValue("Error: ${e.message}")
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore Error", "Error fetching users: ${e.message}", e)
                                    _errorMessage.postValue("Error: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore Error", "Error fetching products: ${e.message}", e)
                            _errorMessage.postValue("Error: ${e.message}")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore Error", "Error fetching transactions: ${e.message}", e)
                    _errorMessage.postValue("Error: ${e.message}")
                }
        }
    }

    private fun computeAndDisplayReport(
        transactions: List<Transaction>,
        products: List<Products>,
        users: List<Users>,
        categories: List<Categories>
    ) {
        val successfulAuctions = transactions.count { it.status == "completed" }
        _successfulAuctions.postValue(successfulAuctions.toString())

        val buyerCounts = transactions.groupBy { it.buyer_id }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val topBuyersText = if (buyerCounts.isNotEmpty()) {
            buyerCounts.joinToString("\n") { "Buyer ID: ${it.first} (${it.second} bids)" }
        } else {
            "No buyers found"
        }
        _topBuyers.postValue(topBuyersText)

        val sellerCounts = products.groupBy { it.seller_id }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val topSellersText = if (sellerCounts.isNotEmpty()) {
            sellerCounts.joinToString("\n") { (sellerId, count) ->
                val email = users.find { it.user_id == sellerId }?.email ?: "Unknown ($sellerId)"
                "$email ($count listings)"
            }
        } else {
            "No sellers found"
        }
        _topSellers.postValue(topSellersText)

        val categoryCounts = products.groupBy { it.category_id }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val popularCategoriesText = if (categoryCounts.isNotEmpty()) {
            categoryCounts.joinToString("\n") { (categoryId, count) ->
                val categoryName = categories.find { it.id == categoryId }?.name ?: "Unknown ($categoryId)"
                "$categoryName ($count listings)"
            }
        } else {
            "No categories found"
        }
        _popularCategories.postValue(popularCategoriesText)

        val cityCounts = products.groupBy { it.city }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val topCitiesText = if (cityCounts.isNotEmpty()) {
            cityCounts.joinToString("\n") { "${it.first} (${it.second} auctions)" }
        } else {
            "No cities found"
        }
        _topCities.postValue(topCitiesText)
    }

    fun fetchStaffList() {
        db.collection("Staffs")
            .whereEqualTo("status", false)
            .whereEqualTo("deleted", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val staff = querySnapshot.documents.mapNotNull { it.toObject(Staff::class.java) }
                _staffList.postValue(staff)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Fetch staff list failed: ${e.message}", e)
                _errorMessage.postValue("Error: ${e.message}")
            }
    }

    fun fetchStaffById(idStaff: Int) {
        db.collection("Staffs")
            .whereEqualTo("id_staff", idStaff)
            .whereEqualTo("deleted", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val staff = querySnapshot.documents.firstOrNull()?.toObject(Staff::class.java)
                _staffDetail.postValue(staff)
                if (staff == null) {
                    _errorMessage.postValue("Staff not found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Fetch staff failed: ${e.message}", e)
                _errorMessage.postValue("Error: ${e.message}")
            }
    }

    fun toggleSuspendStatus(staff: Staff) {
        val newStatus = !staff.suspended
        db.collection("Staffs")
            .whereEqualTo("id_staff", staff.id_staff)
            .whereEqualTo("deleted", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    db.collection("Staffs").document(document.id)
                        .update("suspended", newStatus)
                        .addOnSuccessListener {
                            fetchStaffById(staff.id_staff)
                            _errorMessage.postValue("Status updated")
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.postValue("Error: ${e.message}")
                        }
                } else {
                    _errorMessage.postValue("Staff not found")
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.postValue("Error: ${e.message}")
            }
    }

    fun deleteStaff(staff: Staff) {
        db.collection("Staffs")
            .whereEqualTo("id_staff", staff.id_staff)
            .whereEqualTo("deleted", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    db.collection("Staffs").document(document.id)
                        .update("deleted", true)
                        .addOnSuccessListener {
                            _staffDetail.postValue(null)
                            _errorMessage.postValue("Staff deleted")
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.postValue("Error: ${e.message}")
                        }
                } else {
                    _errorMessage.postValue("Staff not found")
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.postValue("Error: ${e.message}")
            }
    }

    fun addStaff(email: String, name: String, password: String, phone: String) {
        db.collection("Staffs")
            .whereEqualTo("email", email)
            .whereEqualTo("deleted", false)
            .get()
            .addOnSuccessListener { emailQuery ->
                if (!emailQuery.isEmpty) {
                    _addStaffResult.postValue(Result.failure(Exception("Email already taken")))
                    return@addOnSuccessListener
                }

                db.collection("Staffs")
                    .orderBy("id_staff", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { highestIdQuery ->
                        val highestId = if (highestIdQuery.isEmpty) {
                            1
                        } else {
                            highestIdQuery.documents.first().toObject(Staff::class.java)?.id_staff?.plus(1) ?: 1
                        }

                        val staff = Staff(
                            id_staff = highestId,
                            name = name,
                            phone = phone,
                            email = email,
                            password = password,
                            status = false,
                            suspended = false,
                            deleted = false
                        )

                        db.collection("Staffs").document(highestId.toString()).set(staff)
                            .addOnSuccessListener {
                                _addStaffResult.postValue(Result.success(Unit))
                            }
                            .addOnFailureListener { e ->
                                _addStaffResult.postValue(Result.failure(e))
                            }
                    }
                    .addOnFailureListener { e ->
                        _addStaffResult.postValue(Result.failure(e))
                    }
            }
            .addOnFailureListener { e ->
                _addStaffResult.postValue(Result.failure(e))
            }
    }
}