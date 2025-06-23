package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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

        val layout = layoutInflater.inflate(R.layout.custom_toast, null)
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        val toastImage = layout.findViewById<ImageView>(R.id.toastimage)

        binding.RegisterBtn.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }

        Glide.with(this)
            .asGif()
            .load(R.drawable.rotate)
            .preload()

        Glide.with(this)
            .asGif()
            .load(R.drawable.like)
            .preload()

        Glide.with(this)
            .asGif()
            .load(R.drawable.error)
            .preload()

        binding.LoginBtn.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || !email.contains("@gmail.com")) {
                toastText.text = "Invalid email or password"
                Glide.with(this)
                    .asGif()
                    .load(R.drawable.error)
                    .into(toastImage)
                with (Toast(applicationContext)) {
                    duration = Toast.LENGTH_LONG
                    view = layout
                    show()
                }
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
            toastText.text = response
            if (viewModels.checkres.value == false){
                Glide.with(this)
                    .asGif()
                    .load(R.drawable.error)
                    .into(toastImage)
            }
            else{
                Glide.with(this)
                    .asGif()
                    .load(R.drawable.like)
                    .into(toastImage)
            }
            with (Toast(applicationContext)) {
                duration = Toast.LENGTH_LONG
                view = layout
                show()
            }
        }

        viewModels.checkres.observe(this) { success ->
            if (success) {
                toastText.text = "Login successful"
                val email = binding.editTextEmail.text.toString()
                viewModels.loginDestination.observe(this) { destination ->
                    val intent = when (destination) {
                        "HomeUser" -> Intent(this, HomeUser::class.java)
                        "HomeManager" -> Intent(this, HomeManager::class.java)
//                        "HomeStaffs" -> Intent(this, HomeStaffs::class.java)
                        else -> null
                    }
                    intent?.putExtra("email", email)
                    Glide.with(this)
                        .asGif()
                        .load(R.drawable.like)
                        .into(toastImage)
                    with (Toast(applicationContext)) {
                        duration = Toast.LENGTH_LONG
                        view = layout
                        show()
                    }
                    intent?.let {
                        startActivity(it)
                        finish()
                    }
                }

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