package com.example.project

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = AuctionData.categories.size

    override fun createFragment(position: Int): Fragment {
        val category = AuctionData.categories[position]
        return Auction.newInstance(category)
    }
}