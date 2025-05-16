package com.example.project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.project.databinding.ActivityHomeUserBinding
import com.example.project.ui.auction.AuctionData
import com.example.project.ui.profile.Profile
import com.example.project.ui.transaction.Transaction
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.ItemDetails
import java.text.NumberFormat
import java.util.Locale

class HomeUser : BaseClass() {
    private lateinit var binding: ActivityHomeUserBinding
    val viewModels by viewModels<UserViewModel>()
    val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
    val cities = arrayOf("Surabaya", "Malang", "Sidoarjo", "Kediri", "Jember")

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

        val locadapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cities)
        locadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.adapter = locadapter
        binding.spinnerLocation.setSelection(0)

        viewModels.currUser.observe(this) { user ->
            if (user != null) {
                binding.nameUserDis.text = user.name
                binding.saldouserDis.text = "Rp. " + formatter.format(user.balance)

                binding.profilebtn.setOnClickListener {
                    val intent = Intent(this, Profile::class.java)
                    intent.putExtra("email", viewModels.currUser.value?.email)
                    startActivity(intent)
                    finish()
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