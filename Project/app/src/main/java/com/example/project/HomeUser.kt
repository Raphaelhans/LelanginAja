package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.project.databinding.ActivityHomeUserBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeUser : AppCompatActivity() {
    private lateinit var binding: ActivityHomeUserBinding
    val viewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val email = intent.getStringExtra("email")

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

        binding.textView5.text = intent.getStringExtra("name")
        binding.textView9.text = "Rp. "+intent.getStringExtra("balance")


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}