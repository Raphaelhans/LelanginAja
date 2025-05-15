package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val viewModels by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.RegisterBtn.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.rotate)
            .preload()

        binding.LoginBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || !email.contains("@gmail.com")) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                binding.LoginBtn.visibility = View.GONE
                Glide.with(this)
                    .asGif()
                    .load(R.drawable.rotate)
                    .into(binding.loadingLogin)
                binding.loadingLogin.visibility = View.VISIBLE
                viewModels.loginUser(email, password)
            }
        }

        viewModels.resresponse.observe(this){ response ->
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
        }

        viewModels.checkres.observe(this) { success ->
            if (success) {
                val intent = Intent(this, HomeUser::class.java)
                intent.putExtra("email", binding.editTextEmail.text.toString())
                Toast.makeText(getApplication(), "Login Success", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
            else{
                binding.LoginBtn.visibility = View.VISIBLE
                binding.loadingLogin.visibility = View.GONE
            }
        }


    }
}