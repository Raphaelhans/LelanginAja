package com.example.project.database.dataclass

data class Transactions(
    val transaksiId:String = "",
    val produk_id:String = "",
    val buyer_id:Int = 0,
    val seller_id:Int = 0,
    val bidAmount:Double = 0.0,
    val time_bid:String = "",
    val status:String = ""
)
