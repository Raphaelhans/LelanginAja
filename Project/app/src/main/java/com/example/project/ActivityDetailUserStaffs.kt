package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.project.database.dataclass.Users
import com.example.project.databinding.ActivityDetailUserStaffsBinding

class ActivityDetailUserStaffs : AppCompatActivity() {
    private lateinit var binding: ActivityDetailUserStaffsBinding
    private val viewModel: StaffViewModel by viewModels()
    private var user: Users? = null
    private lateinit var productAdapter: ProductListAdapter
    private val REQUEST_CODE_PRODUCT_DETAIL = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailUserStaffsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        user = intent.getParcelableExtra<Users>("USER")
        if (user == null) {
            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupViews(user!!)
        setupButtonListeners()
        setupObservers()
        user?.user_id?.let { viewModel.fetchProductsBySellerId(it.toInt()) }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductListAdapter(mutableListOf()) { product ->
            val intent = Intent(this, ActivityDetailProductStaff::class.java)
            intent.putExtra("PRODUCT", product)
            startActivityForResult(intent, REQUEST_CODE_PRODUCT_DETAIL)
        }
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewProducts.adapter = productAdapter
    }

    private fun setupViews(user: Users) {
        Glide.with(this)
            .load(user.profilePicturePath)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_delete)
            .into(binding.imageViewProfilePicture)

        binding.textViewEmail.text = "Email: ${user.email}"
        binding.textViewLocation.text = "Location: ${user.location}"
        binding.textViewName.text = "Name: ${user.name}"
        binding.textViewPhone.text = "Phone: ${user.phone}"
        binding.textViewStatus.text = "Status: ${user.status}"
        binding.textViewSuspended.text = "Suspended: ${if (user.suspended) "Yes" else "No"}"
        binding.buttonSuspend.text = if (user.suspended) "Unsuspend" else "Suspend"
    }

    private fun setupButtonListeners() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonSuspend.setOnClickListener {
            user?.let { viewModel.toggleSuspendStatus(it) }
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (message == "Status updated") {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }

        viewModel.sellerProducts.observe(this) { products ->
            productAdapter.updateProducts(products ?: emptyList())
            user?.let { user ->
                val updatedUser = viewModel.userList.value?.find { it.user_id == user.user_id }
                updatedUser?.let {
                    this.user = it
                    setupViews(it)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PRODUCT_DETAIL && resultCode == RESULT_OK) {
            user?.user_id?.let { viewModel.fetchProductsBySellerId(it.toInt()) }
        }
    }
}