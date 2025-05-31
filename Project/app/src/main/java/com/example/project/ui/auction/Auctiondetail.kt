package com.example.project.ui.auction

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class Auctiondetail : AppCompatActivity() {

    private lateinit var binding: ActivityAuctiondetailBinding
    private val viewModels by viewModels<UserViewModel>()
    private var countdownJob: Job? = null
    private val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm", Locale.getDefault())
    private val formatterRupiah = NumberFormat.getNumberInstance(Locale("in", "ID"))

    private var produkId: String = ""
    private var buyerId: String = ""
    private var sellerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuctiondetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModels.currItems.observe(this) { items ->
            startAuctionCountdown(items?.end_date.toString())
            Glide.with(this).load(items?.image_url).into(binding.detailImage)
            binding.detailName.text = items?.name
            if (items?.end_bid == 0) {
                binding.detailBid.text = "Rp " + formatterRupiah.format(items?.start_bid)
            } else {
                binding.detailBid.text = "Rp " + formatterRupiah.format(items?.end_bid)
            }

            viewModels.currItems.observe(this) { item ->
                Log.d("Auctiondetail", "onCreate: $item")
                item?.let {
                    startAuctionCountdown(it.end_date)

                    Glide.with(this).load(it.image_url).into(binding.detailImage)
                    binding.detailName.text = it.name
                    binding.detailBid.text = "Rp " + formatterRupiah.format(
                        if (it.end_bid == 0) it.start_bid else it.end_bid
                    )

                    val dateTime = LocalDateTime.parse(it.end_date, formatter)
                    val newFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")
                    binding.auctionDetailTime.text = dateTime.format(newFormatter)
                    binding.auctionDetailLocation.text = it.city
                    binding.descriptionText.text = it.description
                }
            }

            viewModels.currSeller.observe(this) { user ->
                user?.let {
                    binding.sellerName.text = it.name

                    if (user.profilePicturePath.isNotEmpty()) {
                        Glide.with(this).load(user.profilePicturePath).into(binding.sellerAvatar)
                    } else {
                        binding.sellerAvatar.setImageResource(R.drawable.profile)
                    }

                } ?: run {
                    binding.sellerRating.text = "No rating yet"
                }
            }

            binding.bidButton.setOnClickListener {
                val bidText = binding.bidAmountInput.text.toString()
                val bidAmount = bidText.toDoubleOrNull()
                Log.d("BID_DEBUG", "produkId: $produkId, buyerId: $buyerId, sellerId: $sellerId")

                if (bidAmount == null || bidAmount <= 0) {
                    Toast.makeText(this, "Masukkan nominal bid yang valid", Toast.LENGTH_SHORT)
                        .show()
                } else if (produkId.isNotEmpty() && buyerId.isNotEmpty() && sellerId.isNotEmpty()) {
                    viewModels.placingBids(produkId, buyerId, sellerId, bidAmount)
                    binding.bidAmountInput.text.clear()
                } else {
                    Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModels.resresponse.observe(this){ response ->
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val itemId = intent.getStringExtra("auction_item")
        val email = intent.getStringExtra("email")
        produkId = intent.getStringExtra("produk_id") ?: ""
        buyerId = intent.getStringExtra("user_id") ?: ""
        sellerId = intent.getStringExtra("seller_id") ?: ""
        if (!email.isNullOrEmpty() && !itemId.isNullOrEmpty() && sellerId.isNotEmpty()) {
            viewModels.getCurrUser(email)
            viewModels.getCurrItem(itemId)
            viewModels.getCurrSeller(sellerId)
        }
    }

    private fun startAuctionCountdown(endDateString: String) {
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

                binding.timeRemainingText.text =
                    String.format("%02dd : %02dh : %02dm", days, hours, minutes)

                delay(60_000)
            }
        }
    }
}
