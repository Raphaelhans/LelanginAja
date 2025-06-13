package com.example.project.ui.auction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.view.View
import android.widget.RatingBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.project.HomeUser
import com.example.project.R
import com.example.project.UserViewModel
import com.example.project.database.dataclass.Products
import com.example.project.databinding.ActivityAuctiondetailBinding
import com.google.firebase.firestore.FirebaseFirestore
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

            viewModels.getCategoryById(items?.category_id.toString())
            viewModels.currCategories.observe(this) { cate ->
                binding.detailCategory.text = cate?.name ?: "-"
            }

            viewModels.currUser.observe(this) { user ->
                if (user?.user_id == items?.seller_id) {
                    binding.bidButton.visibility = View.GONE
                    binding.bidAmountInput.visibility = View.GONE
                }
                else{
                    binding.bidButton.visibility = View.VISIBLE
                    binding.bidAmountInput.visibility = View.VISIBLE
                }

                binding.btnGroup.setOnClickListener {
                    val intent = Intent(this, ChatActivity::class.java).apply {
                        putExtra("auction_item", items?.items_id)
                        putExtra("current_userId", user?.user_id.toString())
                        putExtra("sellerId", items?.seller_id)
                        putExtra("item_name", items?.name)
                    }
                    startActivity(intent)
                }
                binding.bidButton.setOnClickListener {
                    val bidText = binding.bidAmountInput.text.toString()
                    val bidAmount = bidText.toDoubleOrNull()
                    Log.d("BID_DEBUG", "produkId: $produkId, buyerId: $buyerId, sellerId: $sellerId")

                    if (bidAmount == null || bidAmount <= 0) {
                        Toast.makeText(this, "Masukkan nominal bid yang valid", Toast.LENGTH_SHORT)
                            .show()
                    } else if (produkId.isNotEmpty() && buyerId.isNotEmpty() && sellerId.isNotEmpty()) {
                        viewModels.placingBids(produkId, user?.user_id.toString(), sellerId, bidAmount)
                        binding.bidAmountInput.text.clear()
                    } else {
                        Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show()
                    }
                }

                binding.backBtn.setOnClickListener {
                    val intent = Intent(this, HomeUser::class.java)
                    intent.putExtra("email", user?.email)
                    startActivity(intent)
                    finish()
                }
            }


            items?.seller_id?.let { sellerId ->
                viewModels.getAverageRating(sellerId)
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

            }
        }

        viewModels.currRating.observe(this) { rating ->
            if (rating != null) {
                val formattedRating = String.format("%.2f", rating.rating)
                binding.sellerRating.text = "⭐ $formattedRating"
            } else {
                binding.sellerRating.text = "No rating yet"
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val itemId = intent.getStringExtra("items_id")
        val email = intent.getStringExtra("email")
        produkId = intent.getStringExtra("items_id") ?: ""
        buyerId = intent.getStringExtra("user_id") ?: ""
        sellerId = intent.getStringExtra("seller_id") ?: ""
        if (!email.isNullOrEmpty() && !itemId.isNullOrEmpty() && sellerId.isNotEmpty()) {
            viewModels.getCurrUser(email)
            viewModels.getCurrItem(itemId)
            viewModels.getCurrSeller(sellerId)
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