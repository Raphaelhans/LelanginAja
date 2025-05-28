package com.example.project.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.project.database.local.UserSession
import com.example.project.database.local.UserSessionDao

@Database(entities = [UserSession::class], version = 1)
abstract class AppDatabase:RoomDatabase() {

    abstract fun userSessionDao():UserSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "prakm6"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}