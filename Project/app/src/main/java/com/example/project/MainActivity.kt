package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val registerBtn = findViewById<Button>(R.id.RegisterBtn)
            registerBtn.setOnClickListener {
                val intent = Intent(this, Register::class.java)
                startActivity(intent)
            }

            val loginBtn = findViewById<Button>(R.id.LoginBtn)
            loginBtn.setOnClickListener {
                val intent = Intent(this, HomeUser::class.java)
                startActivity(intent)
                finish()
            }

            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}