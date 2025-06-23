package com.example.project

import android.util.Log
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.database.App
import com.example.project.database.TransactionRepository
import com.example.project.database.dataclass.BankAccount
import com.example.project.database.dataclass.Categories
import com.example.project.database.dataclass.CustomerDetails
import com.example.project.database.dataclass.DisplayItem
import com.example.project.database.dataclass.ItemDetails
import com.example.project.database.dataclass.MidtransPayload
import com.example.project.database.dataclass.Payment
import com.example.project.database.dataclass.Products
import com.example.project.database.dataclass.Ratings
import com.example.project.database.dataclass.TransactionDetails
import com.example.project.database.dataclass.Transactions
import com.example.project.database.dataclass.TransactionwithProduct
import org.mindrot.jbcrypt.BCrypt
import com.example.project.database.dataclass.Users
import com.example.project.database.dataclass.Withdraws
import com.example.project.database.local.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserViewModel(): ViewModel() {
    private val db = Firebase.firestore
    private val client = OkHttpClient()
    private val transactionRepository = TransactionRepository(db)

    private val _currUser = MutableLiveData<Users?>()
    val currUser: LiveData<Users?> get() = _currUser

    private val _currUserTransaction = MutableLiveData<List<TransactionwithProduct>>()
    val currUserTransaction: LiveData<List<TransactionwithProduct>> get() = _currUserTransaction

    private val _currSeller = MutableLiveData<Users?>()
    val currSeller: LiveData<Users?> get() = _currSeller

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

    private val _currCategories = MutableLiveData<Categories?>()
    val currCategories: LiveData<Categories?> = _currCategories

    private val _Items = MutableLiveData<List<Products>>()
    val Items: LiveData<List<Products>> = _Items

    private val _currItems = MutableLiveData<Products?>()
    val currItems: LiveData<Products?> = _currItems

    private val _currRating = MutableLiveData<Ratings?>()
    val currRating: LiveData<Ratings?> = _currRating

    private val _currRatingSeller = MutableLiveData<Ratings?>()
    val currRatingSeller: LiveData<Ratings?> = _currRatingSeller

    private val _search = MutableLiveData<String>()
    val searchBrg: LiveData<String> = _search

    private val _combinedTransactionHistory = MutableLiveData<List<DisplayItem>>(emptyList())
    val combinedTransactionHistory: LiveData<List<DisplayItem>> = _combinedTransactionHistory

    fun setSearchBrg(query: String) {
        _search.value = query
    }

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun getCurrUser(email: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val curruser = db.collection("Users")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!curruser.isEmpty) {
                    val user = curruser.documents.first().toObject(Users::class.java)
                    val sellerId = user?.user_id

                    val allRatingsSnapshot = db.collection("Ratings").get().await()
                    val allRatings =
                        allRatingsSnapshot.documents.mapNotNull { it.toObject(Ratings::class.java) }
                    val globalAvg = allRatings.map { it.rating }.average()

                    val sellerRatings = allRatings.filter { it.seller_id == sellerId }

                    val bayesian = calculateBayesianRating(sellerRatings, globalAvg)

                    _currRating.value = Ratings(
                        rating_id = "",
                        seller_id = 0,
                        rating = bayesian.toDouble()
                    )

                    _currUser.value = user
                    getUserAccount()
                } else {
                    Log.d("Firestore", "No user found with email: $email")
                }
            } catch (e: Exception) {
                Log.e("Firestore Error", "Error fetching user: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getCurrSeller(user_id: String) {
        viewModelScope.launch {
            try {
                val curruser = db.collection("Users")
                    .whereEqualTo("user_id", user_id.toInt())
                    .get()
                    .await()

                if (!curruser.isEmpty) {
                    val user = curruser.documents.first().toObject(Users::class.java)
                    val currRate =
                        db.collection("Ratings").whereEqualTo("seller_id", user?.user_id).get()
                            .await()
                    val rate = currRate.documents.mapNotNull { it.toObject(Ratings::class.java) }
                    _currRatingSeller.value = rate.firstOrNull()
                    _currSeller.value = user
                    getUserAccount()
                } else {
                    Log.d("Firestore", "No user found with ID: $user_id")
                }
            } catch (e: Exception) {
                Log.e("Firestore Error", "Error fetching user: ${e.message}", e)
            }
        }
    }

    fun getCurrentUserId(): String {
        return _currUser.value?.user_id?.toString() ?: "0"
    }

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

    fun getCurrItem(itemIds: String) {
        viewModelScope.launch {
            try {
                val currItem = db.collection("Products").document(itemIds).get().await()
                val item = currItem.toObject(Products::class.java)
                val currcate =
                    db.collection("Categories").document(item?.category_id!!).get().await()
                val cate = currcate.toObject(Categories::class.java)
                Log.d("Firestore", "Current item: $item")
                _currCategories.value = cate
                _currItems.value = item
            } catch (e: Exception) {
                Log.e("Firestore Error", "Error fetching item: ${e.message}", e)
            }
        }
    }

    fun getCategoryById(categoryId: String) {
        db.collection("Categories")
            .document(categoryId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val category = doc.toObject(Categories::class.java)
                    _currCategories.postValue(category)
                } else {
                    _currCategories.postValue(null)
                }
            }
            .addOnFailureListener {
                _currCategories.postValue(null)
            }
    }

    fun getAverageRating(sellerId: Int) {
        db.collection("Ratings")
            .whereEqualTo("seller_id", sellerId)
            .get()
            .addOnSuccessListener { result ->
                val ratings = result.mapNotNull { it.getDouble("rating") }
                if (ratings.isNotEmpty()) {
                    val average = ratings.average()
                    _currRating.postValue(Ratings(seller_id = sellerId, rating = average))
                } else {
                    _currRating.postValue(null)
                }
            }
            .addOnFailureListener {
                _currRating.postValue(null)
            }
    }

    fun submitProductRating(
        itemId: String,
        transactionId: String,
        sellerId: Int,
        ratingValue: Double,
        reviewText: String = ""
    ) {
        viewModelScope.launch {
            try {
                val currentBuyerId = currUser.value?.user_id

                if (currentBuyerId == null || currentBuyerId == 0) {
                    _resresponse.postValue("Gagal memberi rating: Pengguna belum login.")
                    Log.e("Rating", "User not logged in or invalid ID.")
                    return@launch
                }
                if (itemId.isBlank() || transactionId.isBlank() || sellerId == 0 || ratingValue !in 1.0..5.0) {
                    _resresponse.postValue("Gagal memberi rating: Data tidak lengkap atau tidak valid.")
                    Log.e("Rating", "Incomplete or invalid rating data provided.")
                    return@launch
                }

                val transactionQuery = db.collection("Transaksi")
                    .whereEqualTo("transaksiId", transactionId)
                    .whereEqualTo("buyer_id", currentBuyerId)
                    .whereEqualTo("produk_id", itemId)
                    .whereEqualTo("status", "complete")
                    .limit(1)
                    .get()
                    .await()

                if (transactionQuery.isEmpty) {
                    _resresponse.postValue("Gagal memberi rating: Transaksi tidak ditemukan atau belum selesai.")
                    Log.e(
                        "Rating",
                        "Transaction $transactionId not found or not completed for user $currentBuyerId and item $itemId."
                    )
                    return@launch
                }

                val existingRatingQuery = db.collection("Ratings")
                    .whereEqualTo("transaction_id", transactionId)
                    .whereEqualTo("buyer_id", currentBuyerId)
                    .limit(1)
                    .get()
                    .await()

                if (!existingRatingQuery.isEmpty) {
                    _resresponse.postValue("Anda sudah memberikan rating untuk transaksi ini.")
                    Log.d("Rating", "sudah ada rating $transactionId by user $currentBuyerId.")
                    return@launch
                }

                val newRatingId = db.collection("Ratings").document().id
                val ratingData = Ratings(
                    rating_id = newRatingId,
                    seller_id = sellerId,
                    buyer_id = currentBuyerId,
                    item_id = itemId,
                    transaction_id = transactionId,
                    rating = ratingValue,
                    review = reviewText,
                    date = SimpleDateFormat("dd MMMMyyyy, HH:mm", Locale("id", "ID")).format(Date())
                )

                db.collection("Ratings").document(newRatingId).set(ratingData).await()

                _resresponse.postValue("Rating berhasil dikirim!")
                Log.d("Rating", "Rating submitted: $ratingData")

                getAverageRating(sellerId)
            } catch (e: Exception) {
                _resresponse.postValue("Gagal memberi rating: ${e.message}")
                Log.e("Rating Error", "Error submitting rating: ${e.message}", e)
            }
        }
    }

    fun getUserTrans(userid: String) {
        viewModelScope.launch {
            try {
                val rawTrans = db.collection("Transaksi")
                    .whereEqualTo("buyer_id", userid)
                    .get().await()

                val transactions = rawTrans.documents.mapNotNull {
                    it.toObject(Transactions::class.java)
                }
                Log.d("Firestore", "Loaded transactions: $transactions")

                val transactionWithProductList = transactions.map { transaction ->
                    val productSnapshot = db.collection("Products")
                        .document(transaction.produk_id)
                        .get().await()

                    val product = productSnapshot.toObject(Products::class.java)

                    TransactionwithProduct(transaction, product)
                }
                Log.d("Firestore", "Transaction with product list: $transactionWithProductList")

                _currUserTransaction.value = transactionWithProductList
            } catch (e: Exception) {
                Log.e("Firestore Error", "Failed to load transactions: ${e.message}", e)
                _currUserTransaction.value = emptyList()
            }
        }
    }

    suspend fun checkItemValidity() {
        val formatter = DateTimeFormatter.ofPattern("dd MMMMyyyy | HH:mm", Locale.ENGLISH)
        try {
            val now = LocalDateTime.now()
            val productSnapshot = db.collection("Products").get().await()
            for (doc in productSnapshot.documents) {
                val product = doc.toObject(Products::class.java) ?: continue

                val startDate = LocalDateTime.parse(product.start_date, formatter)
                val endDate = LocalDateTime.parse(product.end_date, formatter)

                val newStatus = when {
                    now.isAfter(endDate) -> 1
                    now.isBefore(startDate) -> 2
                    now.isAfter(startDate) && now.isBefore(endDate) -> 0
                    else -> product.status
                }

                if (newStatus != product.status) {
                    db.collection("Products").document(doc.id).update("status", newStatus).await()
                }
            }

        } catch (e: Exception) {
            Log.e("Firestore Error", "Error updating expired items: ${e.message}", e)
        }
    }

    suspend fun loadItemsForCategory(category: String): List<Products> {
        return try {
            val result = db.collection("Products").get().await()
            val final = result.documents.mapNotNull {
                it.toObject(Products::class.java)
            }.filter {
                it.category_id == category &&
                        it.status == 0
            }
            _Items.postValue(final)
            final
        } catch (e: Exception) {
            Log.e("Firestore", "Failed loading items: ${e.message}")
            emptyList()
        }
    }

    fun addItems(
        name: String,
        description: String,
        city: String,
        address: String,
        start_bid: Int,
        start_date: String,
        end_date: String,
        category: String,
        image_url: Uri,
        ContentResolver: ContentResolver
    ) {
        viewModelScope.launch {
            try {
                val collectionRef = db.collection("Products").document().id

                val imgUrl = uploadImageToStorage(image_url, ContentResolver, "")

                val item = Products(
                    collectionRef,
                    category,
                    currUser.value?.user_id!!,
                    0,
                    name,
                    description,
                    city,
                    address,
                    start_date,
                    end_date,
                    start_bid,
                    0,
                    imgUrl,
                    0
                )
                db.runTransaction { transaction ->
                    transaction.set(db.collection("Products").document(collectionRef), item)
                }.await()

                _resresponse.value = "Successfully added item"
            } catch (e: Exception) {
                Log.e("Firestore Error", "Error adding item: ${e.message}", e)
            }
        }
    }

    fun addBankAccount(bankName: String, accountHolder: String, accountNumber: String) {
        viewModelScope.launch {
            try {
                val count = (db.collection("BankAccounts")
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                    .count + 1) ?: 1

                val exists =
                    userBankAccount.value?.any { it.accountNumber == accountNumber } == true

                if (!exists) {
                    val id = "Bank-$count"
                    val account = BankAccount(
                        id,
                        currUser.value?.user_id!!,
                        bankName,
                        accountHolder,
                        accountNumber
                    )

                    db.runTransaction { transaction ->
                        transaction.set(db.collection("BankAccounts").document(id), account)
                    }.await()

                    getUserAccount()
                    _resresponse.value = "Successfully added bank account"
                } else {
                    _resresponse.value = "Bank account already exists"
                }

            } catch (e: Exception) {
                Log.e("Firestore Error", "Error adding bank account: ${e.message}", e)

            }
        }
    }

    fun getUserAccount() {
        viewModelScope.launch {
            try {
                val bankAcc = db.collection("BankAccounts")
                    .whereEqualTo("user_id", currUser.value?.user_id)
                    .get()
                    .await()

                if (!bankAcc.isEmpty) {
                    val accList =
                        bankAcc.documents.mapNotNull { it.toObject(BankAccount::class.java) }
                    _userBankAccount.value = accList.reversed()
                } else {
                    Log.d("Firestore", "No user found with user id: ${currUser.value?.user_id}")
                }

            } catch (e: Exception) {
                Log.e("Firestore Error", "Error fetching user: ${e.message}", e)
            }
        }
    }

    suspend fun uploadImageToStorage(
        uri: Uri,
        contentResolver: ContentResolver,
        condition: String
    ): String {
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
                    .addFormDataPart(
                        "image", "profile_${currUser.value?.user_id}.jpg",
                        okhttp3.RequestBody.create("image/jpeg".toMediaType(), imageBytes)
                    )
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


    fun editProfile(condition: String, changes: String) {
        viewModelScope.launch {
            if (condition == "Name") {
                db.collection("Users").document(currUser.value?.user_id.toString())
                    .update("name", changes)
                _resresponse.value = "Name successfully changed to $changes"
            } else if (condition == "Phone") {
                db.collection("Users").document(currUser.value?.user_id.toString())
                    .update("phone", changes)
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
            } else {
                _resresponse.value = "Incorrect old password"
            }
        }
    }

    fun changePIN(currPIN: String, newPIN: String) {
        viewModelScope.launch {
            val checkPIN = BCrypt.checkpw(currPIN, currUser.value?.pin.toString())
            if (checkPIN) {
                val hashedPIN = BCrypt.hashpw(newPIN, BCrypt.gensalt())
                db.collection("Users").document(currUser.value?.user_id.toString())
                    .update("pin", hashedPIN)
                _resresponse.value = "Successfully changed PIN"
                getCurrUser(currUser.value?.email.toString())
            } else {
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

    fun process(email: String, inputPin: String, amount: Int) {
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

    fun logout() {
        viewModelScope.launch {
            try {
                _currUser.value = null
                _resresponse.value = ""
                _snapRedirectToken.value = ""
                _withdrawResult.value = ""
                _userBankAccount.value = emptyList()
//                App.db.userSessionDao().clearSession()
            } catch (e: Exception) {
                Log.e("Logout", "Error: ${e.message}", e)
            }
        }
    }

    fun withdrawConfirmation(
        saldotarik: Int,
        pin: String,
        bank: String,
        accNumber: String,
        accHolder: String
    ) {
        viewModelScope.launch {
            try {
                val inputPIN = BCrypt.checkpw(pin, currUser.value?.pin)
                val count = db.collection("Withdraws")
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
                    .count

                if (inputPIN) {
                    val formatter = SimpleDateFormat("dd MMMMyyyy, HH:mm", Locale("id", "ID"))
                    val formattedDate = formatter.format(Date())
                    val newWdId = db.collection("Withdraws").document()
                    db.runTransaction { transaction ->
                        val wd = Withdraws(
                            newWdId.id,
                            currUser.value?.user_id!!,
                            saldotarik,
                            bank,
                            accNumber,
                            accHolder,
                            formattedDate
                        )
                        transaction.set(
                            db.collection("Withdraws").document("Withdraw-" + count.toString()), wd
                        )
                    }

                    db.collection("Users").document(currUser.value?.user_id.toString())
                        .update("balance", currUser.value?.balance?.minus(saldotarik))

                    _resresponse.value = "Withdraw successful"
                    getCurrUser(currUser.value?.email.toString())
                } else {
                    _resresponse.value = "Incorrect PIN"
                }
            } catch (e: Exception) {
                _resresponse.value = "Payment failed: ${e.message}"
            }
        }
    }

    fun createMidtransTransaction(amount: Int) {
        viewModelScope.launch {
            try {
                val orderId = "ORDER-${System.currentTimeMillis()}-${
                    UUID.randomUUID().toString().substring(0, 8)
                }"
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

    fun becomeSeller() {
        viewModelScope.launch {
            try {
                db.collection("Users").document(currUser.value?.user_id.toString())
                    .update("status", 1)

                _resresponse.value = "Successfully become a seller"
                getCurrUser(currUser.value?.email.toString())
            } catch (e: Exception) {
                _resresponse.value = "Changes to Seller Failed: ${e.message}"
            }
        }
    }

    fun topupPayment(transactionId: String, paymentType: String, transactionStatus: String) {
        viewModelScope.launch {
            try {
                val formatter = SimpleDateFormat("dd MMMMyyyy, HH:mm", Locale("id", "ID"))
                val formattedDate = formatter.format(Date())
                db.runTransaction { transaction ->
                    val pays = Payment(
                        transactionId,
                        amountTopup.value.toString().toInt(),
                        paymentType,
                        transactionStatus,
                        currUser.value?.email.toString(),
                        formattedDate
                    )
                    transaction.set(
                        db.collection("Payments").document(orderID.value.toString()),
                        pays
                    )
                }
                db.collection("Users").document(currUser.value?.user_id.toString()).update(
                    "balance",
                    currUser.value?.balance?.plus(amountTopup.value.toString().toInt())
                )

                getCurrUser(currUser.value?.email.toString())
                _resresponse.value = "Payment successful"
            } catch (e: Exception) {
                _resresponse.value = "Payment failed: ${e.message}"
                Log.e("PaymentVM", "Error: ${e.message}", e)
            }
        }
    }

    fun placingBids(produkId: String, buyerId: String, sellerId: String, bidAmount: Double) {
        viewModelScope.launch {
            try {
                val produkRef = db.collection("Products").document(produkId)
                val userRef = db.collection("Users").document(buyerId)
                Log.d("BID_DEBUG1", "produkId: $produkId, buyerId: $buyerId, sellerId: $sellerId")
                val formatter = SimpleDateFormat("dd MMMMyyyy, HH:mm", Locale("id", "ID"))
                val formattedDate = formatter.format(Date())

                val existingBidQuery = db.collection("Transaksi")
                    .whereEqualTo("produk_id", produkId)
                    .whereEqualTo("buyer_id", buyerId)
                    .limit(1)
                    .get()
                    .await()

                db.runTransaction { transaction ->
                    val produk2 = transaction.get(produkRef)
                    val user2 = transaction.get(userRef)

                    val currEndBid = produk2.getDouble("end_bid") ?: 0.0
                    val startBid = produk2.getDouble("start_bid") ?: 0.0
                    var userBalance = user2.getDouble("balance") ?: 0.0
                    Log.d(
                        "BID_DEBUG2",
                        "currEndBid: $currEndBid, startBid: $startBid, userBalance: $userBalance"
                    )

                    val highestBid = if (currEndBid == 0.0) startBid else currEndBid

                    if (bidAmount <= highestBid) {
                        throw Exception("Penawaran harus lebih tinggi dari bid saat ini")
                    }
                    if (userBalance < bidAmount) {
                        throw Exception("Saldo tidak cukup")
                    }

                    if (!existingBidQuery.isEmpty) {
                        val existingDoc = existingBidQuery.documents[0]
                        val oldBid = existingDoc.getDouble("bid") ?: 0.0

                        userBalance += oldBid

                        if (userBalance < bidAmount) {
                            throw Exception("Saldo tidak cukup setelah pengembalian bid sebelumnya")
                        }

                        userBalance -= bidAmount
                        transaction.update(
                            existingDoc.reference, mapOf(
                                "bid" to bidAmount,
                                "time_bid" to formattedDate,
                                "status" to "Pending"
                            )
                        )
                    } else {
                        userBalance -= bidAmount
                        val transaksiId = db.collection("Transaksi").document().id
                        val transaksi = hashMapOf(
                            "transaksiId" to transaksiId,
                            "produk_id" to produkId,
                            "buyer_id" to buyerId,
                            "seller_id" to produk2.getString("user_id").orEmpty(),
                            "bid" to bidAmount,
                            "time_bid" to formattedDate,
                            "status" to "Pending"
                        )
                        transaction.set(db.collection("Transaksi").document(transaksiId), transaksi)
                    }

                    transaction.update(
                        produkRef, mapOf(
                            "end_bid" to bidAmount,
                            "buyer_id" to buyerId.toInt()
                        )
                    )
                    transaction.update(userRef, "balance", userBalance)
                    getCurrItem(produkId)
                }.addOnSuccessListener {
                    _resresponse.value = "Bid berhasil dikirim"
                }.addOnFailureListener { e ->
                    _resresponse.value = "Gagal Bid: ${e.message}"
                }
            } catch (e: Exception) {
                _resresponse.value = "Gagal: ${e.message}"
            }
        }
    }

    private fun calculateBayesianRating(
        userRatings: List<Ratings>,
        globalAvg: Double,
        threshold: Int = 5
    ): Double {
        val v = userRatings.size
        val R = if (v > 0) userRatings.map { it.rating }.average() else 0.0
        val m = threshold
        val C = globalAvg

        return ((v.toDouble() / (v + m)) * R) + ((m.toDouble() / (v + m)) * C)
    }

    fun loadCombined() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val currentUserId = _currUser.value?.user_id

                if (currentUserId == null) {
                    _combinedTransactionHistory.postValue(emptyList())
                    _isLoading.postValue(false)
                    return@launch
                }

                val deferredUsers = async { transactionRepository.getAllUsersFirestore() } //
                val deferredProducts = async { transactionRepository.getAllProductsFirestore() } //
                val deferredTransactions = async { transactionRepository.getAllTransactionsFirestore() } //
                val deferredRatings = async { transactionRepository.getAllRatingsFirestore() } //

                val users = deferredUsers.await()
                val products = deferredProducts.await()
                val transactions = deferredTransactions.await()
                val ratings = deferredRatings.await()

                val userMap = users.associateBy { it.user_id }
                val productMap = products.associateBy { it.items_id }
                val ratingMap = ratings.groupBy { it.transaction_id }

                val combinedList: List<DisplayItem> = transactions
                    .filter { it.buyer_id == currentUserId && it.status == "complete" && ratingMap[it.transaksiId]?.any { rating -> rating.buyer_id == currentUserId } == true }
                    .mapNotNull { transaction ->
                        val user = userMap[transaction.buyer_id]
                        val product = productMap[transaction.produk_id]
                        val associatedRating = ratingMap[transaction.transaksiId]?.find {
                            it.buyer_id == transaction.buyer_id
                        }

                        if (user == null) {
                            Log.e(
                                "UserViewModel", "Failed load transaction user ${transaction.buyer_id} (${transaction.transaksiId})"
                            )
                            null
                        } else {
                            DisplayItem(
                                userName = user.name,
                                productName = product?.name ?: "Produk tidak ditemukan",
                                transactionDate = transaction.time_bid,
                                rating = associatedRating?.rating,
                                review = associatedRating?.review,
                                status = transaction.status,
                                itemId = transaction.produk_id,
                                transactionId = transaction.transaksiId,
                                sellerId = transaction.seller_id,
                            )
                        }
                    }
                _combinedTransactionHistory.postValue(combinedList.sortedByDescending { it.transactionDate })
            } catch (e: Exception) {
                Log.e("UserViewModel Error", "Error loading combined history: ${e.message}", e)
                _combinedTransactionHistory.postValue(emptyList())
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setCurrentUser(user: Users) {
        _currUser.value = user
    }

}