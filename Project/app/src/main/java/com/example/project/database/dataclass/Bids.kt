package com.example.project.database.dataclass

data class Bids(
    val produkId: String = "",
    val buyerId: String,
    val sellerId: String,
    val bidAmount: Double
)
