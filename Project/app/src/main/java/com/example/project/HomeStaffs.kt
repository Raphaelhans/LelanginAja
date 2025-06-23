package com.example.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.database.dataclass.Users
import com.example.project.database.dataclass.Products
import com.example.project.databinding.ActivityHomeStaffsBinding

class HomeStaffs : AppCompatActivity() {
    private lateinit var binding: ActivityHomeStaffsBinding
    private val viewModel: StaffViewModel by viewModels()
    private lateinit var userAdapter: UserAdapter
    private lateinit var productAdapter: ProductAdapter
    private var isShowingProducts = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeStaffsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchFilter()
        setupStatusFilter()
        setupButtonListeners()
        observeViewModel()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            viewModel.fetchUserList()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(mutableListOf())
        productAdapter = ProductAdapter(mutableListOf()) { product ->
            viewModel.toggleProductSuspendStatus(product)
        }
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsers.adapter = userAdapter
    }

    private fun setupSearchFilter() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s.toString())
            }
        })
    }

    private fun setupStatusFilter() {
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setFilter(parent?.getItemAtPosition(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                viewModel.setFilter("All")
            }
        }
    }

    private fun setupButtonListeners() {
        binding.buttonLogout.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.filteredUserList.observe(this) { users ->
            userAdapter.updateUsers(users)
        }
        viewModel.productList.observe(this) { products ->
            productAdapter.updateProducts(products)
        }
        viewModel.errorMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showHome() {
        isShowingProducts = false
        binding.recyclerViewUsers.visibility = View.VISIBLE
        binding.editTextSearch.visibility = View.VISIBLE
        binding.spinnerFilter.visibility = View.VISIBLE
        binding.textViewTitle.text = "LELANGINAJA"
        binding.recyclerViewUsers.adapter = userAdapter
        viewModel.fetchUserList()
    }

    private fun showProducts() {
        isShowingProducts = true
        binding.recyclerViewUsers.visibility = View.VISIBLE
        binding.editTextSearch.visibility = View.GONE
        binding.spinnerFilter.visibility = View.GONE
        binding.textViewTitle.text = "Products"
        binding.recyclerViewUsers.adapter = productAdapter
        viewModel.fetchProductList()
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