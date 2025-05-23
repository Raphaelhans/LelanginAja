package com.example.project.ui.auction

data class Bid(
    val bidId: String = "",
    val auctionItemId: Int = 0,
    val userEmail: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
