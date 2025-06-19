package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project.database.dataclass.Products

class ProductListAdapter(
    private val productList: MutableList<Products>,
    private val onItemClick: (Products) -> Unit
) : RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProduct: ImageView = itemView.findViewById(R.id.imageViewProduct)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewStartDate: TextView = itemView.findViewById(R.id.textViewStartDate)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_list, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        Glide.with(holder.itemView.context)
            .load(product.image_url)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_delete)
            .into(holder.imageViewProduct)
        holder.textViewName.text = product.name
        holder.textViewStartDate.text = product.start_date
        holder.textViewStatus.text = if (product.status == 2) "Suspended" else "Active"
        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateProducts(newList: List<Products>) {
        productList.clear()
        productList.addAll(newList.filter { !it.deleted })
        notifyDataSetChanged()
    }
}