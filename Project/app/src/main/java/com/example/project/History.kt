package com.example.project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ActivityHistoryBinding
import com.example.project.databinding.ActivityHomeUserBinding
import com.example.project.databinding.ItemHistoryBinding

class History : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    val viewModels by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val rv = binding.rvHistory
    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        viewModels.getCurrUser(email!!)
    }
}