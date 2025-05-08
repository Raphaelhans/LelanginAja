package com.example.project

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.project.database.local.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUser(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?
}