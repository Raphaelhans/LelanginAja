package com.example.project

import android.app.Dialog
import android.content.Intent
import  android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.project.databinding.ActivityWithdrawBinding
import java.text.NumberFormat
import java.util.Locale

class Withdraw : AppCompatActivity() {
    private lateinit var binding: ActivityWithdrawBinding
    val viewModel by viewModels<UserViewModel>()
    private lateinit var dialog: Dialog
    var amount = 0
    val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
    private lateinit var formattedList:List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var dropdown = findViewById<AutoCompleteTextView>(R.id.listBank)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.pin_confirmation)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.custom_dialog_bg))
        dialog.setCancelable(true)

        val confirmButton = dialog.findViewById<Button>(R.id.btnNext)
        val pin = dialog.findViewById<EditText>(R.id.wdpin)

        viewModel.currUser.observe(this) { user ->
            if (user != null) {
                binding.namewdDis.text = user.name
                binding.balancewdDis.text = "Rp." + formatter.format(user.balance)

                binding.backbtn.setOnClickListener{
                    val intent = Intent(this, HomeUser::class.java)
                    intent.putExtra("email", viewModel.currUser.value?.email)
                    startActivity(intent)
                }

                binding.btnwd.setOnClickListener {
                    if (binding.wdbalanceTxt.text.toString().isNotEmpty()){
                        amount = binding.wdbalanceTxt.text.toString().replace(".", "").toInt()
                        if (amount >= 10000 && amount <= user.balance  &&  viewModel.currUser.value?.pin != ""){
                            dialog.show()
                        }
                        else if (viewModel.currUser.value?.pin == ""){
                            Toast.makeText(this, "Please create PIN first", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                    }
                }

                confirmButton.setOnClickListener {
                    if (pin.text.isNotEmpty() && pin.text.length == 4){
                        val selectedItem = dropdown.text.toString().split(" - ")
                        viewModel.withdrawConfirmation(amount, pin.text.toString(), selectedItem[0], selectedItem[2], selectedItem[1])
                    }
                    else{
                        Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
        }

        viewModel.userBankAccount.observe(this){ account ->
            formattedList = viewModel.userBankAccount.value?.map { "${it.bankName} - ${it.accountHolder} - ${it.accountNumber}" } ?: emptyList()

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, formattedList)
            dropdown.setAdapter(adapter)
            dropdown.setSelection(0)

            dropdown.setOnClickListener {
                dropdown.showDropDown()
            }
        }

        viewModel.resresponse.observe(this){ response ->
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
            if (response == "Withdraw successful"){
                dialog.dismiss()
            }
        }

        binding.wdbalanceTxt.addTextChangedListener(object : TextWatcher{
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                binding.wdbalanceTxt.removeTextChangedListener(this)
                val rawInput = s.toString().replace("[^0-9]".toRegex(), "")

                if (rawInput.isNotEmpty()) {
                    try {
                        val number = rawInput.toLong()

                        formatter.minimumFractionDigits = 0
                        formatter.maximumFractionDigits = 0
                        val formatted = formatter.format(number)

                        if (formatted != current) {
                            current = formatted
                            binding.wdbalanceTxt.setText(formatted)
                            binding.wdbalanceTxt.setSelection(formatted.length)
                        }
                    } catch (e: NumberFormatException) {
                        binding.wdbalanceTxt.setText(current)
                    }
                } else {
                    current = ""
                    binding.wdbalanceTxt.setText("")
                }

                binding.wdbalanceTxt.addTextChangedListener(this)
            }

        })

    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        if (email != null) {
            viewModel.getCurrUser(email)
        }
    }
}