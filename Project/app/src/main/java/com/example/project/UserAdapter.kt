package com.example.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.database.dataclass.Users

class UserAdapter(
    private val userList: MutableList<Users>,
    private val onSuspendClick: (Users) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        val buttonSuspend: Button = itemView.findViewById(R.id.buttonSuspend)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.textViewName.text = user.name
        holder.textViewStatus.text = if (user.suspended) "suspended" else "active"
        holder.buttonSuspend.text = if (user.suspended) "Activate" else "Suspend"
        holder.buttonSuspend.setOnClickListener { onSuspendClick(user) }
    }

    override fun getItemCount(): Int = userList.size

    fun updateUserList(newList: List<Users>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}