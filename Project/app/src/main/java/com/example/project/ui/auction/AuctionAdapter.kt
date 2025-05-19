package com.example.project.ui.auction

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project.AccountNumberAdapter
import com.example.project.R
import com.example.project.database.dataclass.BankAccount
import com.example.project.database.dataclass.Products
import com.example.project.databinding.AccountNumberLayoutBinding
import com.example.project.databinding.ItemlayoutBinding

class AccountDiffUtil: DiffUtil.ItemCallback<Products>(){
    override fun areItemsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem.items_id == newItem.items_id
    }

    override fun areContentsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem == newItem
    }
}

val postDiffUtil = AccountDiffUtil()

class AuctionAdapter(
    var onItemClickListener: ((Products) -> Unit)? = null
) : ListAdapter<Products, AuctionAdapter.ViewHolder>(postDiffUtil) {
    class ViewHolder(val binding: ItemlayoutBinding)
        :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemlayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.itemName.text = getItem(position).name
        holder.binding.itemBid.text = "Highest Bid: Rp ${getItem(position).end_bid}"
        Glide.with(holder.itemView.context).load(getItem(position).image_url).into(holder.binding.itemImage)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(getItem(position))
        }
    }
}