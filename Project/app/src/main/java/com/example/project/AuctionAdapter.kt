package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AuctionAdapter(private val items: List<AuctionItem>) :
    RecyclerView.Adapter<AuctionAdapter.AuctionViewHolder>() {

        class AuctionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameText: TextView = itemView.findViewById(R.id.itemName)
            val bidText: TextView = itemView.findViewById(R.id.itemBid)
            val imageView: ImageView = itemView.findViewById(R.id.itemImage)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuctionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.itemlayout, parent, false)
            return AuctionViewHolder(view)
        }

        override fun onBindViewHolder(holder: AuctionViewHolder, position: Int) {
            val item = items[position]
            holder.nameText.text = item.name
            holder.bidText.text = "Current Bid: $${item.currentBid}"
            holder.imageView.setImageResource(item.imageResId)
        }

        override fun getItemCount(): Int = items.size
    }