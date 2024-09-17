package com.example.voicemedcareapp.medicalReports.PacientDataFragment

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R
import com.example.voicemedcareapp.medicalReports.CnpDataExtract

class PatientsAdapter(
    private var patientsList: List<Patient>,
    private val onSelectPatient: (Patient) -> Unit
) : RecyclerView.Adapter<PatientsAdapter.PatientViewHolder>() {

    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient, parent, false)
        return PatientViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patientsList[position], position)
    }

    override fun getItemCount(): Int = patientsList.size

    fun updateList(newList: List<Patient>) {
        patientsList = newList
        notifyDataSetChanged()
    }

    inner class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.patientName)
        private val ageTextView: TextView = itemView.findViewById(R.id.patientAge)
        private val cnpTextView: TextView = itemView.findViewById(R.id.patientCNP)
        private val emailTextView: TextView = itemView.findViewById(R.id.patientEmail)
        private val phoneTextView: TextView = itemView.findViewById(R.id.patientPhone)
        private val radioButton: RadioButton = itemView.findViewById(R.id.selectPatientRadioButton)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(patient: Patient, position: Int) {
            nameTextView.text = "${patient.firstName} ${patient.lastName}"
            ageTextView.text = if (patient.age != -1) "Age: ${patient.age}" else "Age: Unknown"
            cnpTextView.text = "CNP: ${patient.cnp}"
            emailTextView.text = "Email: ${patient.email}"
            phoneTextView.text = "Phone: ${patient.phone}"

            radioButton.isChecked = position == selectedPosition
            radioButton.setOnClickListener {
                if (selectedPosition != position) {
                    notifyItemChanged(selectedPosition)
                    selectedPosition = position
                    notifyItemChanged(selectedPosition)
                    onSelectPatient(patient)
                }
            }
        }
    }
}
