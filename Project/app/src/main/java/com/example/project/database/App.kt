package com.example.project.database

import android.app.Application
import android.util.Log
import com.example.project.database.local.AppDatabase
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.midtrans.sdk.uikit.SdkUIFlowBuilder

class App: Application() {
    companion object{
        lateinit var db: AppDatabase
    }
    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(baseContext)
        SdkUIFlowBuilder.init()
            .setContext(this)
            .setClientKey("Mid-client-fy6t4tL63hGwcx0n")
            .setTransactionFinishedCallback {
                Log.d("MIDTRANS", "Transaction finished: $it")
            }
            .setMerchantBaseUrl("https://example.com/")
            .enableLog(true)
            .buildSDK()
    }
}