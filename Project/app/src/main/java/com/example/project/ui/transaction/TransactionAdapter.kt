package com.example.project.ui.transaction

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project.database.dataclass.Transactions
import com.example.project.database.dataclass.TransactionwithProduct
import com.example.project.databinding.TranslayoutBinding

class TransactionAdapter(private val transactions: List<TransactionwithProduct>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

        private var onItemClick: ((TransactionwithProduct) -> Unit)? = null

        class TransactionViewHolder(private val binding: TranslayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(transaction: TransactionwithProduct, onClick: ((TransactionwithProduct) -> Unit)?) {
                Glide.with(binding.root.context).load(transaction.produk?.image_url).into(binding.imageView7)
                binding.Statustxt.text = transaction.transaksi.status
                binding.tgltext.text = transaction.transaksi.time_bid
                if (transaction.transaksi.status == "Selesai"){
                    binding.Statustxt.setTextColor(Color.parseColor("#33BA21"))
                }
                binding.Statustxt.text = transaction.transaksi.status
                binding.barangTxt.text = transaction.produk?.name
                binding.lastBidTxt.text = "You Bid: Rp ${transaction.transaksi.bid}"

                if (transaction.transaksi.status != "Completed"){
                    binding.ratelayout.visibility = View.GONE
                }

                binding.root.setOnClickListener { onClick?.invoke(transaction) }
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
            val transaction = transactions[position]
            holder.bind(transaction, onItemClick)
        }

        override fun getItemCount(): Int = transactions.size

        fun setOnItemClickListener(listener: (TransactionwithProduct) -> Unit) {
            onItemClick = listener
        }
    }