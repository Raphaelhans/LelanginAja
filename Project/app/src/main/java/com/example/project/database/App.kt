package com.example.project.database

import android.app.Application
import android.content.Context
import android.os.Build
//import com.example.project.
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.example.project.BuildConfig
import com.example.project.MidtransDao
import com.example.project.UserViewModel
import com.example.project.database.dataclass.MidtransSnap
import com.example.project.database.AppDatabase
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class App: Application() {
    companion object{
        lateinit var db: AppDatabase
        lateinit var api: MidtransDao
        val key = BuildConfig.MIDTRANS_CLIENT_KEY
        val baseUrl: String
            get() = if (isEmulator())
                "http://10.0.2.2:8000/api/"
            else
                "http://10.10.4.249:8000/api/"

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
        initMidtransSDK(this)
        initRetrofit()
    }

    private fun initMidtransSDK(context: Context) {
        Log.d("Midtrans", "Initializing Midtrans SDK")
        try {
            SdkUIFlowBuilder.init()
                .setClientKey(key)
                .setContext(context)
                .setTransactionFinishedCallback {  }
                .setMerchantBaseUrl(baseUrl)
                .enableLog(true)
                .buildSDK()

            Log.d("Midtrans", "Midtrans SDK initialized successfully")
        } catch (e: Exception) {
            Log.e("Midtrans", "Failed to initialize Midtrans SDK: ${e.message}", e)
        }
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(MidtransDao::class.java)
    }
}