package com.example.project.ui.auction

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.R
import com.example.project.databinding.ActivityAuctiondetailBinding

class Auctiondetail : AppCompatActivity() {
    private lateinit var binding: ActivityAuctiondetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuctiondetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getParcelableExtra<AuctionItem>("auction_item")


        item?.let {
            binding.detailImage.setImageResource(it.imageResId)
            binding.detailName.text = it.name
            binding.detailBid.text = "Current Bid: $${it.currentBid}"
//            binding.detailCategory.text = "Category: ${it.category}"

//            binding.bidButton.setOnClickListener {
//                println("Bidding on ${it.name}")
//            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val item = intent.getParcelableExtra<AuctionItem>("auction_item")

            item?.let {
                binding.detailImage.setImageResource(it.imageResId)
                binding.detailName.text = it.name
                binding.detailBid.text = "Current Bid: $${it.currentBid}"
//                binding.detailCategory.text = "Category: ${it.category}"

//                binding.bidButton.setOnClickListener {
//                    println("Bidding on ${it.name}")
//                }
            }

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

}