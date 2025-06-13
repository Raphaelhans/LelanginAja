package com.example.project

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.database.dataclass.Staff

class StaffAdapter(
    private val staffList: MutableList<Staff>
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_staff, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val staff = staffList[position]
        holder.textViewName.text = staff.name
        holder.textViewStatus.text = if (staff.suspended) "suspended" else "active"
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ActivityDetailStaffManager::class.java).apply {
                putExtra("STAFF", staff)
            }
            (context as? ActivityDetailStaffManager)?.startActivityForResult(intent, 2)
                ?: context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = staffList.size

    fun updateStaffList(newList: List<Staff>) {
        staffList.clear()
        staffList.addAll(newList)
        notifyDataSetChanged()
    }
}