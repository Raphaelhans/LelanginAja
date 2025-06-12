package com.example.project.database.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSession(
    @PrimaryKey val userId: String,
    val email: String,
    val name: String,
    val role: Int,
)

