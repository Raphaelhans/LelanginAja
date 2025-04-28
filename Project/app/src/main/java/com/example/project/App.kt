package com.example.project

import android.app.Application

class App: Application() {
    companion object{
        lateinit var db:AppDatabase
    }
    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(baseContext)
    }
}