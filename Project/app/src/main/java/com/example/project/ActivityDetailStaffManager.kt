package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.project.database.dataclass.Staff
import com.example.project.databinding.ActivityDetailStaffManagerBinding

class ActivityDetailStaffManager : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStaffManagerBinding
    private val viewModel: ManagerViewModel by viewModels()
    private var staff: Staff? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStaffManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        staff = intent.getParcelableExtra<Staff>("STAFF")
        if (staff == null) {
            Toast.makeText(this, "Staff data not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

      
        staff?.id_staff?.let { viewModel.fetchStaffById(it) }
        setupObservers()
        setupButtonListeners()
    }

    private fun setupViews(staff: Staff) {
        binding.textViewName.text = staff.name
        binding.textViewPhone.text = staff.phone
        binding.textViewEmail.text = staff.email
        binding.textViewSuspended.text = if (staff.suspended) "Suspended" else "Active"
        binding.textViewDeleted.text = if (staff.deleted) "Deleted" else "Not Deleted"
        binding.buttonSuspend.text = if (staff.suspended) "Unsuspend" else "Suspend"
    }

    private fun setupObservers() {
        viewModel.staffDetail.observe(this) { staff ->
            if (staff != null) {
                this.staff = staff
                setupViews(staff)
            } else {
                Toast.makeText(this, "Staff not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (message == "Staff deleted" || message == "Status updated") {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun setupButtonListeners() {
        binding.buttonBack.setOnClickListener {
            startActivity(Intent(this, HomeManager::class.java))
            finish()
        }

        binding.buttonSuspend.setOnClickListener {
            staff?.let { viewModel.toggleSuspendStatus(it) }
        }

        binding.buttonDelete.setOnClickListener {
            staff?.let { viewModel.deleteStaff(it) }
        }
    }
}