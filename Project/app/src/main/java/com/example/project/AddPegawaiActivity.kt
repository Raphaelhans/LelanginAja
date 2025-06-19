package com.example.project

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityAddPegawaiBinding

class AddPegawaiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPegawaiBinding
    private val viewModel: ManagerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()

        binding.buttonAddStaff.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val name = binding.editTextName.text.toString()
            val password = binding.editTextPassword.text.toString()
            val phone = binding.editTextPhone.text.toString()

            if (email.isEmpty() || name.isEmpty() || password.isEmpty() || phone.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addStaff(email, name, password, phone)
            }
        }
    }

    private fun setupObservers() {
        viewModel.addStaffResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this@AddPegawaiActivity, "Staff added successfully", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }.onFailure { e ->
                Toast.makeText(this@AddPegawaiActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}