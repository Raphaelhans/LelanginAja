package com.example.project

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
//    private val userDao: UserDao
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> get() = _registerResult
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> get() = _items
    private val _currUser = MutableLiveData<User>()
    val currUser: LiveData<User> get() = _currUser

//    init {
//        val db = AppDatabase.getDatabase(application)
//        userDao = db.userDao()
//    }

    fun register(name: String, phone: String, email: String, password: String) {
        viewModelScope.launch {
            val existingUser = App.db.userDao().getUserByEmail(email)
            if (existingUser == null) {
                val user = User(name = name, phone = phone, email = email, password = password, isSeller = false, balance = 0)
                App.db.userDao().insert(user)
                _registerResult.postValue(true)
            } else {
                _registerResult.postValue(false)
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

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = App.db.userDao().getUser(email, password)
            _loginResult.postValue(user != null)
            if (user != null) {
                _currUser.value = user
            }
            getAllItems()
            Log.d("cek", currUser.value?.name.toString())
        }
    }
}