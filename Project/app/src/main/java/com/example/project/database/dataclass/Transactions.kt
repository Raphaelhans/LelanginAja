package com.example.project.database.dataclass

data class Transactions(
    val transaksiId:String = "",
    val produk_id:String = "",
    val buyer_id:String = "",
    val seller_id:String = "",
    val bid:Double = 0.0,
    val time_bid:String = "",
    val status:String = ""
)
