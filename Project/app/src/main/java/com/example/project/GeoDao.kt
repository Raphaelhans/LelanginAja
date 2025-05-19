package com.example.project

import com.example.project.database.OpenCageResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoDao {
    @GET("geocode/v1/json")
    suspend fun geocode(
        @Query("address") address: String,
    ): OpenCageResponse
}