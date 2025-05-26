package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StaffAdapter(
    private val staffList: MutableList<Staff>,
    private val onSuspendClick: (Staff) -> Unit,
    private val onDeleteClick: (Staff) -> Unit
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        val buttonSuspend: Button = itemView.findViewById(R.id.buttonSuspend)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]
        holder.textViewName.text = staff.name
        holder.textViewStatus.text = if (staff.suspended) "suspended" else "active"
        holder.buttonSuspend.text = if (staff.suspended) "Active" else "Suspend"
        holder.buttonSuspend.setOnClickListener { onSuspendClick(staff) }
        holder.buttonDelete.setOnClickListener { onDeleteClick(staff) }
    }

    override fun getItemCount(): Int = staffList.size

    fun updateStaffList(newList: List<Staff>) {
        staffList.clear()
        staffList.addAll(newList)
        notifyDataSetChanged()
    }
}