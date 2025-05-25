package com.example.project.ui.auction

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.project.R
import com.example.project.UserViewModel
import com.example.project.databinding.ActivityAuctiondetailBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Auctiondetail : AppCompatActivity() {
    private lateinit var binding: ActivityAuctiondetailBinding
    val viewModels by viewModels<UserViewModel>()
    private var countdownJob: Job? = null
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm", Locale.getDefault())
    val formatterRupiah = NumberFormat.getNumberInstance(Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuctiondetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModels.currItems.observe(this) { items ->
            Log.d("Auctiondetail", "onCreate: $items")
            startAuctionCountdown(items?.end_date.toString())
            Glide.with(this).load(items?.image_url).into(binding.detailImage)
            binding.detailName.text = items?.name
            if (items?.end_bid == 0){
                binding.detailBid.text = "Rp " + formatterRupiah.format(items?.start_bid.toString())
            }
            else{
                binding.detailBid.text = "Rp " + formatterRupiah.format(items?.end_bid.toString())
            }
//        val item = intent.getParcelableExtra<AuctionItem>("auction_item")
//
//        item?.let {
//            binding.detailImage.setImageResource(it.imageResId)
//            binding.detailName.text = it.name
//            binding.detailBid.text = "Current Bid: $${it.currentBid}"
//            binding.detailCategory.text = "Category: ${it.category}"
//
////            binding.bidButton.setOnClickListener {
////                println("Bidding on ${it.name}")
////            }
//        }
//
//            viewModels.currCategories.observe(this) { cate ->
//                binding.detailCategory.text = cate?.name
//            }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            val dateTime = LocalDateTime.parse(items?.end_date, formatter)
            val newFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")
            val formatted = dateTime.format(newFormatter)
            binding.auctionDetailTime.text = formatted
            binding.auctionDetailLocation.text = items?.city
            binding.descriptionText.text = items?.description
//            val item = intent.getParcelableExtra<AuctionItem>("auction_item")

        }

        viewModels.currUser.observe(this){ user ->
            binding.sellerName.text = user?.name
            Glide.with(this).load(user?.profilePicturePath).into(binding.sellerAvatar)
            if (viewModels.currRating.value != null){

            }
            else{
                binding.sellerRating.text = "No rating yet"
//            item?.let {
//                binding.detailImage.setImageResource(it.imageResId)
//                binding.detailName.text = it.name
//                binding.detailBid.text = "Current Bid: $${it.currentBid}"
//                binding.detailCategory.text = "Category: ${it.category}"
//                binding.bidButton.setOnClickListener {
//                    val bidText = binding.bidAmountInput.text.toString()
//                    if (bidText.isNotEmpty() && item != null) {
//                        val bidAmount = bidText.toDoubleOrNull()
//                        if (bidAmount != null) {
//                            placeBid(item, bidAmount)
//                        } else {
//                            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        Toast.makeText(this, "Please enter a bid amount", Toast.LENGTH_SHORT).show()
//                    }
//                }
            }
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
        }
    }

    override fun onStart() {
        super.onStart()
        val item = intent.getStringExtra("auction_item")
        val email = intent.getStringExtra("email")
        if (email != null && item != null) {
            viewModels.getCurrUser(email)
            viewModels.getCurrItem(item)
        }
    }

//    private fun placeBid(item: AuctionItem, bidAmount: Double) {
//        val db = FirebaseFirestore.getInstance()
//        val bidId = db.collection("Bids").document().id
//        val email = intent.getStringExtra("email")
//        if (email != null && item != null) {
//            viewModels.getCurrUser(email)
//            viewModels.getCurrItem(item)
//        }
//    }

    fun startAuctionCountdown(endDateString: String) {
        val endDateTime = LocalDateTime.parse(endDateString, formatter)

        countdownJob?.cancel()
        countdownJob = lifecycleScope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                if (now.isAfter(endDateTime)) {
                    binding.timeRemainingText.text = "Auction Ended"
                    break
                }

                val duration = Duration.between(now, endDateTime)
                val days = duration.toDays()
                val hours = duration.toHours() % 24
                val minutes = duration.toMinutes() % 60

                binding.timeRemainingText.text = String.format(
                    "%02dd : %02dh : %02dm", days, hours, minutes
                )

                delay(60_000) // update every 1 minute
            }
        }
    }

//    fun startAuctionCountdown(endDateString: String) {
//        val endDateTime = LocalDateTime.parse(endDateString, formatter)
//
//        countdownJob?.cancel()
//        countdownJob = lifecycleScope.launch {
//            while (isActive) {
//                val now = LocalDateTime.now()
//                if (now.isAfter(endDateTime)) {
//                    binding.timeRemainingText.text = "Auction Ended"
//                    break
//                }
//        if (email == null) {
//            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//                val duration = Duration.between(now, endDateTime)
//                val days = duration.toDays()
//                val hours = duration.toHours() % 24
//                val minutes = duration.toMinutes() % 60
//
//                binding.timeRemainingText.text = String.format(
//                    "%02dd : %02dh : %02dm", days, hours, minutes
//                )
//        val bid = hashMapOf(
//            "bidId" to bidId,
//            "auctionItemId" to item.id,
//            "userEmail" to email,
//            "amount" to bidAmount,
//            "timestamp" to System.currentTimeMillis()
//        )
//
//                delay(60_000) // update every 1 minute
//        db.collection("Bids").document(bidId).set(bid)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Bid placed successfully!", Toast.LENGTH_SHORT).show()
//            }
//        }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Failed to place bid: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
}