package com.example.project.ui.auction

import android.R

object AuctionData {
    val categories = listOf("Alat Rumah Tangga", "Mainan & Hobi", "Fashion")

    val items = listOf(
        AuctionItem("1", "Smartphone", "Alat Rumah Tangga", 150.0, "R.drawable.ic_menu_camera", 1),
        AuctionItem("2", "Laptop", "Alat Rumah Tangga", 500.0, "R.drawable.ic_menu_gallery",1),
//        AuctionItem("2", "Laptop", "Alat Rumah Tangga", 500.0, R.drawable.ic_menu_gallery, 1),
//        AuctionItem("2", "Laptop", "Alat Rumah Tangga", 500.0, R.drawable.ic_menu_gallery, 1),
//        AuctionItem("2", "Laptop", "Alat Rumah Tangga", 500.0, R.drawable.ic_menu_gallery, 1),
//        AuctionItem("3", "T-Shirt", "Mainan & Hobi", 20.0, R.drawable.ic_menu_info_details, 1),
//        AuctionItem("4", "Jacket", "Mainan & Hobi", 80.0, R.drawable.ic_menu_manage, 1),
//        AuctionItem("5", "Table", "Fashion", 120.0, R.drawable.ic_menu_share, 1),
//        AuctionItem("6", "Chair", "Fashion", 45.0, R.drawable.ic_menu_slideshow, 1)
    )

    fun getItemsForCategory(category: String): List<AuctionItem> {
        return items.filter { it.category == category }
    }
}