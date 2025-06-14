package com.example.project.ui.transaction

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project.database.dataclass.TransactionwithProduct
import com.example.project.databinding.TranslayoutBinding
import com.example.project.ui.auction.TransaksiDetailActivity

class TransactionAdapter(private val transactions: List<TransactionwithProduct>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var onItemClick: ((TransactionwithProduct) -> Unit)? = null

    class TransactionViewHolder(private val binding: TranslayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransactionwithProduct, onClick: ((TransactionwithProduct) -> Unit)?) {
            val context = binding.root.context
            val imageUrl = transaction.produk?.image_url
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(context).load(imageUrl).into(binding.imageView7)
            }
            binding.barangTxt.text = transaction.produk?.name ?: "Produk"
            binding.lastBidTxt.text = "You Bid: Rp ${transaction.transaksi.bidAmount}"

            binding.tgltext.text = transaction.transaksi.time_bid.toString()
            binding.Statustxt.text = transaction.transaksi.status

            if (transaction.transaksi.status.equals("Selesai", ignoreCase = true)) {
                binding.Statustxt.setTextColor(Color.parseColor("#33BA21"))
            }

            if (!transaction.transaksi.status.equals("completed", ignoreCase = true)) {
                binding.ratelayout.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                val intent = Intent(context, TransaksiDetailActivity::class.java).apply {
                    putExtra("transaksi_id", transaction.transaksi.transaksiId)
                    putExtra("produk_id", transaction.transaksi.produk_id)
                    putExtra("itemName", transaction.produk?.name)
                    putExtra("lastBid", transaction.transaksi.bidAmount.toString())
                    putExtra("status", transaction.transaksi.status)
                    putExtra("sellerName", "John Doe")
                    putExtra("sellerAddress", "Jl. Mawar No.10, Surabaya")
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = TranslayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position], onItemClick)
    }

    override fun getItemCount(): Int = transactions.size

    fun setOnItemClickListener(listener: (TransactionwithProduct) -> Unit) {
        onItemClick = listener
    }
}
