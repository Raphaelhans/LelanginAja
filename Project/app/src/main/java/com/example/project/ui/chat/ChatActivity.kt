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

    private var currentUserId: Int = 0
    private var friendUserId: Int = 0
    private var productId: String = ""
    private var productName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getStringExtra("current_userId")?.toIntOrNull() ?: 0
        friendUserId = intent.getStringExtra("FRIEND_USER_ID")?.toIntOrNull() ?: 0
        productId = intent.getStringExtra("auction_item") ?: ""
        productName = intent.getStringExtra("item_name") ?: "Chat"

        binding.chatProductName.text = productName

        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        chatAdapter = ChatAdapter(currentUserId, friendUserId)

        binding.chatRV.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        setupObservers()
        setupClickListeners()

        loadChatMessages()
        viewModel.setupChatListener(currentUserId, friendUserId, productId)
    }

    private fun setupObservers() {
        viewModel.chatList.observe(this) { chatList ->
            chatAdapter.submitList(chatList.toMutableList())
            if (chatList.isNotEmpty()) {
                binding.chatRV.smoothScrollToPosition(chatList.size - 1)
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.messageStatus.observe(this) { status ->
            Log.d("ChatActivity", "Message status: $status")
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.sendButton.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etMessage.text.clear()
            }
        }
    }

    private fun loadChatMessages() {
        viewModel.getChatWith(currentUserId, friendUserId, productId)
    }

    private fun sendMessage(message: String) {
        viewModel.sendMessageTo(currentUserId, friendUserId, message, productId)
    }
}

