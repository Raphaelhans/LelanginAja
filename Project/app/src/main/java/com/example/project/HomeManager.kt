package com.example.project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityHomeManagerBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeManager : AppCompatActivity() {
    private lateinit var binding: ActivityHomeManagerBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email")
        if (email != null) {
            fetchManagerName(email)
        } else {
            Toast.makeText(this, "Error: No email provided", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchManagerName(email: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val query = db.collection("Staffs")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!query.isEmpty) {
                    val staff = query.documents.first().toObject(Staff::class.java)
                    val name = staff?.name ?: "Manager"
                    withContext(Dispatchers.Main) {
                        binding.textViewGreeting.text = "Halo $name"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeManager, "Manager not found", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeManager, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}