package com.example.project.ui.transaction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.BaseClass
import com.example.project.HomeUser
import com.example.project.ui.profile.Profile
import com.example.project.R
import com.example.project.UserViewModel
import com.example.project.databinding.ActivityTransactionBinding

class Transaction : BaseClass() {
    private lateinit var binding: ActivityTransactionBinding
    private lateinit var recyclerView: RecyclerView
    private val viewModel by viewModels<UserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        recyclerView = findViewById(R.id.transRecycler)

        viewModel.currUserTransaction.observe(this) { user ->
            val adapter = TransactionAdapter(user)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
        }

        viewModel.currUser.observe(this) { user ->
            binding.profilebtn.setOnClickListener {
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                finish()
            }

            binding.homebtn.setOnClickListener {
                val intent = Intent(this, HomeUser::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                finish()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        val user_id = intent.getStringExtra("user_id")
        Log.d("Transaction", "Email: $email, User ID: $user_id")
        if (email != null && user_id != null) {
            viewModel.getCurrUser(email)
            viewModel.getUserTrans(user_id)
            Log.d("Transaction", "Email: $email, User ID: $user_id")
        }
    }
}

