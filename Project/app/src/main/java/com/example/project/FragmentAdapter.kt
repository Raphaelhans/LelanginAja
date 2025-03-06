package com.example.project

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
//        return when (position) {
//            0 -> MondayFragment()
//            1 -> TuesdayFragment()
//            2 -> WednesdayFragment()
//            else -> MondayFragment()
//        }
    }
}