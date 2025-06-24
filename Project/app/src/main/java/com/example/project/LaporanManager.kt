package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityLaporanManagerBinding

class LaporanManager : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanManagerBinding
    private val viewModel: ManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupBackButton()
        viewModel.fetchReportData()
    }

    private fun setupObservers() {
//        viewModel.successfulAuctions.observe(this) { count ->
//            binding.tvSuccessfulAuctions.text = count.toString()
//        }
//
//        viewModel.topBuyers.observe(this) { buyers ->
//            binding.tvTopBuyers.text = buyers
//        }

        viewModel.topSellers.observe(this) { sellers ->
            binding.tvTopSellers.text = sellers
        }

        viewModel.popularCategories.observe(this) { categories ->
            binding.tvPopularCategories.text = categories
        }

        viewModel.topCities.observe(this) { cities ->
            binding.tvTopCities.text = cities
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackButton() {
        binding.buttonBack.setOnClickListener {
            startActivity(Intent(this, HomeManager::class.java))
            finish()
        }
    }
}