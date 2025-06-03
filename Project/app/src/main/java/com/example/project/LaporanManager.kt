package com.example.project

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityLaporanManagerBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executors

class LaporanManager : AppCompatActivity() {

    private lateinit var binding: ActivityLaporanManagerBinding
    private val db = FirebaseFirestore.getInstance()
    private val executor = Executors.newSingleThreadExecutor()


    data class Transaction(
        val bid: Int = 0,
        val buyer_id: String = "",
        val produk_id: String = "",
        val seller_id: String = "",
        val status: String = "",
        val time_bid: Long = 0L,
        val transaksiId: String = ""
    )

    data class Products(
        val items_id: String = "",
        val category_id: String = "",
        val seller_id: Int = 0,
        val status: Int = 0,
        val name: String = "",
        val description: String = "",
        val city: String = "",
        val address: String = "",
        val start_date: String = "",
        val end_date: String = "",
        val start_bid: Int = 0,
        val end_bid: Int = 0,
        val image_url: String = "",
        val buyer_id: Int = 0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        fetchReportData()
    }

    private fun fetchReportData() {

        executor.execute {
            db.collection("Transactions")
                .get()
                .addOnSuccessListener { transactionSnapshot ->
                    val transactions = transactionSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Transaction::class.java)?.copy(transaksiId = doc.id)
                    }


                    db.collection("Products")
                        .get()
                        .addOnSuccessListener { productSnapshot ->
                            val products = productSnapshot.documents.mapNotNull { doc ->
                                doc.toObject(Products::class.java)?.copy(items_id = doc.id)
                            }


                            computeAndDisplayReport(transactions, products)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore Error", "Error fetching products: ${e.message}", e)
                            runOnUiThread {
                                binding.tvSuccessfulAuctions.text = "Error: ${e.message}"
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore Error", "Error fetching transactions: ${e.message}", e)
                    runOnUiThread {
                        binding.tvSuccessfulAuctions.text = "Error: ${e.message}"
                    }
                }
        }
    }

    private fun computeAndDisplayReport(transactions: List<Transaction>, products: List<Products>) {

        val successfulAuctions = transactions.count { it.status == "completed" }
        runOnUiThread {
            binding.tvSuccessfulAuctions.text = successfulAuctions.toString()
        }


        val buyerCounts = transactions.groupBy { it.buyer_id }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val topBuyersText = if (buyerCounts.isNotEmpty()) {
            buyerCounts.joinToString("\n") { "Buyer ID: ${it.first} (${it.second} bids)" }
        } else {
            "No buyers found"
        }
        runOnUiThread {
            binding.tvTopBuyers.text = topBuyersText
        }


        val sellerCounts = products.groupBy { it.seller_id.toString() }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val topSellersText = if (sellerCounts.isNotEmpty()) {
            sellerCounts.joinToString("\n") { "Seller ID: ${it.first} (${it.second} listings)" }
        } else {
            "No sellers found"
        }
        runOnUiThread {
            binding.tvTopSellers.text = topSellersText
        }


        val categoryCounts = products.groupBy { it.category_id }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val popularCategoriesText = if (categoryCounts.isNotEmpty()) {
            categoryCounts.joinToString("\n") { "${it.first} (${it.second} listings)" }
        } else {
            "No categories found"
        }
        runOnUiThread {
            binding.tvPopularCategories.text = popularCategoriesText
        }


        val cityCounts = products.groupBy { it.city }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        val topCitiesText = if (cityCounts.isNotEmpty()) {
            cityCounts.joinToString("\n") { "${it.first} (${it.second} auctions)" }
        } else {
            "No cities found"
        }
        runOnUiThread {
            binding.tvTopCities.text = topCitiesText
        }
    }
}