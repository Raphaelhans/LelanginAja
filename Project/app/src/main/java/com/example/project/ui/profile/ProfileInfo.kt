package com.example.project.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.R
import com.example.project.UserViewModel
import com.example.project.databinding.ActivityProfileInfoBinding
import com.example.project.databinding.PopUpMenuBinding
import org.mindrot.jbcrypt.BCrypt

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
            binding.UserEmail.text = viewModel.currUser.value?.email
            if (viewModel.currUser.value?.pin == ""){
                binding.userPIN.text = "Set a PIN"
            }
            else{
                binding.userPIN.text = "*********"
            }

            binding.editName.setOnClickListener{
                showEditDialog("Name", viewModel.currUser.value?.name!!) { newValue ->
                    if (newValue.isNotEmpty() && newValue != viewModel.currUser.value?.name){
                        viewModel.editProfile("Name", newValue)
                        binding.userName.text = viewModel.currUser.value?.name
                    }
                    else{
                        Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            binding.editNumber.setOnClickListener{
                showEditDialog("Phone Number", viewModel.currUser.value?.phone!!) { newValue ->
                    if (newValue.isNotEmpty() && newValue != viewModel.currUser.value?.name && newValue.isDigitsOnly()){
                        viewModel.editProfile("Phone", newValue)
                        binding.userName.text = viewModel.currUser.value?.name
                    }
                    else{
                        Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            binding.editPass.setOnClickListener{
                showPasswordChangeDialog()
            }

            binding.editPIN.setOnClickListener {
                if (viewModel.currUser.value?.pin == ""){
                    showPINDialog(false)
                }
                else{
                    showPINDialog(true)
                }
            }

            binding.backbtn.setOnClickListener {
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                finish()
            }
        }

        viewModel.resresponse.observe(this) { response ->
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
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

    private fun showPasswordChangeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Password")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_password, null)
        val oldPasswordEditText = view.findViewById<EditText>(R.id.oldPasswordEditText)
        val newPasswordEditText = view.findViewById<EditText>(R.id.newPasswordEditText)

        builder.setView(view)

        builder.setPositiveButton("Save") { _, _ ->
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            if (oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                viewModel.changePassword(oldPassword, newPassword)
            } else {
                Toast.makeText(this, "Both fields must be filled", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showPINDialog(condition: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change PIN")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_pin, null)
        val currentPIN = view.findViewById<EditText>(R.id.currentPinEditText)
        if (condition){
            currentPIN.visibility = View.VISIBLE
        }
        else{
            currentPIN.visibility = View.GONE
        }
        val newPIN = view.findViewById<EditText>(R.id.newPinEditText)
        val confirmPIN = view.findViewById<EditText>(R.id.confirmPinEditText)

        builder.setView(view)

        builder.setPositiveButton("Save") { _, _ ->
            val newPin = newPIN.text.toString()
            val Conpin = confirmPIN.text.toString()
            if (newPin.isNotEmpty() && Conpin.isNotEmpty() && newPin == Conpin && !condition && newPin.length == 4) {
                viewModel.createPIN(newPin)
            }
            else if (newPin.isNotEmpty() && Conpin.isNotEmpty() && newPin == Conpin && condition && newPin.length == 4){
                val currPIN = currentPIN.text.toString()
                viewModel.changePIN(currPIN, newPin)
            }
            else {
                Toast.makeText(this, "Both fields must be filled, New PIN must be the same, and PIN must be 4 digits", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        viewModel.getCurrUser(email!!)
    }

}