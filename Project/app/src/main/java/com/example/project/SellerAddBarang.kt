package com.example.project

import android.R
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.project.databinding.ActivitySellerAddBarangBinding
import com.example.project.ui.profile.Profile
import com.example.project.ui.transaction.Transaction
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class SellerAddBarang : BaseClass() {
    private lateinit var binding: ActivitySellerAddBarangBinding
    val viewModel by viewModels<UserViewModel>()
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    val calendar = Calendar.getInstance()
    val category = arrayOf("Select a category","Household Appliances", "Toy & Hobby", "Fashion")
    val cities = arrayOf("Select a city","Surabaya", "Malang", "Sidoarjo", "Kediri", "Jember")
    val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))

    var startDateTime: LocalDateTime? = null
    var endDateTime: LocalDateTime? = null

    var formatted = ""
    var dateCons = true
    val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val selectedDateTime = LocalDateTime.of(year, month + 1, day, hour, minute)
            formatted = selectedDateTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy | HH:mm", Locale.getDefault()))
            val now = LocalDateTime.now()

            if (selectedDateTime.isBefore(now)) {
                Toast.makeText(this, "Date and time cannot be in the past", Toast.LENGTH_LONG).show()
                return@OnTimeSetListener
            }

            if (dateCons) {
                startDateTime = selectedDateTime
                binding.Tanggal.setText(formatted)
            } else {
                endDateTime = selectedDateTime
                binding.Jam.setText(formatted)

                if (startDateTime != null && endDateTime != null) {
                    val duration = Duration.between(startDateTime, endDateTime)
                    if (duration.toMinutes() < 60) {
                        Toast.makeText(this, "End time must be at least 1 hour after start time", Toast.LENGTH_LONG).show()
                        endDateTime = null
                        binding.Jam.setText("")
                    }
                }
            }
        }

        TimePickerDialog(this, timeSetListener,
            calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true
        ).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySellerAddBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item,
            category
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.categoryList.adapter = adapter
        binding.categoryList.setSelection(0)

        adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item,
            cities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.cityList.adapter = adapter
        binding.cityList.setSelection(0)

        binding.auctionitemimg.setOnClickListener {
            openGallery()
        }

        viewModel.currUser.observe(this){ user ->
            if (user != null) {
                binding.btnSubmitBarang.setOnClickListener {
                    val productName = binding.NamaBarang.text.toString()
                    val description = binding.Deskripsi.text.toString()
                    var city = binding.cityList.selectedItem.toString()
                    val address = binding.Alamat.text.toString()
                    val bid = binding.bidTxt.text.toString().replace(".", "").toInt()
                    val dateStart = binding.Tanggal.text.toString()
                    val dateEnd = binding.Jam.text.toString()
                    var category = binding.categoryList.selectedItem.toString()
                    if (productName.isEmpty() || description.isEmpty() || city.isEmpty() || address.isEmpty() || bid.toString().isEmpty() || dateStart.isEmpty() || dateEnd.isEmpty() || category.isEmpty() || city == "Select a city" || category == "Select a category") {
                        Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
                    }
                    else if (selectedImageUri == null) {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
                    }
                    else if (bid < 10000){
                        Toast.makeText(this, "Minimum start bid is IDR 10.000", Toast.LENGTH_LONG).show()
                    }
                    else{
                        if (category == "Household Appliances"){
                            category = "Category-1"
                        }
                        else if (category == "Toy & Hobby"){
                            category = "Category-2"
                        }
                        else{
                            category = "Category-3"
                        }
                        viewModel.addItems(productName,description,city,address,bid,dateStart,dateEnd,category, selectedImageUri!!,this.contentResolver)
                        binding.NamaBarang.setText("")
                        binding.Deskripsi.setText("")
                        binding.Alamat.setText("")
                        binding.bidTxt.setText("")
                        binding.Tanggal.setText("")
                        binding.Jam.setText("")
                        binding.auctionitemimg.setImageResource(com.example.project.R.drawable.imgplace)
                        selectedImageUri = null
                        binding.cityList.setSelection(0)
                        binding.categoryList.setSelection(0)
                    }
                }

                binding.Tanggal.setOnClickListener {
                    dateCons = true
                    DatePickerDialog(this, dateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                binding.Jam.setOnClickListener{
                    dateCons = false
                    DatePickerDialog(this, dateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                binding.homebtn.setOnClickListener {
                    val intent = Intent(this, HomeUser::class.java)
                    intent.putExtra("email", user.email)
                    startActivity(intent)
                    finish()
                }

                binding.transBtn.setOnClickListener {
                    val intent = Intent(this, Transaction::class.java)
                    intent.putExtra("email", user.email)
                    startActivity(intent)
                    finish()
                }

                binding.profilebtn.setOnClickListener {
                    val intent = Intent(this, Profile::class.java)
                    intent.putExtra("email", user.email)
                    startActivity(intent)
                    finish()
                }

                binding.bidTxt.addTextChangedListener(object : TextWatcher{
                    private var current = ""
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        binding.bidTxt.removeTextChangedListener(this)
                        val rawInput = s.toString().replace("[^0-9]".toRegex(), "")

                        if (rawInput.isNotEmpty()) {
                            try {
                                val number = rawInput.toLong()

                                formatter.minimumFractionDigits = 0
                                formatter.maximumFractionDigits = 0
                                val formatted = formatter.format(number)

                                if (formatted != current) {
                                    current = formatted
                                    binding.bidTxt.setText(formatted)
                                    binding.bidTxt.setSelection(formatted.length)
                                }
                            } catch (e: NumberFormatException) {
                                binding.bidTxt.setText(current)
                            }
                        } else {
                            current = ""
                            binding.bidTxt.setText("")
                        }

                        binding.bidTxt.addTextChangedListener(this)
                    }

                })
            }
        }

        viewModel.resresponse.observe(this) { response ->
            Toast.makeText(this, response, Toast.LENGTH_LONG).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                binding.auctionitemimg.setImageURI(uri)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        if (email != null) {
            viewModel.getCurrUser(email)
        }
    }

}