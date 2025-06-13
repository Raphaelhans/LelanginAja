package com.example.project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.database.dataclass.Users
import com.example.project.database.dataclass.Products
import com.example.project.database.dataclass.Staff
import com.example.project.database.dataclass.Categories
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userList = MutableLiveData<List<Users>>()
    val userList: LiveData<List<Users>> get() = _userList

    private val _filteredUserList = MutableLiveData<List<Users>>()
    val filteredUserList: LiveData<List<Users>> get() = _filteredUserList

    private val _productList = MutableLiveData<List<Products>>()
    val productList: LiveData<List<Products>> get() = _productList

    private val _sellerProducts = MutableLiveData<List<Products>>()
    val sellerProducts: LiveData<List<Products>> get() = _sellerProducts

    private val _currentUser = MutableLiveData<Staff?>()
    val currentUser: LiveData<Staff?> get() = _currentUser

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _categoryName = MutableLiveData<String?>()
    val categoryName: LiveData<String?> get() = _categoryName

    private var currentFilter = "All"
    private var currentSearchQuery = ""

    init {
        fetchUserList()
        loadCurrentUser()
    }

    fun fetchUserList() {
        db.collection("Users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.mapNotNull { it.toObject(Users::class.java) }
                _userList.value = users
                applyFilters()
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error: ${e.message}"
                Log.e("FirestoreError", "Fetch user list failed: ${e.message}", e)
            }
    }

    fun fetchProductList() {
        db.collection("Products")
            .whereEqualTo("deleted", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val products = querySnapshot.documents.mapNotNull { it.toObject(Products::class.java) }
                _productList.value = products
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error: ${e.message}"
                Log.e("FirestoreError", "Fetch product list failed: ${e.message}", e)
            }
    }

    fun fetchProductsBySellerId(sellerId: Int) {
        db.collection("Products")
            .whereEqualTo("seller_id", sellerId)
            .whereEqualTo("deleted", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val products = querySnapshot.documents.mapNotNull { it.toObject(Products::class.java) }
                _sellerProducts.value = products
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error fetching products: ${e.message}"
                Log.e("FirestoreError", "Fetch products by seller failed: ${e.message}", e)
            }
    }

    fun fetchCategoryName(categoryId: String) {
        if (categoryId.isEmpty()) {
            Log.d("StaffViewModel", "Empty category ID, skipping fetch")
            _categoryName.value = null
            return
        }
        Log.d("StaffViewModel", "Fetching category for ID: $categoryId")
        db.collection("Categories")
            .document(categoryId)
            .get()
            .addOnSuccessListener { document ->
                val category = document.toObject(Categories::class.java)
                Log.d("StaffViewModel", "Category fetched: ${category?.name}")
                _categoryName.value = category?.name
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Fetch category failed: ${e.message}", e)
                _errorMessage.value = "Error fetching category: ${e.message}"
                _categoryName.value = null
            }
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query.trim().lowercase()
        applyFilters()
    }

    fun setFilter(filter: String) {
        currentFilter = filter
        applyFilters()
    }

    private fun applyFilters() {
        val users = _userList.value ?: return
        val filtered = users.filter { user ->
            val matchesEmail = user.email.lowercase().contains(currentSearchQuery)
            val matchesStatus = when (currentFilter) {
                "All" -> true
                "Suspended" -> user.suspended
                "Active" -> !user.suspended
                else -> true
            }
            matchesEmail && matchesStatus
        }
        _filteredUserList.value = filtered
    }

    fun toggleSuspendStatus(user: Users) {
        val newStatus = !user.suspended
        val newProductStatus = if (newStatus) 2 else 1


        db.collection("Users")
            .whereEqualTo("user_id", user.user_id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    db.collection("Users").document(document.id)
                        .update("suspended", newStatus)
                        .addOnSuccessListener {

                            db.collection("Products")
                                .whereEqualTo("seller_id", user.user_id.toInt())
                                .whereEqualTo("deleted", false)
                                .get()
                                .addOnSuccessListener { productSnapshot ->
                                    val batch = db.batch()
                                    productSnapshot.documents.forEach { productDoc ->
                                        batch.update(productDoc.reference, "status", newProductStatus)
                                    }
                                    batch.commit()
                                        .addOnSuccessListener {
                                            fetchUserList()
                                            fetchProductList()
                                            fetchProductsBySellerId(user.user_id.toInt())
                                            _errorMessage.value = "Status updated"
                                        }
                                        .addOnFailureListener { e ->
                                            _errorMessage.value = "Error updating products: ${e.message}"
                                            Log.e("FirestoreError", "Batch update failed: ${e.message}", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    _errorMessage.value = "Error fetching products: ${e.message}"
                                    Log.e("FirestoreError", "Fetch products failed: ${e.message}", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = "Error updating user: ${e.message}"
                            Log.e("FirestoreError", "Update user failed: ${e.message}", e)
                        }
                } else {
                    _errorMessage.value = "User not found"
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error: ${e.message}"
                Log.e("FirestoreError", "Fetch user failed: ${e.message}", e)
            }
    }

    fun toggleProductSuspendStatus(product: Products) {
        val newStatus = if (product.status == 2) 1 else 2
        db.collection("Products")
            .whereEqualTo("items_id", product.items_id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    db.collection("Products").document(document.id)
                        .update("status", newStatus)
                        .addOnSuccessListener {
                            fetchProductList()
                            fetchProductsBySellerId(product.seller_id)
                            _errorMessage.value = "Product status updated"
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = "Error: ${e.message}"
                            Log.e("FirestoreError", "Update product status failed: ${e.message}", e)
                        }
                } else {
                    _errorMessage.value = "Product not found"
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error: ${e.message}"
                Log.e("FirestoreError", "Fetch product failed: ${e.message}", e)
            }
    }

    fun deleteProduct(product: Products) {
        db.collection("Products")
            .whereEqualTo("items_id", product.items_id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    db.collection("Products").document(document.id)
                        .update("deleted", true)
                        .addOnSuccessListener {
                            fetchProductList()
                            fetchProductsBySellerId(product.seller_id)
                            _errorMessage.value = "Product deleted"
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = "Error: ${e.message}"
                            Log.e("FirestoreError", "Delete product failed: ${e.message}", e)
                        }
                } else {
                    _errorMessage.value = "Product not found"
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error: ${e.message}"
                Log.e("FirestoreError", "Fetch product failed: ${e.message}", e)
            }
    }

    private fun loadCurrentUser() {
        val email = auth.currentUser?.email ?: return
        db.collection("Staffs")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                _currentUser.value = querySnapshot.documents.firstOrNull()?.toObject(Staff::class.java)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to load current staff: ${e.message}", e)
            }
    }
}