package com.example.project.ui.transaction

import com.example.project.R

object TransData {
    val transactions = listOf(
        TransactionItem(
            id = "1",
            type = "Bidding",
            itemName = "Smartphone",
            itemId = "2",
            date = "8 Maret 2025",
            status = "Berlangsung",
            info = "Penawaran tertinggi Anda", // Added info
            amount = 1500000, // Added amount, assuming it's the lastBid for bidding
            typeIconResId = R.drawable.auction,
            itemImageResId = R.drawable.guitar, // Assuming guitar is a placeholder image
            lastBid = 1500000
        ),
        TransactionItem(
            id = "2",
            type = "Completed",
            itemName = "Laptop",
            itemId = "3",
            date = "7 Maret 2025",
            status = "Selesai",
            info = "Terjual",
            amount = 2500000,
            typeIconResId = R.drawable.auction,
            itemImageResId = R.drawable.shoes, // Assuming shoes is a placeholder image
            lastBid = 2500000
        ),
        TransactionItem(
            id = "3",
            type = "Cancelled",
            itemName = "T-Shirt",
            itemId = "8",
            date = "6 Maret 2025",
            status = "Dibatalkan",
            info = "Penawaran dibatalkan", // Added info
            amount = 0, // Added amount, set to 0 for cancelled
            typeIconResId = R.drawable.auction,
            itemImageResId = R.drawable.friedchicken, // Assuming friedchicken is a placeholder image
            lastBid = 1700000
        )
    )
    fun getAllTransactions(): List<TransactionItem> {
        return transactions
    }
}