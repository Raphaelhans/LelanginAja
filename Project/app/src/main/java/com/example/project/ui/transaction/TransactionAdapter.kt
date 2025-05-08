package com.example.project.ui.transaction

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.TranslayoutBinding

class TransactionAdapter(private val transactions: List<TransactionItem>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

        private var onItemClick: ((TransactionItem) -> Unit)? = null

        class TransactionViewHolder(private val binding: TranslayoutBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(transaction: TransactionItem, onClick: ((TransactionItem) -> Unit)?) {
                binding.imageView5.setImageResource(transaction.typeIconResId)
                binding.Statustxt.text = transaction.type
                binding.tgltext.text = transaction.date
                binding.Statustxt.text = transaction.status
                binding.imageView7.setImageResource(transaction.itemImageResId)
                binding.barangTxt.text = transaction.itemName
                if (transaction.type != "Completed"){
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

        fun setOnItemClickListener(listener: (TransactionItem) -> Unit) {
            onItemClick = listener
        }
    }