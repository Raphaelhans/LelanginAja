package com.example.project.database.dataclass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Products(
    val items_id: String = "",
    val category_id: String = "",
    val seller_id: Int = 0,
    val buyer_id: Int = 0,
    val name: String = "",
    val description: String = "",
    val city: String = "",
    val address: String = "",
    val start_date: String = "",
    val end_date: String = "",
    val start_bid: Int = 0,
    val end_bid: Int = 0,
    val image_url: String = "",
    val status: Int = 0,
    val deleted: Boolean = false
) : Parcelable