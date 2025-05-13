package com.example.project.database

import android.app.Application
import android.os.Build
//import com.example.project.
import android.util.Log
import android.widget.Toast
import com.example.project.BuildConfig
import com.example.project.MidtransDao
import com.example.project.database.dataclass.MidtransSnap
import com.example.project.database.local.AppDatabase
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class App: Application() {
    companion object{
        lateinit var db: AppDatabase
        val key = BuildConfig.MIDTRANS_CLIENT_KEY
        private val baseUrl: String
            get() = if (isEmulator())
                "http://10.0.2.2:8000/api/"
            else
                "http://192.168.0.101:8000/api/"

        private val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(MidtransDao::class.java)


        fun isEmulator(): Boolean {
            return Build.FINGERPRINT.contains("generic") ||
                    Build.MODEL.contains("Emulator") ||
                    Build.BRAND.startsWith("generic") ||
                    Build.MANUFACTURER.contains("Genymotion") ||
                    Build.HARDWARE.contains("goldfish") ||
                    Build.HARDWARE.contains("ranchu") ||
                    Build.PRODUCT.contains("sdk") ||
                    Build.PRODUCT.contains("emulator")
        }
    }
    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(baseContext)
        SdkUIFlowBuilder.init()
            .setClientKey(key)
            .setContext(this)
            .setTransactionFinishedCallback {
                Toast.makeText(this, "Transaction Finished", Toast.LENGTH_SHORT).show()
            }
            .setMerchantBaseUrl(baseUrl)
            .enableLog(true)
            .buildSDK()
    }


}