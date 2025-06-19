package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.database.dataclass.DisplayItem
import java.util.Locale

class RatingAdapter(
    private val transactions: List<DisplayItem>,
    private val onGiveRatingClick: (DisplayItem) -> Unit
) : RecyclerView.Adapter<RatingAdapter.RatingViewHolder>() {

    class RatingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
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
        val item = transactions[position]
        holder.tvUserName.text = "Pembeli: ${item.userName}"
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

            if (item.status == "complete") {
                holder.btnGiveRating.visibility = View.VISIBLE
            } else {
                holder.btnGiveRating.visibility = View.GONE
            }

            holder.btnGiveRating.setOnClickListener {
                onGiveRatingClick(item)
            }
        }
    }

    override fun getItemCount(): Int = transactions.size
}