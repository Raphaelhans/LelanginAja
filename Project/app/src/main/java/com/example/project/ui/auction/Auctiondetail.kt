package com.example.project.ui.auction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.project.R
import com.example.project.UserViewModel
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
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm", Locale.getDefault())
    val formatterRupiah = NumberFormat.getNumberInstance(Locale("in", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuctiondetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val item = intent.getParcelableExtra<AuctionItem>("auction_item")
        val user = intent.getStringExtra("current_userId")

        viewModels.currItems.observe(this) { items ->
            startAuctionCountdown(items?.end_date.toString())
            Glide.with(this).load(items?.image_url).into(binding.detailImage)
            binding.detailName.text = items?.name
            if (items?.end_bid == 0){
                binding.detailBid.text = "Rp " + formatterRupiah.format(items?.start_bid)
            }
            else{
                binding.detailBid.text = "Rp " + formatterRupiah.format(items?.end_bid)
            }

            viewModels.currCategories.observe(this) { cate ->
                binding.detailCategory.text = cate?.name
            }

            val dateTime = LocalDateTime.parse(items?.end_date, formatter)
            val newFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")
            val formatted = dateTime.format(newFormatter)
            binding.auctionDetailTime.text = formatted
            binding.auctionDetailLocation.text = items?.city
            binding.descriptionText.text = items?.description
            binding.btnGroup.setOnClickListener {
                Toast.makeText(baseContext, "Button clicked!", Toast.LENGTH_SHORT).show()

                Log.e("btn", "btn")
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra("auction_item", items?.items_id)
                    putExtra("current_userId", user)
                    putExtra("sellerId", items?.seller_id)
                    putExtra("item_name", items?.name)
                }
                startActivity(intent)
            }

        }

        viewModels.currUser.observe(this){ user ->
            binding.sellerName.text = user?.name
            Glide.with(this).load(user?.profilePicturePath).into(binding.sellerAvatar)
            if (viewModels.currRating.value != null){

            }
            else{
                binding.sellerRating.text = "No rating yet"
            }
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