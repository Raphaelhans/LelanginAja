package com.example.project.ui.chat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.ChatAdapter
import com.example.project.ChatViewModel
import com.example.project.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter

    private var currentUserId: String = ""
    private var productId: String = ""
    private var sellerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productId = intent.getStringExtra("auction_item") ?: ""
        currentUserId = intent.getStringExtra("current_userId") ?: ""
        sellerId = intent.getStringExtra("sellerId") ?: ""
        val itemName = intent.getStringExtra("item_name") ?: ""

        chatAdapter = ChatAdapter(currentUserId)
        binding.chatRV.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }

        viewModel.chatList.observe(this) { chats ->
            chatAdapter.submitList(chats)
            if (chats.isNotEmpty()) {
                binding.chatRV.scrollToPosition(chats.size - 1)
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.setupChatListener(currentUserId, sellerId, productId)

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.sendButton.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessageTo(currentUserId, sellerId, message, productId)
                binding.etMessage.text.clear()
            }
        }

        supportActionBar?.title = "Chat - $itemName"
    }
}

