package com.example.project

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@JsonClass(generateAdapter = true)
@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id_chat: String = "",
    val id_user: String = "",
    val id_barang: String = "",
    val chat: String = "",
    val time: Timestamp? = null,
    val id_seller: String = "",

    @Transient val content: String = chat
) {
    fun getFormattedTimestamp(): String {
        return time?.let {
            val date = it.toDate()
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            format.format(date)
        } ?: ""
    }

    fun getSenderId(): String {
        return id_user
    }

    fun getSellerId(): String {
        return id_seller
    }

    fun isFromCurrentUser(currentUserId: String): Boolean {
        return id_user == currentUserId
    }
}