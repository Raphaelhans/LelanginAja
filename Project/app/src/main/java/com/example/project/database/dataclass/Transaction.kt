package com.example.project.database.dataclass

data class Transaction(
    val bid: Int = 0,
    val buyer_id: String = "",
    val produk_id: String = "",
    val seller_id: String = "",
    val status: String = "",
    val time_bid: Long = 0L,
    val transaksiId: String = ""
)