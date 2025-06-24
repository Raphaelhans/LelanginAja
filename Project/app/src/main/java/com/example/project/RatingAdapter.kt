package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil // Import DiffUtil
import androidx.recyclerview.widget.ListAdapter // Import ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project.database.dataclass.DisplayItem
import java.util.Locale

class RatingAdapter(
    private val onGiveRatingClick: (DisplayItem) -> Unit
) : ListAdapter<DisplayItem, RatingAdapter.RatingViewHolder>(DIFF_CALLBACK) {

    class RatingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvTransactionDate: TextView = view.findViewById(R.id.tvTransactionDate)
        val ratingContainer: LinearLayout = view.findViewById(R.id.ratingContainer)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvReview: TextView = view.findViewById(R.id.tvReview)
        val btnGiveRating: Button = view.findViewById(R.id.btnGiveRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rating, parent, false)
        return RatingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        val item = getItem(position)
        holder.tvProductName.text = "Produk: ${item.productName}"
        holder.tvTransactionDate.text = "Tanggal: ${item.transactionDate}"

        if (item.rating != null && item.rating > 0.0) {
            holder.ratingContainer.visibility = View.VISIBLE
            holder.tvRating.text = String.format(Locale("id", "ID"), "%.1f/5", item.rating)

            if (!item.review.isNullOrBlank()) {
                holder.tvReview.visibility = View.VISIBLE
                holder.tvReview.text = "Review Anda: \"${item.review}\""
            } else {
                holder.tvReview.visibility = View.GONE
            }
            holder.btnGiveRating.visibility = View.GONE
        } else {
            holder.ratingContainer.visibility = View.GONE
            holder.tvReview.visibility = View.GONE
            holder.btnGiveRating.visibility = View.GONE
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DisplayItem>() {
            override fun areItemsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
                return oldItem.transactionId == newItem.transactionId
            }

            override fun areContentsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}