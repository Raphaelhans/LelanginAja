package com.example.project

object AuctionData {
    val categories = listOf("Alat Rumah Tangga", "Mainan & Hobi", "Fashion")

    val items = listOf(
        AuctionItem(1, "Smartphone", "Alat Rumah Tangga", 150.0, android.R.drawable.ic_menu_camera),
        AuctionItem(2, "Laptop", "Alat Rumah Tangga", 500.0, android.R.drawable.ic_menu_gallery),
        AuctionItem(3, "T-Shirt", "Mainan & Hobi", 20.0, android.R.drawable.ic_menu_info_details),
        AuctionItem(4, "Jacket", "Mainan & Hobi", 80.0, android.R.drawable.ic_menu_manage),
        AuctionItem(5, "Table", "Fashion", 120.0, android.R.drawable.ic_menu_share),
        AuctionItem(6, "Chair", "Fashion", 45.0, android.R.drawable.ic_menu_slideshow)
    )

    fun getItemsForCategory(category: String): List<AuctionItem> {
        return items.filter { it.category == category }
    }
}