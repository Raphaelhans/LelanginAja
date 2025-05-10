package com.example.project.ui.transaction

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.HomeUser
import com.example.project.ui.profile.Profile
import com.example.project.R
import com.example.project.databinding.ActivityTransactionBinding

class Transaction : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionBinding
    private lateinit var recyclerView: RecyclerView
//    private lateinit var viewModel: TransactionHistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transactions = TransData.getAllTransactions()

        recyclerView = findViewById(R.id.transRecycler)

        val adapter = TransactionAdapter(transactions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        binding.profilebtn.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
            finish()
        }

        binding.homebtn.setOnClickListener {
            val intent = Intent(this, HomeUser::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}