package com.example.voicemedcareapp

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

class MedicalReportAdapter(
    private val context: Context,
    private val reportsList: List<Map<String, Any>>
) : RecyclerView.Adapter<MedicalReportAdapter.MedicalReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalReportViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_medical_report, parent, false)
        return MedicalReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicalReportViewHolder, position: Int) {
        val report = reportsList[position]
        holder.bind(report)
    }

    override fun getItemCount(): Int = reportsList.size

    inner class MedicalReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val patientNameTextView: TextView = itemView.findViewById(R.id.patientName)
        private val reportLinkTextView: TextView = itemView.findViewById(R.id.reportLink)
        private val pdfIcon: ImageView = itemView.findViewById(R.id.pdfIcon)

        fun bind(report: Map<String, Any>) {
            val patientName = report["patientName"] as? String ?: "Unknown Patient"
            val reportUrl = report["pdfUrl"] as? String ?: "No URL"

            patientNameTextView.text = patientName
            reportLinkTextView.text = reportUrl

            pdfIcon.setOnClickListener {
                if (reportUrl != "No URL") {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reportUrl))
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "No URL available for this report", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
