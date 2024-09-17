package com.example.voicemedcareapp.Memento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationAdapter(
    private val notifications: MutableList<NotificationEntity>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val notificationTextView: TextView = itemView.findViewById(R.id.notificationText)
        val deleteImageView: ImageView = itemView.findViewById(R.id.deleteImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        holder.dateTextView.text = dateFormat.format(notification.date)
        holder.timeTextView.text = timeFormat.format(notification.date)
        holder.notificationTextView.text = notification.text

        holder.deleteImageView.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = notifications.size
}