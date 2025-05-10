package com.example.project.ui.transaction

import com.example.project.R

object TransData {
    val transactions = listOf(
        TransactionItem(
            id = 1,
            type = "Bidding",
            itemName = "Smartphone",
            date = "8 Maret 2025",
            status = "Berlangsung",
            typeIconResId = R.drawable.auction,
            itemImageResId = R.drawable.guitar
        ),
        TransactionItem(
            id = 2,
            type = "Completed",
            itemName = "Laptop",
            date = "7 Maret 2025",
            status = "Selesai",
            typeIconResId = R.drawable.auction,
            itemImageResId = R.drawable.shoes
        ),
        TransactionItem(
            id = 3,
            type = "Cancelled",
            itemName = "T-Shirt",
            date = "6 Maret 2025",
            status = "Dibatalkan",
            typeIconResId = R.drawable.auction,
            itemImageResId = R.drawable.friedchicken
        )
    )
    fun getAllTransactions(): List<TransactionItem> {
        return transactions
    }


}
