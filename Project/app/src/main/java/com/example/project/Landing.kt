package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.project.database.App
import com.example.project.databinding.Landing1Binding
import com.example.project.databinding.Landing2Binding
import kotlinx.coroutines.launch

class LandingActivity : AppCompatActivity() {
    private lateinit var binding1: Landing1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding1 = Landing1Binding.inflate(layoutInflater)
        setContentView(binding1.root)

        binding1.btnNext1.setOnClickListener {
            val intent = Intent(this, Landing2Activity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

class Landing2Activity : AppCompatActivity() {
    private lateinit var binding2: Landing2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding2 = Landing2Binding.inflate(layoutInflater)
        setContentView(binding2.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.memory)
            .preload()

        val layout = layoutInflater.inflate(R.layout.custom_toast, null)
        val toastText = layout.findViewById<TextView>(R.id.toast_text)
        val toastImage = layout.findViewById<ImageView>(R.id.toastimage)

        Glide.with(this)
            .asGif()
            .load(R.drawable.loadingani)
            .preload()

        binding2.btnNext2.setOnClickListener {
            binding2.btnNext2.visibility = View.GONE

            Glide.with(this)
                .asGif()
                .load(R.drawable.loadingani)
                .into(binding2.loadingGIf)
            binding2.loadingGIf.visibility = View.VISIBLE

            lifecycleScope.launch {
                val session = App.db.userSessionDao().getSession()
                toastText.text = "We Remember You"
                if (session != null) {
                    val intent = Intent(this@Landing2Activity, HomeUser::class.java)
                    intent.putExtra("email", session.email)
                    Glide.with(this@Landing2Activity)
                        .asGif()
                        .load(R.drawable.memory)
                        .into(toastImage)
                    with (Toast(applicationContext)) {
                        duration = Toast.LENGTH_LONG
                        view = layout
                        show()
                    }
                    intent?.let {
                        startActivity(it)
                        finish()
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@Landing2Activity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }
    }
}
