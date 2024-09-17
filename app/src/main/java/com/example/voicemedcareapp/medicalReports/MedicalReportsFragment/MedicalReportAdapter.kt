package com.example.voicemedcareapp.medicalReports.MedicalReportsFragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R
import com.example.voicemedcareapp.medicalReports.CnpDataExtract

class MedicalReportAdapter(
    private val context: Context,
    private val reportsList: List<Map<String, Any>>
) : RecyclerView.Adapter<MedicalReportAdapter.MedicalReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicalReportViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_medical_report, parent, false)
        return MedicalReportViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MedicalReportViewHolder, position: Int) {
        val report = reportsList[position]
        holder.bind(report)
    }

    override fun getItemCount(): Int {
        return reportsList.size
    }

    inner class MedicalReportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val patientNameTextView: TextView = itemView.findViewById(R.id.patientName)
        private val agePatientTextView: TextView = itemView.findViewById(R.id.agePatient)
        private val genderPatientTextView: TextView = itemView.findViewById(R.id.genderPatient)
        private val pdfIcon: ImageView = itemView.findViewById(R.id.pdfIcon)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(report: Map<String, Any>) {
            val patientName = report["patientName"] as? String ?: "Unknown Patient"
            val cnp = report["cnp"] as? String ?: "Unknown CNP"

            patientNameTextView.text = patientName

            if (cnp != "Unknown CNP") {
                val cnpData = CnpDataExtract(cnp)
                val age = cnpData.getAge()
                val gender = cnpData.getGender()

                agePatientTextView.text = "$age"
                genderPatientTextView.text = gender
            } else {
                agePatientTextView.text = "Unknown age"
                genderPatientTextView.text = "Unknown gender"
            }

            val reportUrl = report["pdfUrl"] as? String ?: "No URL"

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
