package com.example.project.ui.auction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.R
import com.example.project.databinding.ActivityAuctiondetailBinding
import com.example.project.ui.chat.ChatActivity

class Auctiondetail : AppCompatActivity() {
    private lateinit var binding: ActivityAuctiondetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuctiondetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getParcelableExtra<AuctionItem>("auction_item")
        val user = intent.getStringExtra("current_userId")

        item?.let {
            binding.detailImage.setImageResource(it.imageResId)
            binding.detailName.text = it.name
            binding.detailBid.text = "Current Bid: $${it.currentBid}"
            binding.detailCategory.text = "Category: ${it.category}"

            binding.root.findViewById<Button>(R.id.btnGroup).setOnClickListener {
                Log.e("tes", "tes")
            }

            binding.btnGroup.setOnClickListener {
                Log.e("tes", "tes")

                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("auction_item", item.id)
                    putExtra("current_userId", user)
                    putExtra("sellerId", item.sellerId)
                    putExtra("item_name", item.name)
                }
                startActivity(intent)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}