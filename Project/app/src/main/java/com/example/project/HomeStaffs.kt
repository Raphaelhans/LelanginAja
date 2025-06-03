package com.example.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.database.dataclass.Users
import com.example.project.database.dataclass.Products
import com.example.project.databinding.ActivityHomeStaffsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeStaffs : AppCompatActivity() {
    private lateinit var binding: ActivityHomeStaffsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var userAdapter: UserAdapter
    private lateinit var productAdapter: ProductAdapter
    private val userList = mutableListOf<Users>()
    private val filteredUserList = mutableListOf<Users>()
    private val productList = mutableListOf<Products>()
    private var currentFilter = "All"
    private var currentSearchQuery = ""
    private var currentUser: Staff? = null
    private var isShowingProducts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeStaffsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchFilter()
        setupStatusFilter()
        fetchUserList()
        loadCurrentUser()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    showProfile()
                    true
                }
                R.id.nav_home -> {
                    showHome()
                    true
                }
                R.id.nav_products -> {
                    showProducts()
                    true
                }
                else -> false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            fetchUserList()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(filteredUserList) { user ->
            toggleSuspendStatus(user)
        }
        productAdapter = ProductAdapter(productList) { product ->
            toggleProductSuspendStatus(product)
        }
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsers.adapter = userAdapter
    }

    private fun setupSearchFilter() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString().trim().lowercase()
                applyFilters()
            }
        })
    }

    private fun setupStatusFilter() {
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = parent?.getItemAtPosition(position).toString()
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                currentFilter = "All"
                applyFilters()
            }
        }
    }

    private fun fetchUserList() {
        db.collection("Users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.mapNotNull { it.toObject(Users::class.java) }
                userList.clear()
                userList.addAll(users)
                applyFilters()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@HomeStaffs, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreError", "Fetch user list failed: ${e.message}", e)
            }
    }

    private fun fetchProductList() {
        db.collection("Products")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val products = querySnapshot.documents.mapNotNull { it.toObject(Products::class.java) }
                productList.clear()
                productList.addAll(products)
                productAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@HomeStaffs, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreError", "Fetch product list failed: ${e.message}", e)
            }
    }

    private fun applyFilters() {
        filteredUserList.clear()
        val filtered = userList.filter { user ->
            val matchesEmail = user.email.lowercase().contains(currentSearchQuery)
            val matchesStatus = when (currentFilter) {
                "All" -> true
                "Suspended" -> user.suspended
                "Active" -> !user.suspended
                else -> true
            }
            matchesEmail && matchesStatus
        }
        filteredUserList.addAll(filtered)
        userAdapter.notifyDataSetChanged()
    }

    private fun toggleSuspendStatus(user: Users) {
        val newStatus = !user.suspended
        db.collection("Users")
            .whereEqualTo("user_id", user.user_id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    db.collection("Users").document(document.id)
                        .update("suspended", newStatus)
                        .addOnSuccessListener {
                            fetchUserList()
                            Toast.makeText(this@HomeStaffs, "Status updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@HomeStaffs, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@HomeStaffs, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@HomeStaffs, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleProductSuspendStatus(product: Products) {
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
                            Toast.makeText(this@HomeStaffs, "Product status updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@HomeStaffs, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@HomeStaffs, "Product not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@HomeStaffs, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadCurrentUser() {
        val email = auth.currentUser?.email ?: return
        db.collection("Staffs")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                currentUser = querySnapshot.documents.firstOrNull()?.toObject(Staff::class.java)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to load current staff: ${e.message}", e)
            }
    }

    private fun showProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Staff data not loaded", Toast.LENGTH_SHORT).show()
            return
        }
        isShowingProducts = false
        binding.recyclerViewUsers.visibility = View.GONE
        binding.editTextSearch.visibility = View.GONE
        binding.spinnerFilter.visibility = View.GONE
        binding.textViewTitle.text = "Profile"
        binding.textViewTitle.text = """
            LELANGINAJA
            Name: ${currentUser?.name}
            Phone: ${currentUser?.phone}
            Email: ${currentUser?.email}
        """.trimIndent()
        binding.recyclerViewUsers.adapter = userAdapter
    }

    private fun showHome() {
        isShowingProducts = false
        binding.recyclerViewUsers.visibility = View.VISIBLE
        binding.editTextSearch.visibility = View.VISIBLE
        binding.spinnerFilter.visibility = View.VISIBLE
        binding.textViewTitle.text = "LELANGINAJA"
        binding.recyclerViewUsers.adapter = userAdapter
        fetchUserList()
    }

    private fun showProducts() {
        isShowingProducts = true
        binding.recyclerViewUsers.visibility = View.VISIBLE
        binding.editTextSearch.visibility = View.GONE
        binding.spinnerFilter.visibility = View.GONE
        binding.textViewTitle.text = "Products"
        binding.recyclerViewUsers.adapter = productAdapter
        fetchProductList()
    }

    override fun onResume() {
        super.onResume()
        if (!isShowingProducts) {
            showHome()
        } else {
            showProducts()
        }
    }
}