package com.example.project.database.dataclass

data class DisplayItem(
    val userName: String,
    val productName: String,
    val transactionDate: String,
    val rating: Double? = null,
    val review: String? = null,
    val status: String,
    val itemId: String,
    val transactionId: String,
    val sellerId: Int
)