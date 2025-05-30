package com.example.project.ui.auction

data class Transaksi(
    val transaksiId: String = "",
    val seller_id: String = "",
    val buyer_id: String = "",
    val produk_id: String = "",
    val bidAmount: Double = 0.0,
    val bidTime: Long = System.currentTimeMillis(),
    val status: String = "pending"
)

