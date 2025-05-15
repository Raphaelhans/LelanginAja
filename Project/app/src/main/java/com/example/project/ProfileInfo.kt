package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.databinding.ActivityProfileInfoBinding
import com.example.project.databinding.PopUpMenuBinding

class ProfileInfo : AppCompatActivity() {
    private lateinit var binding: ActivityProfileInfoBinding
    private var userName = "Jasong Oweng"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editName.setOnClickListener{
            showEditDialog("Name", userName) { newValue ->
                userName = newValue
                binding.userName.text = newValue
            }
        }

        binding.backbtn.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showEditDialog(fieldName: String, currentValue: String, onSave: (String) -> Unit) {
        val dialogBinding = PopUpMenuBinding.inflate(layoutInflater)
        dialogBinding.currentValue.text = "Current $fieldName: $currentValue"

        AlertDialog.Builder(this)
            .setTitle("Edit $fieldName")
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val newValue = dialogBinding.editTxt.text.toString()
                if (newValue.isNotEmpty()) {
                    onSave(newValue)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}