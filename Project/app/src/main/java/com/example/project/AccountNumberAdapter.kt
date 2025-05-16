package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project.database.dataclass.BankAccount
import com.example.project.databinding.AccountNumberLayoutBinding

class AccountDiffUtil: DiffUtil.ItemCallback<BankAccount>(){
    override fun areItemsTheSame(oldItem: BankAccount, newItem: BankAccount): Boolean {
        return oldItem.bank_id == newItem.bank_id
    }

    override fun areContentsTheSame(oldItem: BankAccount, newItem: BankAccount): Boolean {
        return oldItem == newItem
    }
}

val postDiffUtil = AccountDiffUtil()

class AccountNumberAdapter :
    ListAdapter<BankAccount, AccountNumberAdapter.ViewHolder>(postDiffUtil) {
    class ViewHolder(val binding: AccountNumberLayoutBinding)
        :RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AccountNumberLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = getItem(position)
        holder.binding.bankNameText.text = "Bank: ${account.bankName}"
        holder.binding.accountHolderText.text = "Name: ${account.accountHolder}"
        holder.binding.accountNumberText.text = "Account: ${account.accountNumber}"
    }

}

