package com.example.project.database.dataclass

data class MidtransPayload(
    val transaction_details: TransactionDetails,
    val customer_details: CustomerDetails,
    val item_details: List<ItemDetails>
)

data class TransactionDetails(
    val order_id: String,
    val gross_amount: Int,
    val currency: String = "IDR"
)

data class CustomerDetails(
    val first_name: String,
    val email: String
)

data class ItemDetails(
    val id: String,
    val price: Int,
    val quantity: Int,
    val name: String
)
