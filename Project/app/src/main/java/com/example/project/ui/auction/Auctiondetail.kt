package com.example.project.ui.auction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.project.R
import com.example.project.UserViewModel
import com.example.project.database.dataclass.Products
import com.example.project.databinding.ActivityAuctiondetailBinding
import com.example.project.ui.chat.ChatActivity
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
    val viewModels by viewModels<UserViewModel>()
    private var countdownJob: Job? = null
    private val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm", Locale.getDefault())
    private val formatterRupiah = NumberFormat.getNumberInstance(Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuctiondetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userId = intent.getIntExtra("current_userId", -1)
        val product = intent.getSerializableExtra("product") as? Products

        if (product != null) {
            startAuctionCountdown(product.end_date)

            Glide.with(this).load(product.image_url).into(binding.detailImage)
            binding.detailName.text = product.name
            binding.detailBid.text = if (product.end_bid == 0) {
                "Rp " + formatterRupiah.format(product.start_bid)
            } else {
                "Rp " + formatterRupiah.format(product.end_bid)
            }

            val dateTime = LocalDateTime.parse(product.end_date, formatter)
            val newFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")
            val formattedDate = dateTime.format(newFormatter)
            binding.auctionDetailTime.text = formattedDate

            binding.auctionDetailLocation.text = product.city
            binding.descriptionText.text = product.description

            viewModels.getCategoryById(product.category_id)
            viewModels.currCategories.observe(this) { cate ->
                binding.detailCategory.text = cate?.name ?: "-"
            }

            binding.btnGroup.setOnClickListener {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("auction_item", product.items_id)
                    putExtra("current_userId", userId)
                    putExtra("sellerId", product.seller_id)
                    putExtra("item_name", product.name)
                }
                startActivity(intent)
            }

            if (product.status == 1 && product.buyer_id == userId) {
                binding.btnGiveRating.visibility = View.VISIBLE
            } else {
                binding.btnGiveRating.visibility = View.GONE
            }

            product.seller_id?.let { sellerId ->
                viewModels.getAverageRating(sellerId)
            }

            viewModels.currRating.observe(this) { rating ->
                if (rating != null) {
                    val formattedRating = String.format("%.2f", rating.rating)
                    binding.sellerRating.text = "⭐ $formattedRating"
                } else {
                    binding.sellerRating.text = "No rating yet"
                }
            }

            binding.btnGiveRating.setOnClickListener {
                val ratingBar = RatingBar(this)
                ratingBar.numStars = 5
                ratingBar.stepSize = 1.0f

                AlertDialog.Builder(this)
                    .setTitle("Beri Rating Penjual")
                    .setView(ratingBar)
                    .setPositiveButton("Kirim") { _, _ ->
                        val ratingValue = ratingBar.rating.toInt()
                        viewModels.giveRating(product.seller_id, ratingValue)
                        Toast.makeText(this, "Rating dikirim!", Toast.LENGTH_SHORT).show()
                        binding.btnGiveRating.visibility = View.GONE
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
        }
    }

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

                delay(60_000)
            }
        }
    }
}
