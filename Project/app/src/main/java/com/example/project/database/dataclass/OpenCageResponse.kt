package com.example.project.database.dataclass

data class OpenCageResponse(
    val results: List<OpenCageResult>
)

data class OpenCageResult(
    val geometry: Geometry,
    val formatted: String
)

data class Geometry(
    val lat: Double,
    val lng: Double
)
