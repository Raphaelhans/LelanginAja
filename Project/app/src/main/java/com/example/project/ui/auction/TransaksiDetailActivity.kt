package com.example.project.ui.auction

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.HomeUser
import com.example.project.databinding.ActivityTransaksiDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class TransaksiDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransaksiDetailBinding
    private val db = FirebaseFirestore.getInstance()

    private var transaksiId: String = ""
    private var buyerId: String = ""
    private var sellerId: String = ""
    private var bidAmount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transaksiId = intent.getStringExtra("transaksi_id") ?: ""
        val itemName = intent.getStringExtra("itemName") ?: "-"
        val lastBid = intent.getStringExtra("lastBid") ?: "0"
        val status = intent.getStringExtra("status") ?: "-"
        val sellerName = intent.getStringExtra("sellerName") ?: "-"
        val sellerAddress = intent.getStringExtra("sellerAddress") ?: "-"

        bidAmount = lastBid.toIntOrNull() ?: 0

        binding.itemName.text = "Barang: $itemName"
        binding.lastBid.text = "Harga Terakhir: Rp $lastBid"
        binding.status.text = "Status: $status"
        binding.sellerName.text = "Penjual: $sellerName"
        binding.sellerAddress.text = "Alamat: $sellerAddress"

        binding.acceptButton.setOnClickListener {
            if (transaksiId.isNotEmpty()) {
                completeTransaction(transaksiId)
            } else {
                Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
        if (status.lowercase() == "pending" || status.lowercase() == "complete") {
            binding.acceptButton.visibility = android.view.View.GONE
        } else {
            binding.acceptButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun completeTransaction(transaksiId: String) {
        db.collection("Transaksi").document(transaksiId)
            .get()
            .addOnSuccessListener { document ->
                val buyerId = document.getString("buyer_id") ?: return@addOnSuccessListener
                val sellerId = document.getString("seller_id") ?: return@addOnSuccessListener
                val bidAmount = document.getDouble("bidAmount") ?: return@addOnSuccessListener
                val produkId = document.getString("produk_id") ?: return@addOnSuccessListener

                db.collection("Transaksi").document(transaksiId)
                    .update("status", "complete")
                    .addOnSuccessListener {
                        db.collection("Products").document(produkId)
                            .update("status", 2)
                            .addOnSuccessListener {
                                updateSaldoAndGoHome(buyerId.toInt(), sellerId.toInt(), bidAmount.toLong())
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Gagal mengupdate status produk", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyelesaikan transaksi", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateSaldoAndGoHome(buyerId: Int, sellerId: Int, amount: Long) {
        val usersRef = db.collection("Users")

        usersRef.whereEqualTo("user_id", buyerId).get()
            .addOnSuccessListener { buyerSnap ->
                if (!buyerSnap.isEmpty) {
                    val buyerDoc = buyerSnap.documents[0]
                    val buyerSaldo = buyerDoc.getLong("balance") ?: 0

                    if (buyerSaldo >= amount) {
                        usersRef.document(buyerDoc.id)
                            .update("balance", buyerSaldo - amount)

                        usersRef.whereEqualTo("user_id", sellerId).get()
                            .addOnSuccessListener { sellerSnap ->
                                if (!sellerSnap.isEmpty) {
                                    val sellerDoc = sellerSnap.documents[0]
                                    val sellerSaldo = sellerDoc.getLong("balance") ?: 0

                                    usersRef.document(sellerDoc.id)
                                        .update("balance", sellerSaldo + amount)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Transaksi selesai. Kembali ke Home.", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, HomeUser::class.java))
                                            finish()
                                        }
                                }
                            }
                    } else {
                        Toast.makeText(this, "Saldo tidak cukup untuk menyelesaikan transaksi", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }



}
