package com.example.voicemedcareapp.Memento

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.voicemedcareapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationFragment : Fragment() {

    private lateinit var notificationRecyclerView: RecyclerView
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var addNotificationFab: FloatingActionButton
    private val notificationList = mutableListOf<NotificationEntity>()

    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        notificationRecyclerView = view.findViewById(R.id.notificationRecyclerView)
        addNotificationFab = view.findViewById(R.id.addNotificationFab)

        notificationAdapter = NotificationAdapter(notificationList) { position ->
            deleteNotification(position)
        }

        notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationRecyclerView.adapter = notificationAdapter

        addNotificationFab.setOnClickListener {
            showAddNotificationDialog()
        }
        return view
    }
    private fun loadNotifications() {
        lifecycleScope.launch {
            val notifications = withContext(Dispatchers.IO) {
            }
            notificationList.clear()
            notificationAdapter.notifyDataSetChanged()
        }
    }

    private fun addNotification(notification: NotificationEntity) {
        notificationList.add(notification)
        notificationAdapter.notifyItemInserted(notificationList.size - 1)

        lifecycleScope.launch(Dispatchers.IO) {
//            db.notificationDao().insert(notification)
        }

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("notificationText", notification.text)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            notificationList.size, // Folosește ID-uri diferite pentru fiecare notificare
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            notification.date.time, // Setează ora exactă pentru notificare
            pendingIntent
        )
    }


    private fun deleteNotification(position: Int) {
        val notification = notificationList[position]
        notificationList.removeAt(position)
        notificationAdapter.notifyItemRemoved(position)

        lifecycleScope.launch(Dispatchers.IO) {
//            db.notificationDao().delete(notification)
        }
    }

    private fun showAddNotificationDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_notification, null)
        val dateTextView = dialogView.findViewById<TextView>(R.id.dateTextView)
        val timeTextView = dialogView.findViewById<TextView>(R.id.timeTextView)
        val notificationText = dialogView.findViewById<EditText>(R.id.notificationText)

        val calendar = Calendar.getInstance()

        // Deschide DatePickerDialog la apăsarea pe TextView pentru dată
        dateTextView.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    dateTextView.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // Deschide TimePickerDialog la apăsarea pe TextView pentru oră
        timeTextView.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    timeTextView.text = timeFormat.format(calendar.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )
            timePickerDialog.show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Notification")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val date = calendar.time
                val text = notificationText.text.toString()

                // Adaugă notificarea în listă și salvează în baza de date
                addNotification(NotificationEntity(date = date, text = text))
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

//    private fun addNotification(notification: NotificationEntity) {
//        notificationList.add(notification)
//        notificationAdapter.notifyItemInserted(notificationList.size - 1)
//
//        lifecycleScope.launch(Dispatchers.IO) {
//            db.notificationDao().insert(notification)
//        }
//
//        // Configurarea AlarmManager pentru notificări
//        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
//            putExtra("notificationText", notification.text)
//        }
//        val pendingIntent = PendingIntent.getBroadcast(
//            requireContext(),
//            notificationList.size, // Folosește ID-uri diferite pentru fiecare notificare
//            intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        alarmManager.setExact(
//            AlarmManager.RTC_WAKEUP,
//            notification.date.time, // Setează ora exactă pentru notificare
//            pendingIntent
//        )
//    }
}
