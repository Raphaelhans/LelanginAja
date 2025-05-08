package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.project.databinding.ActivityHomeUserBinding
import com.example.project.ui.auction.AuctionData
import com.example.project.ui.transaction.Transaction
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HomeUser : AppCompatActivity() {
    private lateinit var binding: ActivityHomeUserBinding
    val viewModel by viewModels<UserViewModel>()

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

        viewModel.currUser.observe(this) { user ->
            if (user != null) {
                binding.nameUserDis.text = user.name
                binding.saldouserDis.text = user.balance.toString()

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
                    intent.putExtra("email", viewModel.currUser.value?.email)
                    startActivity(intent)
                    finish()
                }
            } else {
                binding.nameUserDis.text = ""
                binding.saldouserDis.text = ""
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        viewModel.getCurrUser(email!!)
    }
}