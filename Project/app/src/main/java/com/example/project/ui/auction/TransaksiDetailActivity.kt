package com.example.project.ui.auction

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityTransaksiDetailBinding

class TransaksiDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransaksiDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransaksiDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemName = intent.getStringExtra("itemName") ?: "-"
        val lastBid = intent.getIntExtra("lastBid", 0)
        val status = intent.getStringExtra("status") ?: "-"
        val sellerName = intent.getStringExtra("sellerName") ?: "-"
        val sellerAddress = intent.getStringExtra("sellerAddress") ?: "-"

        binding.itemName.text = "Barang: $itemName"
        binding.lastBid.text = "Harga Terakhir: Rp $lastBid"
        binding.status.text = "Status: $status"
        binding.sellerName.text = "Penjual: $sellerName"
        binding.sellerAddress.text = "Alamat: $sellerAddress"

        binding.acceptButton.setOnClickListener {
            Toast.makeText(this, "Barang telah diterima dan transaksi selesai!", Toast.LENGTH_SHORT).show()
        }
    }
}
