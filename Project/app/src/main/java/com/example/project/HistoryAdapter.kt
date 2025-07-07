package com.example.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ItemHistoryBinding
import com.example.project.ui.transaction.TransactionItem
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (TransactionItem) -> Unit = {}
) : ListAdapter<TransactionItem, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

    class HistoryViewHolder(
        private val binding: ItemHistoryBinding,
        private val onItemClick: (TransactionItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionItem) {
            binding.itemHistory.text = when {
                item.itemName.isNotEmpty() -> item.itemName
                item.type.contains("topup", ignoreCase = true) -> "Top Up Saldo"
                item.type.contains("withdraw", ignoreCase = true) -> "Withdraw"
                item.type.contains("payment", ignoreCase = true) -> "Pembayaran"
                else -> "Transaksi"
            }

            val outputFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm", Locale("id", "ID"))
            binding.dateHistory.text = try {
                LocalDateTime.parse(item.date, DateTimeFormatter.ISO_DATE_TIME).format(outputFormat)
            } catch (e: Exception) {
                try {
                    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
                    sdf.format(sdf.parse(item.date) ?: Date())
                } catch (e2: Exception) {
                    item.date
                }
            }

            val displayAmount = if (item.lastBid > 0) item.lastBid else item.lastBid
            binding.priceHistory.text = "Rp ${NumberFormat.getNumberInstance(Locale("in", "ID")).format(displayAmount)}"

            setTransactionTypeUI(item)

            binding.root.setOnClickListener {
                onItemClick(item)
            }

            if (item.itemImageResId != 0) {
                binding.imgItemHistory.setImageResource(item.itemImageResId)
            }
        }

        private fun setTransactionTypeUI(item: TransactionItem) {
            val context = binding.root.context

            when (item.type.lowercase()) {
                "topup", "top up", "top-up" -> {
                    binding.imgItemHistory.setImageResource(R.drawable.add)
                    binding.priceHistory.setTextColor(
                        ContextCompat.getColor(context, android.R.color.holo_green_dark)
                    )
                }
                "payment", "pembayaran", "bayar" -> {
                    binding.imgItemHistory.setImageResource(R.drawable.card)
                    binding.priceHistory.setTextColor(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark)
                    )
                }
                "withdraw", "penarikan" -> {
                    binding.imgItemHistory.setImageResource(R.drawable.transaction)
                    binding.priceHistory.setTextColor(
                        ContextCompat.getColor(context, android.R.color.holo_orange_dark)
                    )
                }
                "bid", "penawaran" -> {
                    binding.imgItemHistory.setImageResource(R.drawable.auction)
                    binding.priceHistory.setTextColor(
                        ContextCompat.getColor(context, android.R.color.holo_blue_dark)
                    )
                }
                else -> {
                    binding.imgItemHistory.setImageResource(R.drawable.ic_history)
                    binding.priceHistory.setTextColor(
                        ContextCompat.getColor(context, android.R.color.black)
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionItem>() {
            override fun areItemsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TransactionItem, newItem: TransactionItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}