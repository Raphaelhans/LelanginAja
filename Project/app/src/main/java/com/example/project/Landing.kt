package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.Landing1Binding
import com.example.project.databinding.Landing2Binding

class LandingActivity : AppCompatActivity() {
    private lateinit var binding1: Landing1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding1 = Landing1Binding.inflate(layoutInflater)
        setContentView(binding1.root)

        binding1.btnNext1.setOnClickListener {
            val intent = Intent(this, Landing2Activity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

class Landing2Activity : AppCompatActivity() {
    private lateinit var binding2: Landing2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding2 = Landing2Binding.inflate(layoutInflater)
        setContentView(binding2.root)

        binding2.btnNext2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
