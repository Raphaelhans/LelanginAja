package com.example.project

data class AuctionItem(
    val id: Int,
    val name: String,
    val category: String,
    val currentBid: Double,
    val imageResId: Int
)