package com.example.project

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ItemChatBinding
import androidx.databinding.DataBindingUtil

class ChatAdapter(
    private val currentUserId: Int,
    private val friendUserId: Int
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<Chat>()

    fun submitList(newList: MutableList<Chat>) {
        messages.clear()
        messages.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.txtChat.text = chat.content

            val params = binding.chatContainer.layoutParams as ViewGroup.MarginLayoutParams
            val context = binding.root.context

            if (chat.from == currentUserId) {
                binding.chatContainer.setBackgroundResource(R.drawable.bubble_left)
                binding.txtChat.setTextColor(context.getColor(android.R.color.white))
                params.setMargins(100, 0, 0, 10)
            } else {
                binding.chatContainer.setBackgroundResource(R.drawable.bubble_left)
                binding.txtChat.setTextColor(context.getColor(android.R.color.black))
                params.setMargins(0, 0, 100, 10)
            }

            binding.chatContainer.layoutParams = params
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = DataBindingUtil.inflate<ItemChatBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_chat,
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size
}