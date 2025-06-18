package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ActivityAuctiondetailBinding
import com.example.project.databinding.ActivityHistoryBinding
import com.example.project.databinding.ActivityHomeUserBinding
import com.example.project.databinding.ItemHistoryBinding
import kotlinx.coroutines.Job
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class History : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val viewModels by viewModels<HistoryViewModel>()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyAdapter = HistoryAdapter()
        binding.rvHistory.apply {
            layoutManager = LinearLayoutManager(this@History)
            adapter = historyAdapter
        }

        binding.backbtn.setOnClickListener {
            val intent = Intent(this, HomeUser::class.java)
            startActivity(intent)
            finish()
        }

        viewModels.historyList.observe(this) { historyList ->
            historyAdapter.submitList(historyList ?: emptyList())

            if (historyList.isNullOrEmpty()) {
                binding.tvNoTransaction.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
            } else {
                binding.tvNoTransaction.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
            }
        }

        viewModels.resresponse.observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModels.isLoading.observe(this) { isLoading ->
            binding.progresBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvHistory.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.tvNoTransaction.visibility = if (isLoading) View.GONE else View.VISIBLE // Adjust visibility of no transaction text as needed
        }
    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        if (email != null) {
            viewModels.getCurrUserHistory(email)
        }
    }
}
