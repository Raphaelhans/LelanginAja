package com.example.project.database.dataclass

data class Users(
    val user_id: Int = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val balance: Int = 0,
    val status: Int = 0,
    val location: String = "",
    val profilePicturePath: String = "",
    val pin: String = "",
    val suspended: Boolean = false
)