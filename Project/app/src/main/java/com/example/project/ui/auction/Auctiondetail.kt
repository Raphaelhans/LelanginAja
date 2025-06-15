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
import com.google.firebase.firestore.Query
import com.example.project.ui.chat.ChatActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
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

        produkId = intent.getStringExtra("produk_id") ?: ""
        buyerId = intent.getStringExtra("user_id") ?: ""
        sellerId = intent.getStringExtra("seller_id") ?: ""

        viewModels.currItems.observe(this) { item ->
            item?.let {
                startAuctionCountdown(it.end_date)
                checkAuctionAndSetWinner(it.items_id, it.end_date, it.status)

                Glide.with(this).load(it.image_url).into(binding.detailImage)
                binding.detailName.text = "Nama Produk: ${it.name}"
                binding.detailBid.text = "Rp ${formatterRupiah.format(if (it.end_bid == 0) it.start_bid else it.end_bid)}"

                val dateTime = LocalDateTime.parse(it.end_date, formatter)
                val newFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm")
                binding.auctionDetailTime.text = "${dateTime.format(newFormatter)}"
                binding.auctionDetailLocation.text = "${it.city}"
                binding.descriptionText.text = "${it.description}"
            }
        }

        viewModels.currUser.observe(this) { user ->
            user?.let {
                binding.sellerName.text = "${it.name}"
                Glide.with(this).load(it.profilePicturePath).into(binding.sellerAvatar)
            } ?: run {
                binding.sellerRating.text = "No rating yet"
            }
        }

        binding.bidButton.setOnClickListener {
            val bidText = binding.bidAmountInput.text.toString()
            val bidAmount = bidText.toDoubleOrNull()
            Log.d("BID_DEBUG", "produkId: $produkId, buyerId: $buyerId, sellerId: $sellerId")

            if (bidAmount == null || bidAmount <= 0) {
                Toast.makeText(this, "Masukkan nominal bid yang valid", Toast.LENGTH_SHORT).show()
            } else if (produkId.isNotEmpty() && buyerId.isNotEmpty() && sellerId.isNotEmpty()) {
                placeBid(produkId, buyerId, sellerId, bidAmount)
                binding.bidAmountInput.text.clear()
            } else {
                Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backBtn.setOnClickListener {
            val intent = Intent(this, HomeUser::class.java)
//            intent.putExtra("email", user?.email)
            startActivity(intent)
            finish()
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

    private fun placeBid(
        produkId: String,
        buyerId: String,
        sellerId: String,
        bidAmount: Double
    ) {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection("Products").document(produkId)
        productRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentEndBid = document.getDouble("end_bid") ?: 0.0
                val currentStartBid = document.getDouble("start_bid") ?: 0.0
                val minBid = if (currentEndBid > 0) currentEndBid else currentStartBid.toDouble()
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val formattedDate = dateFormat.format(Date())

                if (bidAmount > minBid) {
                    productRef.update(
                        mapOf(
                            "end_bid" to bidAmount,
                            "buyer_id" to buyerId.toInt()
                        )
                    )

                    val transaksiId = db.collection("Transaksi").document().id
                    val transaksi = hashMapOf(
                        "transaksiId" to transaksiId,
                        "produk_id" to produkId,
                        "buyer_id" to buyerId,
                        "seller_id" to sellerId,
                        "bidAmount" to bidAmount,
                        "time_bid" to formattedDate,
                        "status" to "pending"
                    )

                    db.collection("Transaksi").document(transaksiId).set(transaksi)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Penawaran berhasil: Rp ${formatterRupiah.format(bidAmount)}", Toast.LENGTH_SHORT).show()
                            binding.bidAmountInput.text.clear()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Gagal menyimpan penawaran", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Bid harus lebih tinggi dari penawaran saat ini", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal mengambil data produk", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAuctionAndSetWinner(itemId: String, endDateString: String, status: Int) {
        val endDateTime = LocalDateTime.parse(endDateString, formatter)
        val now = LocalDateTime.now()

        if (now.isAfter(endDateTime) && status == 0) {
            val db = FirebaseFirestore.getInstance()

            db.collection("Transaksi")
                .whereEqualTo("produk_id", itemId)
                .orderBy("bidAmount", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val doc = snapshot.documents[0]
                        val docId = doc.id

                        db.collection("Transaksi").document(docId)
                            .update("status", "menang")
                            .addOnSuccessListener {
                                db.collection("Products").document(itemId)
                                    .update("status", 1)
                            }
                    }
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
