package com.example.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao: UserDao
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> get() = _registerResult
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    init {
        val db = AppDatabase.getDatabase(application)
        userDao = db.userDao()
    }

    fun register(name: String, phone: String, email: String, password: String) {
        viewModelScope.launch {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser == null) {
                val user = User(name = name, phone = phone, email = email, password = password)
                userDao.insert(user)
                _registerResult.postValue(true)
            } else {
                _registerResult.postValue(false)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = userDao.getUser(email, password)
            _loginResult.postValue(user != null)
        }
    }
}