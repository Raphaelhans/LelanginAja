package com.example.project.database.dataclass

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Staff(
    val id_staff: Int = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val status: Boolean = false,
    val suspended: Boolean = false,
    val deleted: Boolean = false
) : Parcelable