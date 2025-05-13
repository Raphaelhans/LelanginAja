package com.example.project

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
    val cities = arrayOf("Surabaya", "Malang", "Sidoarjo", "Kediri", "Jember")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.LogBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.RegisBtn.setOnClickListener {
            val name = binding.Nametext.text.toString()
            val phone = binding.Numbertext.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.PasswordText.text.toString()
            val confirmPassword = binding.editTextPassword.text.toString()
            val location = binding.Lokasitxt.selectedItem.toString()

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (!email.contains("@gmail.com")) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
            } else {
                binding.RegisBtn.text = ""
//                binding.loadingRegis.visibility = View.VISIBLE
//                Glide.with(this)
//                    .asGif()
//                    .load(com.example.project.R.drawable.rotate)
//                    .into(binding.loadingRegis)
                viewModel.registerUser(name, phone, email, password, 0, 0, location)
            }
        }

        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, cities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.Lokasitxt.adapter = adapter
        binding.Lokasitxt.setSelection(0)

        viewModel.checkres.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                binding.RegisBtn.text = "Register"
//                binding.loadingRegis.visibility = View.GONE
            }
        }

        viewModel.resresponse.observe(this) { response ->
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
            binding.RegisBtn.text = "Register"
//            binding.loadingRegis.visibility = View.GONE
        }



    }
}