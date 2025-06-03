package com.example.project.ui.transaction

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.project.databinding.ActivityTransactionDetailBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TransactionDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionDetailBinding
    private val db = FirebaseFirestore.getInstance()

    private var transaksiId: String = ""
    private var produkId: String = ""
    private var buyerId: String = ""
    private var sellerId: String = ""
    private var bidAmount: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transaksiId = intent.getStringExtra("transaksi_id") ?: return
        produkId = intent.getStringExtra("produk_id") ?: ""
        buyerId = intent.getStringExtra("buyer_id") ?: ""
        sellerId = intent.getStringExtra("seller_id") ?: ""
        bidAmount = intent.getDoubleExtra("bidAmount", 0.0)

        getTransactionDetails()

        binding.completeButton.setOnClickListener {
            completeTransaction()
        }
    }

    private fun getTransactionDetails() {
        lifecycleScope.launch {
            db.collection("Products").document(produkId).get().addOnSuccessListener { doc ->
                binding.detailName.text = doc.getString("name")
                binding.detailLocation.text = doc.getString("city")
                Glide.with(this@TransactionDetailActivity).load(doc.getString("image_url")).into(binding.detailImage)
            }

            db.collection("Users").whereEqualTo("user_id", sellerId.toInt()).get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        val user = snapshot.documents[0]
                        binding.sellerName.text = user.getString("name")
                        binding.sellerAddress.text = user.getString("location")
                    }
                }
        }
    }

    private fun completeTransaction() {
        val transaksiRef = db.collection("Transaksi").document(transaksiId)
        val produkRef = db.collection("Products").document(produkId)

        transaksiRef.update("status", "complete")
        produkRef.update("status", 2)

        // Transfer saldo buyer -> seller
        lifecycleScope.launch {
            val buyerSnap = db.collection("Users").whereEqualTo("user_id", buyerId.toInt()).get().await()
            val sellerSnap = db.collection("Users").whereEqualTo("user_id", sellerId.toInt()).get().await()

            val buyerDoc = buyerSnap.documents.first()
            val sellerDoc = sellerSnap.documents.first()

            val buyerBalance = buyerDoc.getLong("balance") ?: 0
            val sellerBalance = sellerDoc.getLong("balance") ?: 0

            db.collection("Users").document(buyerDoc.id).update("balance", buyerBalance - bidAmount.toLong())
            db.collection("Users").document(sellerDoc.id).update("balance", sellerBalance + bidAmount.toLong())

            Toast.makeText(this@TransactionDetailActivity, "Transaksi selesai!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
