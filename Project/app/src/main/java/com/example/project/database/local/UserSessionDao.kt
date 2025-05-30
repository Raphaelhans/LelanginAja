package com.example.project.database.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserSessionDao {
    @Insert
    suspend fun saveSession(user: UserSession)

    @Query("SELECT * FROM user_session LIMIT 1")
    suspend fun getSession(): UserSession?

    @Query("DELETE FROM user_session")
    suspend fun clearSession()
}