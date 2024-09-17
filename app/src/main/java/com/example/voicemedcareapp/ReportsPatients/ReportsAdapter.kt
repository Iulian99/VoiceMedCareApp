package com.example.voicemedcareapp.ReportsPatients

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R

data class ReportItem(
    val firstName: String,
    val lastName: String,
    val age: String,
    val gender: String,
    val doctorName: String,
    val pdfUrl: String?
)

class ReportsAdapter(private val context: Context, private val reportsList: List<ReportItem>) : RecyclerView.Adapter<ReportsAdapter.ReportViewHolder>() {

    class ReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val patientNameTextView: TextView = itemView.findViewById(R.id.patientName)
        val agePatientTextView: TextView = itemView.findViewById(R.id.agePatient)
        val genderPatientTextView: TextView = itemView.findViewById(R.id.genderPatient)
        val doctorNameTextView: TextView = itemView.findViewById(R.id.doctorName)
        val pdfIcon: ImageView = itemView.findViewById(R.id.pdfIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val reportItem = reportsList[position]
        holder.patientNameTextView.text = "${reportItem.firstName} ${reportItem.lastName}"
        holder.agePatientTextView.text = reportItem.age
        holder.genderPatientTextView.text = reportItem.gender
        holder.doctorNameTextView.text = reportItem.doctorName

        holder.pdfIcon.setOnClickListener {
            val pdfUrl = reportItem.pdfUrl
            if (pdfUrl != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No URL available for this report", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int {
        return reportsList.size
    }
}