package com.example.project.database.dataclass

data class Ratings(
    val rating_id: String = "",
    val seller_id: String = "",
    val buyer_id: String = "",
    val item_id: String = "",
    val transaction_id: String = "",
    val rating: Int = 0,
    val review: String = "",
    val date: String = ""
)
