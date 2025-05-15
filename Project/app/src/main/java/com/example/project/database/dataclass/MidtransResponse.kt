package com.example.project.database.dataclass

data class MidtransResponse(
    val orderId: String = "",
    val transactionStatus: String = "",
    val statusMessage: String = "",
    val grossAmount: String = ""
)
