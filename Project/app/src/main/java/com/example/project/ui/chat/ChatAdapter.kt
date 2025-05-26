package com.example.project

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ItemChatBinding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class ChatAdapter(private val currentUserId: String) :
    ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_chat,
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position), currentUserId)
    }

    class ChatViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat, currentUserId: String) {
            binding.setVariable(BR.chat, chat)

            val chatContainer = binding.root.findViewById<ConstraintLayout>(R.id.chatContainer)
            val layoutParams = chatContainer.layoutParams as FrameLayout.LayoutParams

            if (chat.isFromCurrentUser(currentUserId)) {
                layoutParams.gravity = Gravity.END  //supaya di kanan
                binding.root.findViewById<TextView>(R.id.txtChat)?.setBackgroundResource(R.drawable.bubble_right)
                binding.root.findViewById<TextView>(R.id.txtSenderId)?.text = "You"
            } else {
                // Pesan dari seller/orang lain (kiri)
                layoutParams.gravity = Gravity.START
                binding.root.findViewById<TextView>(R.id.txtChat)?.setBackgroundResource(R.drawable.bubble_left)
                binding.root.findViewById<TextView>(R.id.txtSenderId)?.text = "Seller ID: ${chat.id_seller}"
            }

            // Format dan set timestamp
            binding.root.findViewById<TextView>(R.id.txtTimestamp)?.text = chat.getFormattedTimestamp()

            chatContainer.layoutParams = layoutParams
            binding.executePendingBindings()
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem.id_chat == newItem.id_chat
        }

        override fun areContentsTheSame(oldItem: Chat, newItem: Chat): Boolean {
            return oldItem == newItem
        }
    }
}