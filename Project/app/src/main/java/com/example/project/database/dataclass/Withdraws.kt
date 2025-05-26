package com.example.project.database.dataclass

data class Withdraws(
    val user_id: Int = 0,
    val amountWd:Int = 0,
    val bank:String = "",
    val accHolder:String = "",
    val accNumber:String = "",
    val date:String = ""
)
