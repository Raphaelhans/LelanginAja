package com.example.project.database.dataclass

data class Payment (
    val transaction_id: String = "",
    val name: String,
    val amount: Int = 0,
    val payment_type: String = "",
    val transaction_status: String = "",
    val emailUser: String = "",
    val date: String = ""
)
