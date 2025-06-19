package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ActivityRatingBinding // Assuming you're using ViewBinding
import android.widget.Toast // For showing messages
import com.example.project.database.dataclass.DisplayItem
import com.example.project.ui.profile.Profile

class RatingActivity : AppCompatActivity() {
    private lateinit var userViewModel: UserViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RatingAdapter
    private lateinit var binding: ActivityRatingBinding // Declare binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        recyclerView = binding.rvRating
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = RatingAdapter(emptyList()) { clickedItem ->
            showRatingInputDialog(clickedItem)
        }
        recyclerView.adapter = adapter

        userViewModel.combinedTransactionHistory.observe(this){ displayItems ->
            adapter = RatingAdapter(displayItems) { clickedItem ->
                showRatingInputDialog(clickedItem)
            }
            recyclerView.adapter = adapter
        }

        userViewModel.loadCombined()

        userViewModel.resresponse.observe(this) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                // userViewModel.clearResResponse() // You might need to add this function to ViewModel
            }
        }

        binding.backbtn.setOnClickListener{
            val intent = Intent(this, Profile::class.java)
            val userEmail = userViewModel.currUser.value?.email
            if (userEmail != null) {
                intent.putExtra("email", userEmail)
            } else {
                Toast.makeText(this, "User email not available.", Toast.LENGTH_SHORT).show()
            }
            startActivity(intent)
            finish()
        }
    }

    private fun showRatingInputDialog(item: DisplayItem) {
        Toast.makeText(this, "Opening rating for ${item.productName}", Toast.LENGTH_SHORT).show()

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Beri Rating untuk ${item.productName}")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null)
        val etRating = view.findViewById<android.widget.EditText>(R.id.etRatingValue)
        val etReview = view.findViewById<android.widget.EditText>(R.id.etReviewText)

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