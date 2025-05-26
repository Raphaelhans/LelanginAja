package com.example.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityAddPegawaiBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AddPegawaiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPegawaiBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPegawaiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonAddStaff.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val name = binding.editTextName.text.toString()
            val password = binding.editTextPassword.text.toString()
            val phone = binding.editTextPhone.text.toString()

            if (email.isEmpty() || name.isEmpty() || password.isEmpty() || phone.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            } else {
                addStaff(email, name, password, phone)
            }
        }
    }

    private fun addStaff(email: String, name: String, password: String, phone: String) {
        db.collection("Staffs")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { emailQuery ->
                if (!emailQuery.isEmpty) {
                    Toast.makeText(this@AddPegawaiActivity, "Email already taken", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }


                db.collection("Staffs")
                    .orderBy("id_staff", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { highestIdQuery ->
                        val highestId = if (highestIdQuery.isEmpty) {
                            1
                        } else {
                            highestIdQuery.documents.first().toObject(Staff::class.java)?.id_staff?.plus(1) ?: 1
                        }


                        val staff = Staff(
                            id_staff = highestId,
                            name = name,
                            phone = phone,
                            email = email,
                            password = password,
                            status = false,
                            suspended = false
                        )


                        db.collection("Staffs").document(highestId.toString()).set(staff)
                            .addOnSuccessListener {
                                Toast.makeText(this@AddPegawaiActivity, "Staff added successfully", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@AddPegawaiActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this@AddPegawaiActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@AddPegawaiActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}