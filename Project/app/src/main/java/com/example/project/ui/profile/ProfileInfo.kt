package com.example.project.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.R
import com.example.project.UserViewModel
import com.example.project.databinding.ActivityProfileInfoBinding
import com.example.project.databinding.PopUpMenuBinding

class ProfileInfo : AppCompatActivity() {
    private lateinit var binding: ActivityProfileInfoBinding
    val viewModel by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.currUser.observe(this) { user ->
            binding.userName.text = viewModel.currUser.value?.name
            binding.userNumber.text = viewModel.currUser.value?.phone
            binding.UserPass.text = "*********"
            binding.userEmail.text = viewModel.currUser.value?.email

            binding.editName.setOnClickListener{
                showEditDialog("Name", viewModel.currUser.value?.name!!) { newValue ->
                    if (newValue.isNotEmpty() && newValue != viewModel.currUser.value?.name){
                        viewModel.editProfile("Name", newValue)
                        binding.userName.text = viewModel.currUser.value?.name
                    }
                }
            }

            binding.backBtn.setOnClickListener {
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                finish()
            }
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

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        viewModel.getCurrUser(email!!)
    }

}