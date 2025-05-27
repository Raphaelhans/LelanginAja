package com.example.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.database.dataclass.Products
import com.example.project.databinding.ItemProductStaffBinding

class ProductAdapter(
    private val productList: List<Products>,
    private val onSuspendClick: (Products) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: ItemProductStaffBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        with(holder.binding) {
            itemName.text = product.name
            itemEndDate.text = product.end_date
            buttonSuspend.text = if (product.status == 2) "Unsuspend" else "Suspend"
            buttonSuspend.setOnClickListener { onSuspendClick(product) }
        }
    }

    override fun getItemCount(): Int = productList.size
}