package com.example.project

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val spinner = findViewById<Spinner>(R.id.listBank)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.bankoption,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val backbtn = findViewById<ImageView>(R.id.backbtn)
        backbtn.setOnClickListener{
            val intent = Intent(this, HomeUser::class.java)
            startActivity(intent)
        }

        viewModel.currUser.observe(this) { user ->
            if (user != null) {
                binding.namewdDis.text = user.name
                binding.balancewdDis.text = "Rp." + user.balance.toString()


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

                        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
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