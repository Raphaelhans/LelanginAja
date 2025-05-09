package com.example.project.ui.profile

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.AuthViewModel
import com.example.project.R
import com.example.project.UserViewModel
import com.example.project.databinding.ActivityAccNumberBinding

class AccNumber : AppCompatActivity() {
    private lateinit var binding: ActivityAccNumberBinding
    val viewModels by viewModels<UserViewModel>()
    val email = intent.getStringExtra("email")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}