package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.project.databinding.ActivityHomeUserBinding
import com.example.project.ui.profile.Profile
import com.example.project.ui.transaction.Transaction
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class HomeUser : BaseClass() {
    private lateinit var binding: ActivityHomeUserBinding
    val viewModels by viewModels<UserViewModel>()
    val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
    var balanceDisplay = true
    val cities = arrayOf("Surabaya", "Malang", "Sidoarjo", "Kediri", "Jember")
    lateinit var adapter: FragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val etSearch = binding.etSearch
        val btnSearch = binding.btnSearch

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModels.setSearchBrg(query)
            } else {
                Toast.makeText(this, "Enter Product Name!", Toast.LENGTH_SHORT).show()
            }
        }



        viewModels.currUser.observe(this) { user ->
            if (user != null) {
                lifecycleScope.launch {
                    val categories = viewModels.loadCategories()

                    if (categories.isNotEmpty()) {
                        adapter = FragmentAdapter(this@HomeUser, categories, user.email)
                        viewPager.adapter = adapter

                        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                            tab.text = categories[position].name
                        }.attach()

                        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                    } else {
                        Toast.makeText(this@HomeUser, "No categories found", Toast.LENGTH_SHORT).show()
                    }
                }

                binding.nameUserDis.text = user.name
                binding.saldouserDis.text = "Rp. **********"

                if (user.status == 1){
                    binding.addBid.visibility = View.VISIBLE
                    binding.addBid.setOnClickListener {
                        val intent = Intent(this, SellerAddBarang::class.java)
                        intent.putExtra("email", viewModels.currUser.value?.email)
                        startActivity(intent)
                        finish()
                    }
                }

                binding.profilebtn.setOnClickListener {
                    val intent = Intent(this, Profile::class.java)
                    intent.putExtra("email", viewModels.currUser.value?.email)
                    startActivity(intent)
                    finish()
                }

                binding.showBalance.setOnClickListener {
                    if (balanceDisplay) {
                        binding.saldouserDis.text = "Rp. " + formatter.format(user.balance)
                        binding.showBalance.setImageResource(R.drawable.hide)
                    }
                    else {
                        binding.saldouserDis.text = "Rp. **********"
                        binding.showBalance.setImageResource(R.drawable.visible)
                    }
                    balanceDisplay = !balanceDisplay
                }

                binding.transBtn.setOnClickListener {
                    val intent = Intent(this, Transaction::class.java)
                    intent.putExtra("email", viewModels.currUser.value?.email)
                    startActivity(intent)
                    finish()
                }

                binding.withdrawbtn.setOnClickListener {
                    val intent = Intent(this, Withdraw::class.java)
                    intent.putExtra("email", viewModels.currUser.value?.email)
                    startActivity(intent)
                    finish()
                }

                binding.topupbtn.setOnClickListener {
                    val intent = Intent(this, TopUp::class.java)
                    intent.putExtra("email", viewModels.currUser.value?.email)
                    startActivity(intent)
                    finish()
                }

                if (viewModels.currUser.value?.profilePicturePath != "") {
                    Glide.with(this).load(viewModels.currUser.value?.profilePicturePath).into(binding.userpfpHome)
                } else {
                    binding.userpfpHome.setImageResource(R.drawable.profile)
                }
            } else {
                binding.nameUserDis.text = ""
                binding.saldouserDis.text = ""
            }
        }

        viewModels.resresponse.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        viewModels.getCurrUser(email!!)
    }
}