package com.example.project

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.project.database.dataclass.Categories
import com.example.project.ui.auction.Auction
import kotlinx.coroutines.CoroutineScope
import com.example.project.ui.auction.Auction
import com.example.project.ui.auction.AuctionData

class FragmentAdapter(
    activity: AppCompatActivity,
    private val categories: List<Categories>,
    private val email: String
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        val category = categories[position]
        return Auction.newInstance(category.id, email)
    }
}