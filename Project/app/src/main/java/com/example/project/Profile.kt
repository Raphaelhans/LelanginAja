package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.databinding.ActivityProfileBinding
import com.example.project.ui.transaction.Transaction

class Profile : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homebtn.setOnClickListener {
            val intent = Intent(this, HomeUser::class.java)
            startActivity(intent)
            finish()
        }

        binding.transBtn.setOnClickListener {
            val intent = Intent(this, Transaction::class.java)
            startActivity(intent)
            finish()
        }

        binding.logoutBtn.setOnClickListener{
            showOutsellDialog("Are you sure you want to logout?", "logout") { newValue ->

            }
        }

        binding.sellerBtn.setOnClickListener{
            showOutsellDialog("Are you sure you want to become a seller?", "seller") { newValue ->

            }
        }

        binding.profileInfoBtn.setOnClickListener {
            val intent = Intent(this, ProfileInfo::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showOutsellDialog(custext: String, mode: String,  onSave: (String) -> Unit) {
        if (mode == "seller"){
            AlertDialog.Builder(this)
                .setTitle(custext)
                .setPositiveButton("Yes") { _, _ ->

                }
                .setNegativeButton("No", null)
                .show()
        }
        else{
            AlertDialog.Builder(this)
                .setTitle(custext)
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }
}