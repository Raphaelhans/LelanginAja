package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.project.databinding.ActivityHomeUserBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeUser : AppCompatActivity() {
    private lateinit var binding: ActivityHomeUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        val adapter = FragmentAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = AuctionData.categories[position]
        }.attach()

        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE

        binding.profilebtn.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }

        binding.transBtn.setOnClickListener {
            val intent = Intent(this, Transaction::class.java)
            startActivity(intent)
            finish()
        }

        binding.withdrawbtn.setOnClickListener {
            val intent = Intent(this, Withdraw::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}