package com.example.project

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project.database.App
import com.example.project.database.dataclass.Users
import com.example.project.database.local.Item
import com.example.project.database.local.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> get() = _items
    private val _currUser = MutableLiveData<User?>()
    val currUser: LiveData<User?> get() = _currUser
    private val _resresponse = MutableLiveData<String>()
    val resresponse: LiveData<String> get() = _resresponse

    var checkres = MutableLiveData<Boolean>(false)

    fun registerUser(name: String, phone: String, email: String, password: String, balance: Int, status: Int, lokasi: String) {
        viewModelScope.launch {
            try {
                val usernameQuery = db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!usernameQuery.isEmpty) {
                    _resresponse.value = "Email already taken"
                    return@launch
                }

                val highestIdQuery = db.collection("Users")
                    .orderBy("user_id", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val highestId = if (highestIdQuery.isEmpty) {
                    1
                } else {
                    highestIdQuery.documents.first().toObject(Users::class.java)?.user_id?.plus(1) ?: 1
                }

                val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

                db.runTransaction { transaction ->
                    val user = Users(
                        user_id = highestId,
                        name = name,
                        phone = phone,
                        email = email,
                        password = hashedPassword,
                        balance = balance,
                        status = status,
                        location = lokasi,
                        profilePicturePath = "",
                        pin = ""
                    )
                    transaction.set(db.collection("Users").document(highestId.toString()), user)
                }

                _resresponse.value = "Successfully registered"
                checkres.value = true
            } catch (e: Exception) {
                Log.d("Firestore Error", "Error adding user: ${e.message}", e)
                _resresponse.value = "Failed to add user: ${e.message}"
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val query = db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (query.isEmpty) {
                    _resresponse.value = "Invalid username"
                    return@launch
                }

                val user = query.documents.first().toObject(Users::class.java)
                val storedHash = user?.password ?: throw Exception("Invalid user data")

                val isPasswordCorrect = BCrypt.checkpw(password, storedHash)

                if (isPasswordCorrect) {
                    checkres.value = true
                } else {
                    _resresponse.value = "Incorrect password"
                    checkres.value = false
                }
            } catch (e: Exception) {
                _resresponse.value = e.message
                checkres.value = false
            }
        }
    }

    fun getcurrUser(email: String){
        viewModelScope.launch {
            val user = App.db.userDao().getUserByEmail(email)
            _currUser.value = user
            Log.d("cekbtrg", currUser.value?.name.toString())
        }
    }

    fun getAllItems(){
        viewModelScope.launch {
            val items = App.db.itemDao().getItems()
            _items.value = items
        }
    }

}