package com.example.project.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.project.AccountNumber
import com.example.project.AuthViewModel
import com.example.project.BaseClass
import com.example.project.History
import com.example.project.HomeUser
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.RatingActivity
import com.example.project.SellerAddBarang
import com.example.project.UserViewModel
import com.example.project.databinding.ActivityProfileBinding
import com.example.project.ui.transaction.Transaction
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class Profile : BaseClass() {
    private lateinit var binding: ActivityProfileBinding
    val viewModel by viewModels<UserViewModel>()
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null

    val avatarNames = listOf("bycicle", "profile", "burger", "friedchicken")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.logoutBtn.setOnClickListener{
            showOutsellDialog("Are you sure you want to logout?", "logout") { newValue ->

            }
        }

        viewModel.currUser.observe(this){ user ->
            binding.homebtn.setOnClickListener {
                val intent = Intent(this, HomeUser::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                finish()
            }
            binding.transBtn.setOnClickListener {
                val intent = Intent(this, Transaction::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                intent.putExtra("user_id", viewModel.currUser.value?.user_id.toString())
                startActivity(intent)
                finish()
            }

            if (viewModel.currUser.value?.profilePicturePath != "") {
                Glide.with(this).load(viewModel.currUser.value?.profilePicturePath).into(binding.userpfpDis)
            } else {
                binding.userpfpDis.setImageResource(R.drawable.profile)
            }

            if(user?.status == 0){
                binding.sellerBtn.visibility = View.VISIBLE
                binding.sellerBtn.setOnClickListener{
                    showOutsellDialog("Are you sure you want to become a seller?", "seller") { newValue ->

                    }
                }
            }
            else{
                binding.sellerBtn.visibility = View.GONE
                binding.addBid.visibility = View.VISIBLE
                binding.addBid.setOnClickListener{
                    val intent = Intent(this, SellerAddBarang::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            binding.profileInfoBtn.setOnClickListener {
                val intent = Intent(this, ProfileInfo::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                finish()
            }

            binding.accNumberBtn.setOnClickListener{
                val intent = Intent(this, AccountNumber::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                finish()
            }

            binding.accHistoryBtn.setOnClickListener{
                val intent = Intent(this, History::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                finish()
            }

            binding.accReview.setOnClickListener{
                val intent = Intent(this, RatingActivity::class.java)
                intent.putExtra("email", viewModel.currUser.value?.email)
                startActivity(intent)
                Log.e("profil", "start")
                finish()
            }

            binding.pfpchangeBtn.setOnClickListener {
//                val dialogView = layoutInflater.inflate(R.layout.pfp_layout, null)
//                val dialog = BottomSheetDialog(this)
//                dialog.setContentView(dialogView)
//
//                val grid = dialogView.findViewById<GridView>(R.id.avatarGrid)
//                grid.adapter = object : BaseAdapter() {
//                    override fun getCount() = avatarNames.size
//                    override fun getItem(position: Int) = avatarNames[position]
//                    override fun getItemId(position: Int) = position.toLong()
//                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//                        val imageView = ImageView(parent.context).apply {
//                            val resId = resources.getIdentifier(avatarNames[position], "drawable", context.packageName)
//                            setImageResource(resId)
//                            layoutParams = AbsListView.LayoutParams(200, 200)
//                            scaleType = ImageView.ScaleType.CENTER_CROP
//                        }
//                        return imageView
//                    }
//                }
//                grid.setOnItemClickListener { _, _, position, _ ->
//                    val selected = avatarNames[position]
//
//                    val selectedResId = resources.getIdentifier(selected, "drawable", this.packageName)
//                    binding.userpfpDis.setImageResource(selectedResId)
//                    viewModel.uploadImageToStorage(selected)
//
//                    dialog.dismiss()
//                }
//                dialog.show()
                openGallery()

            }

        }

        viewModel.resresponse.observe(this){ res ->
            Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStart() {
        super.onStart()
        val email = intent.getStringExtra("email")
        viewModel.getCurrUser(email!!)
    }

    private fun showOutsellDialog(custext: String, mode: String,  onSave: (String) -> Unit) {
        if (mode == "seller"){
            AlertDialog.Builder(this)
                .setTitle(custext)
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.becomeSeller()
                }
                .setNegativeButton("No", null)
                .show()
        }
        else{
            AlertDialog.Builder(this)
                .setTitle(custext)
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
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
                binding.userpfpDis.setImageURI(uri)
                lifecycleScope.launch {
                    try {
                        viewModel.uploadImageToStorage(uri, contentResolver, "pfp")
                    } catch (e: Exception) {
                        Log.e("Upload", "Failed to upload profile image", e)
                    }
                }
            }
        }
    }


}