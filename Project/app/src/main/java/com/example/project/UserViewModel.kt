package com.example.project

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.project.database.dataclass.Users
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserViewModel:ViewModel() {
    private val db = Firebase.firestore

    private val _currUser = MutableLiveData<Users?>()
    val currUser: LiveData<Users?> get() = _currUser

    private val _withdrawResult = MutableLiveData<String>()
    val withdrawResult: LiveData<String> get() = _withdrawResult

    fun getCurrUser(email: String) {
        viewModelScope.launch {
            try {
                val curruser = db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!curruser.isEmpty) {
                    val user = curruser.documents.first().toObject(Users::class.java)
                    _currUser.value = user
                } else {
                    Log.d("Firestore", "No user found with email: $email")
                }
            } catch (e: Exception) {
                Log.e("Firestore Error", "Error fetching user: ${e.message}", e)
            }
        }
    }

    fun process(email: String, inputPin: String, amount: Int){
        viewModelScope.launch {
            try {
                val userSnapshot = db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (userSnapshot.isEmpty) {
                    _withdrawResult.value = "User tidak ditemukan"
                    return@launch
                }

                val doc = userSnapshot.documents.first()
                val user = doc.toObject(Users::class.java)

                if (user == null) {
                    _withdrawResult.value = "Data user tidak valid"
                    return@launch
                }

                if (user.pin != inputPin) {
                    _withdrawResult.value = "PIN salah"
                    return@launch
                }

                if (user.balance < amount) {
                    _withdrawResult.value = "Saldo tidak mencukupi"
                    return@launch
                }

                val newBalance = user.balance - amount
                db.collection("Users").document(doc.id)
                    .update("balance", newBalance)
                    .await()

                _currUser.value = user.copy(balance = newBalance)
                _withdrawResult.value = "Withdraw berhasil. Saldo sekarang: Rp$newBalance"

            } catch (e: Exception) {
                Log.e("Withdraw", "Error: ${e.message}", e)
                _withdrawResult.value = "Gagal melakukan withdraw: ${e.message}"
            }
            }
    }
}