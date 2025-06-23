package com.example.project

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.project.database.dataclass.BankAccount
import com.example.project.databinding.ActivityAccountNumberBinding
import com.example.project.ui.profile.Profile

class AccountNumber : BaseClass() {
    private lateinit var binding: ActivityAccountNumberBinding
    val viewModel by viewModels<UserViewModel>()
    val option = arrayOf("Bank Mandiri", "Bank Permata", "Bank Danamon", "Bank BNI", "Bank BCA")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.bank)
            .into(binding.loadingBank)
        binding.notfoundTxt.visibility = View.VISIBLE
        binding.loadingBank.visibility = View.VISIBLE
        binding.addedAccView.visibility = View.GONE

        val dropdown = findViewById<AutoCompleteTextView>(R.id.listBank)

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, option)
        dropdown.setAdapter(adapter)
        dropdown.setSelection(0)

        dropdown.setOnClickListener {
            dropdown.showDropDown()
        }

        val adapterRecycler = AccountNumberAdapter()
        binding.addedAccView.layoutManager = LinearLayoutManager(this)
        binding.addedAccView.adapter = adapterRecycler

        viewModel.currUser.observe(this) { user ->
            if (user != null){
                binding.btnAddAcc.setOnClickListener {
                    val accName = binding.accnameTxt.text.toString()
                    val accNumber = binding.accnumberTxt.text.toString()

                    if (accName.isNotEmpty() && accNumber.isNotEmpty() && dropdown.text.isNotEmpty() && accNumber.length == 19){
                        viewModel.addBankAccount(dropdown.text.toString(), accName, accNumber)
                    }
                    else{
                        Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                    }
                }

                binding.backbtn.setOnClickListener {
                    val intent = Intent(this, Profile::class.java)
                    intent.putExtra("email", user.email)
                    startActivity(intent)
                    finish()
                }
            }
        }

        viewModel.userBankAccount.observe(this){ account ->
            if (!account.isNullOrEmpty()){
                binding.loadingBank.visibility = View.GONE
                binding.notfoundTxt.visibility = View.GONE
                binding.addedAccView.visibility = View.VISIBLE
                adapterRecycler.submitList(account)
                Log.d("Account", account.toString())
            }
            else{
                Glide.with(this)
                    .asGif()
                    .load(R.drawable.bank)
                    .into(binding.loadingBank)
                binding.notfoundTxt.visibility = View.VISIBLE
                binding.loadingBank.visibility = View.VISIBLE
                binding.addedAccView.visibility = View.GONE
            }

        }

        viewModel.resresponse.observe(this){ response ->
            Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
        }

        binding.accnumberTxt.addTextChangedListener(object :TextWatcher{
            private var isFormatting = false
            private var previousText = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s.toString()
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val digitsOnly = s.toString().replace(" ", "")
                val formatted = StringBuilder()

                for (i in digitsOnly.indices) {
                    formatted.append(digitsOnly[i])
                    if ((i + 1) % 4 == 0 && i != digitsOnly.length - 1) {
                        formatted.append(" ")
                    }
                }

                if (formatted.toString() != s.toString()) {
                    binding.accnumberTxt.setText(formatted.toString())
                    binding.accnumberTxt.setSelection(formatted.length)
                }

                isFormatting = false
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