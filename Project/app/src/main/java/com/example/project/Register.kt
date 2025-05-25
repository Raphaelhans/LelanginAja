package com.example.project

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
import com.example.project.database.dataclass.Users
import com.example.project.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    val viewModel by viewModels<AuthViewModel>()
    val viewModel by viewModels<AuthViewModel>()
    val cities = arrayOf("Surabaya", "Malang", "Sidoarjo", "Kediri", "Jember")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layout = layoutInflater.inflate(com.example.project.R.layout.custom_toast, null)
        val toastText = layout.findViewById<TextView>(com.example.project.R.id.toast_text)
        val toastImage = layout.findViewById<ImageView>(com.example.project.R.id.toastimage)

        binding.LogBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        Glide.with(this)
            .asGif()
            .load(com.example.project.R.drawable.rotate)
            .preload()

        Glide.with(this)
            .asGif()
            .load(com.example.project.R.drawable.like)
            .preload()

        Glide.with(this)
            .asGif()
            .load(com.example.project.R.drawable.error)
            .preload()

        binding.RegisBtn.setOnClickListener {
            val name = binding.Nametext.text.toString()
            val phone = binding.Numbertext.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.PasswordText.text.toString()
            val confirmPassword = binding.editTextPassword.text.toString()
//            val location = binding.Lokasitxt.selectedItem.toString()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                toastText.text = "Please Fill All Fields"
                Glide.with(this)
                    .asGif()
                    .load(com.example.project.R.drawable.error)
                    .into(toastImage)
            } else if (password != confirmPassword) {
                toastText.text = "Password Do Not Match"
                Glide.with(this)
                    .asGif()
                    .load(com.example.project.R.drawable.error)
                    .into(toastImage)
            } else if (!email.contains("@gmail.com")) {
                toastText.text = "Invalid email"
                Glide.with(this)
                    .asGif()
                    .load(com.example.project.R.drawable.error)
                    .into(toastImage)
            } else {
                binding.RegisBtn.visibility = View.GONE
                binding.loadingGif.visibility = View.VISIBLE
                Glide.with(this)
                    .asGif()
                    .load(com.example.project.R.drawable.rotate)
                    .into(binding.loadingGif)
                viewModel.registerUser(name, phone, email, password, 0, 0, "")
            }
            with (Toast(applicationContext)) {
                duration = Toast.LENGTH_LONG
                view = layout
                show()
            }
        }

        viewModel.checkres.observe(this) { success ->
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.Lokasitxt.adapter = adapter
        binding.Lokasitxt.setSelection(0)

        viewModel.checkres.observe(this) { success ->
            if (success) {
                toastText.text = "Registration Successfull"
                Glide.with(this)
                    .asGif()
                    .load(com.example.project.R.drawable.like)
                    .into(toastImage)
                with (Toast(applicationContext)) {
                    duration = Toast.LENGTH_LONG
                    view = layout
                    show()
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                binding.RegisBtn.visibility = View.VISIBLE
                binding.loadingGif.visibility = View.GONE
            }
        }

        viewModel.resresponse.observe(this) { response ->
            toastText.text = "Invalid email or password"
            if (viewModel.checkres.value == false){
                Glide.with(this)
                    .asGif()
                    .load(com.example.project.R.drawable.error)
                    .into(toastImage)
            }
            with (Toast(applicationContext)) {
                duration = Toast.LENGTH_LONG
                view = layout
                show()
            }
            binding.RegisBtn.text = "Register"
//            binding.loadingGif.visibility = View.GONE
        }



    }
}