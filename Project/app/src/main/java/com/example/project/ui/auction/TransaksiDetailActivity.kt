package com.example.project.ui.auction

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        if (status.lowercase() == "pending") {
            binding.acceptButton.visibility = android.view.View.GONE
        } else {
            binding.acceptButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun completeTransaction(transaksiId: String) {
        db.collection("Transaksi").document(transaksiId)
            .update("status", "Selesai")
            .addOnSuccessListener {
                Toast.makeText(this, "Transaksi berhasil diselesaikan!", Toast.LENGTH_SHORT).show()
                // Tambahkan logika update saldo jika diperlukan
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menyelesaikan transaksi", Toast.LENGTH_SHORT).show()
            }
    }
}
