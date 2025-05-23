package com.example.project.ui.auction

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.R
import com.example.project.databinding.ActivityAuctiondetailBinding
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


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
            binding.detailCategory.text = "Category: ${it.category}"

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
                binding.detailCategory.text = "Category: ${it.category}"
                binding.bidButton.setOnClickListener {
                    val bidText = binding.bidAmountInput.text.toString()
                    if (bidText.isNotEmpty() && item != null) {
                        val bidAmount = bidText.toDoubleOrNull()
                        if (bidAmount != null) {
                            placeBid(item, bidAmount)
                        } else {
                            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Please enter a bid amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun placeBid(item: AuctionItem, bidAmount: Double) {
        val db = FirebaseFirestore.getInstance()
        val bidId = db.collection("Bids").document().id
        val email = intent.getStringExtra("email")

        if (email == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val bid = hashMapOf(
            "bidId" to bidId,
            "auctionItemId" to item.id,
            "userEmail" to email,
            "amount" to bidAmount,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("Bids").document(bidId).set(bid)
            .addOnSuccessListener {
                Toast.makeText(this, "Bid placed successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to place bid: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}