package com.example.project

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.project.database.dataclass.Users

class UserAdapter(
    private val userList: MutableList<Users>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewEmail: TextView = itemView.findViewById(R.id.textViewEmail)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.textViewEmail.text = user.email
        holder.textViewStatus.text = if (user.suspended) "Suspended" else "Active"
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ActivityDetailUserStaffs::class.java).apply {
                putExtra("USER", user)
            }
            (context as? AppCompatActivity)?.startActivityForResult(intent, 1)
                ?: context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    fun updateUsers(newList: List<Users>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }
}