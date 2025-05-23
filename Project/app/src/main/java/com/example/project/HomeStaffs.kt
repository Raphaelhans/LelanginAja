package com.example.project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityHomeStaffsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeStaffs : AppCompatActivity() {
    private lateinit var binding: ActivityHomeStaffsBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeStaffsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email")
        if (email != null) {
            fetchStaffName(email)
        } else {
            Toast.makeText(this, "Error: No email provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchStaffName(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val query = db.collection("Staffs")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!query.isEmpty) {
                    val staff = query.documents.first().toObject(Staff::class.java)
                    val name = staff?.name ?: "Staff"
                    withContext(Dispatchers.Main) {
                        binding.textViewGreeting.text = "Halo $name"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeStaffs, "Staff not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeStaffs, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}