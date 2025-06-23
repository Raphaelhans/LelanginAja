package com.example.project.database.dataclass

data class Ratings(
    val rating_id: String = "",
    val seller_id: Int = 0,
    val buyer_id: Int = 0,
    val item_id: String = "",
    val transaction_id: String = "",
    val rating: Double = 0.0,
    val review: String = "",
    val date: String = ""
)
