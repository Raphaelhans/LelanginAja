package com.example.project.database.dataclass

data class Payment (
    val transaction_id: String,
    val amount: Int,
    val payment_type: String,
    val transaction_status: String,
    val emailUser: String
)
