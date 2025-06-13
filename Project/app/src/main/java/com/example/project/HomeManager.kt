package com.example.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.database.dataclass.Staff
import com.example.project.databinding.ActivityHomeManagerBinding

class HomeManager : AppCompatActivity() {
    private lateinit var binding: ActivityHomeManagerBinding
    private val viewModel: ManagerViewModel by viewModels()
    private lateinit var staffAdapter: StaffAdapter
    private val filteredStaffList = mutableListOf<Staff>()
    private var currentFilter = "All"
    private var currentSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchFilter()
        setupStatusFilter()
        setupBottomNavigation()
        setupButtonListeners()
        viewModel.fetchStaffList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            viewModel.fetchStaffList()
        }
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            viewModel.fetchStaffList()
        }
    }

    private fun setupRecyclerView() {
        staffAdapter = StaffAdapter(filteredStaffList)
        binding.recyclerViewStaff.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewStaff.adapter = staffAdapter
    }

    private fun setupSearchFilter() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString().trim().lowercase()
                applyFilters()
            }
        })
    }

    private fun setupStatusFilter() {
        binding.spinnerFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFilter = parent?.getItemAtPosition(position).toString()
                applyFilters()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                currentFilter = "All"
                applyFilters()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    true
                }
                R.id.nav_add -> {
                    startActivityForResult(Intent(this, AddPegawaiActivity::class.java), 1)
                    true
                }
                R.id.nav_certificate -> {
                    startActivity(Intent(this, LaporanManager::class.java))
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun setupButtonListeners() {
        binding.buttonLogout.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.staffList.observe(this) { staff ->
            filteredStaffList.clear()
            filteredStaffList.addAll(staff)
            applyFilters()
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this@HomeManager, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFilters() {
        filteredStaffList.clear()
        val filtered = viewModel.staffList.value?.filter { staff ->
            val matchesName = staff.name.lowercase().contains(currentSearchQuery)
            val matchesStatus = when (currentFilter) {
                "All" -> true
                "Active" -> !staff.suspended
                "Suspended" -> staff.suspended
                else -> true
            }
            matchesName && matchesStatus
        } ?: emptyList()
        filteredStaffList.addAll(filtered)
        staffAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchStaffList()
    }
}