package com.example.project.database

import android.app.Application
import com.example.project.database.local.AppDatabase
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore

class App: Application() {
    companion object{
        lateinit var db: AppDatabase
    }
    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(baseContext)
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
    }
}