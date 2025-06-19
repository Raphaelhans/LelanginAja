package com.example.project

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.project.database.dataclass.Products
import com.example.project.databinding.ActivityDetailProductStaffBinding

class ActivityDetailProductStaff : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProductStaffBinding
    private val viewModel: StaffViewModel by viewModels()
    private var product: Products? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductStaffBinding.inflate(layoutInflater)
        setContentView(binding.root)

        product = intent.getParcelableExtra<Products>("PRODUCT")
        if (product == null) {
            Toast.makeText(this, "Product data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews(product!!)
        setupButtonListeners()
        setupObservers()
        product?.category_id?.takeIf { it.isNotEmpty() }?.let { viewModel.fetchCategoryName(it) }
    }

    private fun setupViews(product: Products) {
        Glide.with(this)
            .load(product.image_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_delete)
            .into(binding.imageViewProduct)

        binding.textViewName.text = product.name
        binding.textViewCity.text = "City: ${product.city}"
        binding.textViewStartBid.text = "Start Bid: ${product.start_bid}"
        binding.textViewEndBid.text = "End Bid: ${product.end_bid}"
        binding.textViewStartDate.text = "Start Date: ${product.start_date}"
        binding.textViewEndDate.text = "End Date: ${product.end_date}"
        binding.buttonSuspend.text = if (product.status == 2) "Unsuspend" else "Suspend"
        binding.textViewCategory.text = "Category: Loading..."
    }

    private fun setupButtonListeners() {
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonSuspend.setOnClickListener {
            product?.let { viewModel.toggleProductSuspendStatus(it) }
        }

        binding.buttonDelete.setOnClickListener {
            product?.let { viewModel.deleteProduct(it) }
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (message == "Product status updated") {
                    product?.let { product ->
                        val newStatus = if (product.status == 2) 1 else 2
                        this.product = product.copy(status = newStatus)
                        setupViews(this.product!!)
                    }
                } else if (message == "Product deleted") {
                    setResult(RESULT_OK)
                    finish()
                } else if (message.startsWith("Error fetching category")) {
                    binding.textViewCategory.text = "Category: Unknown"
                }
            }
        }

        viewModel.categoryName.observe(this) { categoryName ->
            binding.textViewCategory.text = "Category: ${categoryName ?: "Unknown"}"
        }
    }
}