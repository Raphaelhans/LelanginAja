package com.example.project

import android.R
import android.util.Log
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.database.App
import com.example.project.database.dataclass.BankAccount
import com.example.project.database.dataclass.Categories
import com.example.project.database.dataclass.CustomerDetails
import com.example.project.database.dataclass.ItemDetails
import com.example.project.database.dataclass.MidtransPayload
import com.example.project.database.dataclass.MidtransResponse
import com.example.project.database.dataclass.Payment
import com.example.project.database.dataclass.Products
import com.example.project.database.dataclass.TransactionDetails
import org.mindrot.jbcrypt.BCrypt
import com.example.project.database.dataclass.Users
import com.example.project.database.dataclass.Withdraws
import com.example.project.database.local.Item
import com.example.project.ui.auction.AuctionItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
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
import okhttp3.internal.wait
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserViewModel:ViewModel() {
    private val db = Firebase.firestore
    private val client = OkHttpClient()

    private val _currUser = MutableLiveData<Users?>()
    val currUser: LiveData<Users?> get() = _currUser

    private val _userBankAccount = MutableLiveData<List<BankAccount>>()
    val userBankAccount: MutableLiveData<List<BankAccount>> get() = _userBankAccount

    private val _resresponse = MutableLiveData<String>()
    val resresponse: LiveData<String> get() = _resresponse

    private val _snapRedirectToken = MutableLiveData<String>()
    val snapRedirectToken: LiveData<String> get() = _snapRedirectToken

    private val _withdrawResult = MutableLiveData<String>()
    val withdrawResult: LiveData<String> get() = _withdrawResult

    private val _orderID = MutableLiveData<String>()
    val orderID: LiveData<String> get() = _orderID

    private val _amountTopup = MutableLiveData<Int>()
    val amountTopup: LiveData<Int> get() = _amountTopup

    private val _categories = MutableLiveData<List<Categories>>()
    val categories: LiveData<List<Categories>> = _categories

    private val _Items = MutableLiveData<List<Products>>()
    val Items: LiveData<List<Products>> = _Items


//private val _allItems = mutableMapOf<String, List<Products>>()  // key = categoryId
//    private val _filteredItems = MutableLiveData<List<Products>>(emptyList())
//    val filteredItems: LiveData<List<Products>> = _filteredItems



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
                    getUserAccount()
                } else {
                    Log.d("Firestore", "No user found with email: $email")
                }
            } catch (e: Exception) {
                Log.e("Firestore Error", "Error fetching user: ${e.message}", e)
            }
        }
    }


//    fun setItemsForCategory(categoryId: String, items: List<Products>) {
//    _allItems[categoryId] = items
//    _filteredItems.value = items
//    }
//
//
//    fun filterItems(query: String, categoryId: String) {
//        val all = _allItems[categoryId] ?: emptyList()
//        val filtered = all.filter { it.name.contains(query, ignoreCase = true) }
//        _filteredItems.value = filtered
//    }




    suspend fun loadCategories(): List<Categories> {
        return try {
            val snapshot = db.collection("Categories").get().await()
            val accList = snapshot.documents.mapNotNull { it.toObject(Categories::class.java) }
            _categories.postValue(accList)
            accList
        } catch (e: Exception) {
            _resresponse.postValue("Error loading categories: ${e.message}")
            Log.e("Firestore Error", "Error loading categories: ${e.message}", e)
            emptyList()
        }
    }


    suspend fun loadItemsForCategory(category: String): List<Products> {
        return try {
            val result = db.collection("Products").get().await()
            val final = result.documents.mapNotNull {
                it.toObject(Products::class.java)
            }.filter {
                it.category_id == category
            }
            _Items.postValue(final)
            final
        } catch (e: Exception) {
            Log.e("Firestore", "Failed loading items: ${e.message}")
            emptyList()
        }
    }

    fun addItems(name:String, description:String, city:String, address:String, start_bid:Int, start_date:String, end_date:String, category: String, image_url:Uri, ContentResolver:ContentResolver){
        viewModelScope.launch {
            try {
                val collectionRef = db.collection("Products").document().id

                val imgUrl = uploadImageToStorage(image_url, ContentResolver, "")

                val item = Products(collectionRef, category, currUser.value?.user_id!!, 0, name, description, city, address, start_date, end_date, start_bid,0, imgUrl,0)
                db.runTransaction { transaction ->
                    transaction.set(db.collection("Products").document(collectionRef), item)
                }.await()

                _resresponse.value = "Successfully added item"
            }catch (e: Exception){
                Log.e("Firestore Error", "Error adding item: ${e.message}", e)
            }
        }
    }

    fun addBankAccount(bankName: String, accountHolder: String, accountNumber: String){
        viewModelScope.launch {
            try {
                val count = (db.collection("BankAccounts")
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                    .count + 1) ?: 1

                val exists = userBankAccount.value?.any { it.accountNumber == accountNumber } == true

                if (!exists) {
                    val id = "Bank-$count"
                    val account = BankAccount(id, currUser.value?.user_id!!, bankName, accountHolder, accountNumber)

                    db.runTransaction { transaction ->
                        transaction.set(db.collection("BankAccounts").document(id), account)
                    }.await()

                    getUserAccount()
                    _resresponse.value = "Successfully added bank account"
                } else {
                    _resresponse.value = "Bank account already exists"
                }

            }catch (e: Exception){
                Log.e("Firestore Error", "Error adding bank account: ${e.message}", e)

            }
        }
    }

    fun getUserAccount(){
        viewModelScope.launch {
            try {
                val bankAcc = db.collection("BankAccounts")
                    .whereEqualTo("user_id", currUser.value?.user_id)
                    .get()
                    .await()

                if (!bankAcc.isEmpty){
                    val accList = bankAcc.documents.mapNotNull { it.toObject(BankAccount::class.java) }
                    _userBankAccount.value = accList.reversed()
                }
                else{
                    Log.d("Firestore", "No user found with user id: ${currUser.value?.user_id}")
                }

            }catch (e:Exception){
                Log.e("Firestore Error", "Error fetching user: ${e.message}", e)
            }
        }
    }

    suspend fun uploadImageToStorage(uri: Uri, contentResolver: ContentResolver, condition: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    ?: throw Exception("Unable to open input stream")

                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (inputStream != null) {
                    inputStream.close()
                }

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

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) throw Exception("Imgur upload failed: ${response.code} - ${response.message}")

                val json = JSONObject(response.body?.string() ?: throw Exception("Empty response"))
                val imageUrl = json.getJSONObject("data").getString("link")
                Log.d("Imgur", "Uploaded URL: $imageUrl")

                if (condition == "pfp") {
                    db.collection("Users").document(currUser.value?.user_id.toString())
                        .update("profilePicturePath", imageUrl).await()
                    _resresponse.postValue("Successfully updated the profile picture on $imageUrl")
                }

                getCurrUser(currUser.value?.email.toString())

                return@withContext imageUrl
            } catch (e: Exception) {
                Log.e("Imgur Error", "Upload failed: ${e.message}", e)
                _resresponse.postValue(e.message)
                throw e
            }
        }
    }


    fun editProfile(condition: String, changes:String){
        viewModelScope.launch {
            if (condition == "Name"){
                db.collection("Users").document(currUser.value?.user_id.toString()).update("name", changes)
                _resresponse.value = "Name successfully changed to $changes"
            }
            else if (condition == "Phone"){
                db.collection("Users").document(currUser.value?.user_id.toString()).update("phone", changes)
                _resresponse.value = "Phone successfully changed to $changes"
            }
            getCurrUser(currUser.value?.email.toString())
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            val checkPass = BCrypt.checkpw(oldPassword, currUser.value?.password)
            if (checkPass) {
                val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
                db.collection("Users").document(currUser.value?.user_id.toString())
                    .update("password", hashedPassword)
                _resresponse.value = "Password successfully changed"
                getCurrUser(currUser.value?.email.toString())
            }
            else{
                _resresponse.value = "Incorrect old password"
            }
        }
    }

    fun changePIN(currPIN: String, newPIN: String) {
        viewModelScope.launch {
            val checkPIN = BCrypt.checkpw(currPIN, currUser.value?.pin.toString())
            if (checkPIN){
                val hashedPIN = BCrypt.hashpw(newPIN, BCrypt.gensalt())
                db.collection("Users").document(currUser.value?.user_id.toString())
                    .update("pin", hashedPIN)
                _resresponse.value = "Successfully changed PIN"
                getCurrUser(currUser.value?.email.toString())
            }
            else{
                _resresponse.value = "Incorrect current PIN"
            }
        }
    }

    fun createPIN(newPIN: String) {
        viewModelScope.launch {
            val hashedPIN = BCrypt.hashpw(newPIN, BCrypt.gensalt())
            db.collection("Users").document(currUser.value?.user_id.toString())
                .update("pin", hashedPIN)
            _resresponse.value = "Successfully created PIN"
            getCurrUser(currUser.value?.email.toString())
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

                if (user.pin.toString() != inputPin) {
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

    fun withdrawConfirmation(saldotarik:Int, pin:String, bank:String, accNumber:String, accHolder: String){
        viewModelScope.launch {
            try {
                val inputPIN = BCrypt.checkpw(pin,currUser.value?.pin)
                val count = db.collection("Withdraws")
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                    .count

                if (inputPIN){
                    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    val formattedDate = formatter.format(Date())
                    db.runTransaction { transaction ->
                         val wd = Withdraws(
                             currUser.value?.user_id!!, saldotarik, bank, accNumber, accHolder,formattedDate
                        )
                        transaction.set(db.collection("Withdraws").document("Withdraw-"+count.toString()), wd)
                    }

                    db.collection("Users").document(currUser.value?.user_id.toString()).update("balance", currUser.value?.balance?.minus(saldotarik))

                    _resresponse.value = "Withdraw successful"
                    getCurrUser(currUser.value?.email.toString())
                }
                else{
                    _resresponse.value = "Incorrect PIN"
                }
            }catch (e: Exception) {
                _resresponse.value = "Payment failed: ${e.message}"
            }
        }
    }

    fun createMidtransTransaction(amount: Int) {
        viewModelScope.launch {
            try {
                val orderId = "ORDER-${System.currentTimeMillis()}-${UUID.randomUUID().toString().substring(0, 8)}"
                val customerName = currUser.value?.name.toString()
                val customerEmail = currUser.value?.email.toString()

                val payload = MidtransPayload(
                    TransactionDetails(orderId, amount),
                    CustomerDetails(customerName, customerEmail),
                    listOf(ItemDetails("TOPUP-${orderId.takeLast(8)}", amount, 1, "Top Up"))
                )
                val response = App.api.createTransaction(payload)

                _orderID.value = orderId
                _amountTopup.value = amount
                _snapRedirectToken.value = response.token
            } catch (e: Exception) {
                _resresponse.value = "Payment failed: ${e.message}"
                Log.e("PaymentVM", "Error: ${e.message}", e)
            }
        }
    }

    fun becomeSeller(){
        viewModelScope.launch {
            try {
                db.collection("Users").document(currUser.value?.user_id.toString()).update("status", 1)

                _resresponse.value = "Successfully become a seller"
                getCurrUser(currUser.value?.email.toString())
            }catch (e: Exception) {
                _resresponse.value = "Changes to Seller Failed: ${e.message}"
            }
        }
    }

    fun topupPayment(transactionId: String, paymentType: String, transactionStatus: String){
        viewModelScope.launch {
            try {
                val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                val formattedDate = formatter.format(Date())
                db.runTransaction { transaction ->
                    val pays = Payment(transactionId, amountTopup.value.toString().toInt(), paymentType, transactionStatus, currUser.value?.email.toString(), formattedDate)
                    transaction.set(db.collection("Payments").document(orderID.value.toString()), pays)
                }
                db.collection("Users").document(currUser.value?.user_id.toString()).update("balance", currUser.value?.balance?.plus(amountTopup.value.toString().toInt()))

                getCurrUser(currUser.value?.email.toString())
                _resresponse.value = "Payment successful"
            }catch (e: Exception) {
                _resresponse.value = "Payment failed: ${e.message}"
                Log.e("PaymentVM", "Error: ${e.message}", e)
            }
        }
    }
}