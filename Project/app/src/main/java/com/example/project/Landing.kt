package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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
        Glide.with(this)
            .asGif()
            .load(R.drawable.loadingani)
            .preload()

        binding2.btnNext2.setOnClickListener {
            binding2.btnNext2.visibility = View.GONE

            Glide.with(this)
                .asGif()
                .load(R.drawable.loadingani)
                .into(binding2.loadingGIf)
            binding2.loadingGIf.visibility = View.VISIBLE
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
