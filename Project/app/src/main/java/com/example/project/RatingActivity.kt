package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ActivityRatingBinding
import android.widget.Toast
import com.example.project.database.dataclass.DisplayItem
import com.example.project.ui.profile.Profile
import android.util.Log // Import Log for debugging

class RatingActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RatingAdapter
    private lateinit var binding: ActivityRatingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        recyclerView = binding.rvRating
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RatingAdapter { clickedItem ->
            showRatingInputDialog(clickedItem)
        }
        recyclerView.adapter = adapter

        val userEmail = intent.getStringExtra("email")
        if (userEmail != null) {
            userViewModel.getCurrUser(userEmail)
        } else {
            Toast.makeText(this, "User email not provided.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userViewModel.currUser.observe(this) { user ->
            if (user != null) {
                Log.e("user", user.toString())
                userViewModel.loadCombined()
                binding.backbtn.setOnClickListener {
                    val intent = Intent(this, Profile::class.java)
                    intent.putExtra("email", user.email)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this, "Failed to load user data.", Toast.LENGTH_SHORT).show()
            }
        }


        userViewModel.combinedTransactionHistory.observe(this){ displayItems ->
            Log.d("RatingActivity", "Observed combinedTransactionHistory: ${displayItems.size} items")
            adapter.submitList(displayItems)

            if (displayItems.isNullOrEmpty()) {
                binding.tvNoReview.visibility = View.VISIBLE
                binding.rvRating.visibility = View.GONE
            } else {
                binding.tvNoReview.visibility = View.GONE
                binding.rvRating.visibility = View.VISIBLE
            }
        }

        userViewModel.resresponse.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        userViewModel.isLoading.observe(this) { isLoading ->
            binding.progresBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvRating.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.tvNoReview.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun showRatingInputDialog(item: DisplayItem) {
        // Ensure that we only show the dialog if the item hasn't been rated yet
        if (item.rating != null) {
            Toast.makeText(this, "Anda sudah memberikan rating untuk produk ini.", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Opening rating for ${item.productName}", Toast.LENGTH_SHORT).show()

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Beri Rating untuk ${item.productName}")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null)
        val etRating = view.findViewById<android.widget.EditText>(R.id.etRating)
        val etReview = view.findViewById<android.widget.EditText>(R.id.etReview)

        builder.setView(view)

        builder.setPositiveButton("Kirim") { dialog, which ->
            val rating = etRating.text.toString().toDoubleOrNull()
            val review = etReview.text.toString()

            if (rating != null && rating >= 1.0 && rating <= 5.0) {
                userViewModel.submitProductRating(
                    itemId = item.itemId,
                    transactionId = item.transactionId,
                    sellerId = item.sellerId,
                    ratingValue = rating,
                    reviewText = review
                )
            } else {
                Toast.makeText(this, "Rating tidak valid (harus 1.0-5.0)", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }
}