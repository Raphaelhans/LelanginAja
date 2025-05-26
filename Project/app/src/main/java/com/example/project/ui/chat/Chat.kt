package com.example.project

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import com.google.firebase.Timestamp

@JsonClass(generateAdapter = true)
@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey(autoGenerate = true) val id: String = "",
    val from: Int = 0,
    val to: Int = 0,
    val content: String = "",
    val timestamp: Timestamp? = null,
    val productId: String = ""
)
