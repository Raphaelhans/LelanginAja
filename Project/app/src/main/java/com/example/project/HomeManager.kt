package com.example.project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.databinding.ActivityHomeManagerBinding
import com.google.firebase.firestore.FirebaseFirestore

class HomeManager : AppCompatActivity() {
    private lateinit var binding: ActivityHomeManagerBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var staffAdapter: StaffAdapter
    private val staffList = mutableListOf<Staff>()
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
        fetchStaffList()

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            fetchStaffList()
        }
    }

    private fun setupRecyclerView() {
        staffAdapter = StaffAdapter(filteredStaffList,
            onSuspendClick = { staff ->
                toggleSuspendStatus(staff)
            },
            onDeleteClick = { staff ->
                deleteStaff(staff)
            }
        )
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

    private fun fetchStaffList() {
        db.collection("Staffs")
            .whereEqualTo("status", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val staff = querySnapshot.documents.mapNotNull { it.toObject(Staff::class.java) }
                staffList.clear()
                staffList.addAll(staff)
                applyFilters()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@HomeManager, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FirestoreError", "Fetch staff list failed: ${e.message}", e)
            }
    }

    private fun applyFilters() {
        filteredStaffList.clear()
        val filtered = staffList.filter { staff ->
            val matchesName = staff.name.lowercase().contains(currentSearchQuery)
            val matchesStatus = when (currentFilter) {
                "All" -> true
                "Active" -> !staff.suspended
                "Suspended" -> staff.suspended
                else -> true
            }
            matchesName && matchesStatus
        }
        filteredStaffList.addAll(filtered)
        staffAdapter.notifyDataSetChanged()
    }

    private fun toggleSuspendStatus(staff: Staff) {
        val newStatus = !staff.suspended
        db.collection("Staffs")
            .whereEqualTo("id_staff", staff.id_staff)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    db.collection("Staffs").document(document.id)
                        .update("suspended", newStatus)
                        .addOnSuccessListener {
                            fetchStaffList()
                            Toast.makeText(this@HomeManager, "Status updated", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@HomeManager, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@HomeManager, "Staff not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@HomeManager, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteStaff(staff: Staff) {
        db.collection("Staffs")
            .whereEqualTo("id_staff", staff.id_staff)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    db.collection("Staffs").document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            fetchStaffList()
                            Toast.makeText(this@HomeManager, "Staff deleted", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@HomeManager, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this@HomeManager, "Staff not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@HomeManager, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        fetchStaffList()
    }
}