package com.example.project.database.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ItemDao {
    @Insert
    suspend fun insert(item: Item)

    @Query("SELECT * FROM items")
    suspend fun getItems(): List<Item>

    @Query("SELECT * FROM items WHERE seller = :username")
    suspend fun getUserByEmail(username: String): List<Item>
}