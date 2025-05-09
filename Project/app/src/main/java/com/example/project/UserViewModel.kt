package com.example.project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.content.ContentResolver
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.project.database.dataclass.Users
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

class UserViewModel:ViewModel() {
    private val db = Firebase.firestore
    private val client = OkHttpClient()

    private val _currUser = MutableLiveData<Users?>()
    val currUser: LiveData<Users?> get() = _currUser
    private val storage = FirebaseStorage.getInstance()

    private val _resresponse = MutableLiveData<String>()
    val resresponse: LiveData<String> get() = _resresponse

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

    fun uploadImageToStorage(path:String) {
        viewModelScope.launch {
            try {
                val userId = currUser.value?.user_id.toString()
                db.collection("Users").document(userId).update("profilePicturePath", path).await()

                _resresponse.value = "Success changing pfp to "+path
            } catch (e: Exception) {
                _resresponse.value = e.message
            }
        }
    }

    fun uploadImageToStorage(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                if (inputStream == null) throw Exception("Unable to open input stream")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap == null) throw Exception("Failed to decode image from Uri")

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                val imageBytes = baos.toByteArray()

                val clientId = "1e4cd04cd3b4bce"
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "profile_${currUser.value?.user_id}.jpg",
                        okhttp3.RequestBody.create("image/jpeg".toMediaType(), imageBytes))
                    .build()

                val request = Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .header("Authorization", "Client-ID $clientId")
                    .post(requestBody)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (!response.isSuccessful) throw Exception("Imgur upload failed: ${response.code} - ${response.message}")

                val json = JSONObject(response.body?.string() ?: throw Exception("Empty response"))
                val imageUrl = json.getJSONObject("data").getString("link")
                Log.d("Imgur", "Uploaded URL: $imageUrl")

                db.collection("Users").document(currUser.value?.user_id.toString()).update("profilePicturePath", imageUrl).await()

                _resresponse.value = "Successfully updated the profile picture on " + imageUrl
                getCurrUser(currUser.value?.email.toString())
            } catch (e: Exception) {
                Log.e("Imgur Error", "Upload failed: ${e.message}", e)
                _resresponse.value = e.message
            }
        }
    }

    fun editProfile(condition: String, changes:String){
        viewModelScope.launch {
            if (condition == "Name"){
                db.collection("Users").document(currUser.value?.user_id.toString()).update("name", changes)
            }
            else if (condition == "Phone"){
                db.collection("Users").document(currUser.value?.user_id.toString()).update("phone", changes)
            }
            else{
                db.collection("Users").document(currUser.value?.user_id.toString()).update("password", changes)
            }
            getCurrUser(currUser.value?.email.toString())
        }
    }

}