package com.example.project

import java.time.LocalDateTime

data class HistoryItem (
    val id: String,
    val type: String,
    val name: String,
    val amount: Int,
    val statusOrBank: String,
    val info: String,
    val date: String,
)