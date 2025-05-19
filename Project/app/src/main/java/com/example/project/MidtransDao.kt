package com.example.project

import androidx.room.Dao
import com.example.project.database.dataclass.MidtransPayload
import com.example.project.database.dataclass.MidtransResponse
import com.example.project.database.dataclass.MidtransSnap
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

@Dao
interface MidtransDao {
    @POST("charge")
    suspend fun createTransaction(@Body payload: MidtransPayload): MidtransSnap

    @GET("check-status/{orderId}")
    suspend fun checkStatus(@Path("orderId") orderId: String): MidtransResponse
}


