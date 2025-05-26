package com.example.project

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _chatList = MutableLiveData<List<Chat>>()
    val chatList: LiveData<List<Chat>> = _chatList

    private val _messageStatus = MutableLiveData<String>()
    val messageStatus: LiveData<String> = _messageStatus

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun getChatWith(
        currentUserId: Int,
        friendUserId: Int,
        productId: String
    ) {
        viewModelScope.launch {
            try {
                val chatsSnapshot = db.collection("chats")
                    .document(productId)
                    .collection("messages")
                    .whereIn("from", listOf(currentUserId, friendUserId))
                    .whereIn("to", listOf(currentUserId, friendUserId))
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .await()

                val chatMessages = chatsSnapshot.documents.mapNotNull { doc ->
                    val chat = doc.toObject(Chat::class.java)
                    if (chat != null &&
                        ((chat.from == currentUserId && chat.to == friendUserId) ||
                                (chat.from == friendUserId && chat.to == currentUserId))) {
                        chat.copy(id = doc.id)
                    } else null
                }

                _chatList.value = chatMessages

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error getting chats: ${e.message}", e)
                _error.value = "Error fetching messages: ${e.message}"
            }
        }
    }

    fun sendMessageTo(
        currentUserId: Int,
        friendUserId: Int,
        message: String,
        productId: String
    ) {
        viewModelScope.launch {
            try {
                val chatMessage = Chat(
                    from = currentUserId,
                    to = friendUserId,
                    content = message,
                    timestamp = Timestamp.now(),
                    productId = productId
                )

                db.collection("chats")
                    .document(productId)
                    .collection("messages")
                    .add(chatMessage)
                    .await()

                _messageStatus.value = "Message sent successfully"

                getChatWith(currentUserId, friendUserId, productId)

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message: ${e.message}", e)
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }

    fun setupChatListener(
        currentUserId: Int,
        friendUserId: Int,
        productId: String
    ) {
        db.collection("chats")
            .document(productId)
            .collection("messages")
            .whereIn("from", listOf(currentUserId, friendUserId))
            .whereIn("to", listOf(currentUserId, friendUserId))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("ChatViewModel", "Listen failed: ${e.message}")
                    _error.value = "Real-time update error: ${e.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatMessages = snapshot.documents.mapNotNull { doc ->
                        val chat = doc.toObject(Chat::class.java)
                        if (chat != null &&
                            ((chat.from == currentUserId && chat.to == friendUserId) ||
                                    (chat.from == friendUserId && chat.to == currentUserId))) {
                            chat.copy(id = doc.id)
                        } else null
                    }

                    _chatList.value = chatMessages
                }
            }
    }
}
