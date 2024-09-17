package com.example.voicemedcareapp.ReportsPatients

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R
import com.example.voicemedcareapp.medicalReports.CnpDataExtract
import com.google.firebase.firestore.FirebaseFirestore

class ResultsPatientTestFragment : Fragment() {

    private lateinit var reportsRecyclerView: RecyclerView
    private lateinit var reportsAdapter: ReportsAdapter
    private val reportsList = mutableListOf<ReportItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_results_patient_test, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reportsRecyclerView = view.findViewById(R.id.recyclerViewReports)
        reportsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reportsAdapter = ReportsAdapter(requireContext(), reportsList)
        reportsRecyclerView.adapter = reportsAdapter

        val patientId = arguments?.getString("patientId")

        if (patientId != null) {
            loadPatientReports(patientId)
        } else {
            Toast.makeText(context, "Patient ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadPatientReports(patientId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("medical_reports")
            .whereEqualTo("patientId", patientId)
            .get()
            .addOnSuccessListener { documents ->
                reportsList.clear() // Golește lista actuală de rapoarte
                for (document in documents) {
                    val reportData = document.data
                    val pdfUrl = reportData["pdfUrl"] as? String
                    val doctorId = reportData["doctorId"] as? String

                    firestore.collection("patients")
                        .document(patientId)
                        .get()
                        .addOnSuccessListener { patientDocument ->
                            if (patientDocument.exists()) {
                                val firstName = patientDocument.getString("firstName") ?: "Unknown"
                                val lastName = patientDocument.getString("lastName") ?: "Unknown"
                                val cnp = patientDocument.getString("cnp") ?: "Unknown"
                                val email = patientDocument.getString("email") ?: "Unknown"
                                val phone = patientDocument.getString("phone") ?: "Unknown"

                                val cnpData = CnpDataExtract(cnp)
                                val age = cnpData.getAge().toString()
                                val gender = cnpData.getGender()

                                if (doctorId != null) {
                                    firestore.collection("doctors")
                                        .document(doctorId)
                                        .get()
                                        .addOnSuccessListener { doctorDocument ->
                                            if (doctorDocument.exists()) {
                                                val doctorFirstName = doctorDocument.getString("firstName") ?: "Unknown"
                                                val doctorLastName = doctorDocument.getString("lastName") ?: "Unknown"
                                                val doctorName = "$doctorFirstName $doctorLastName"

                                                reportsList.add(ReportItem(firstName, lastName, age, gender, doctorName, pdfUrl))
                                                reportsAdapter.notifyDataSetChanged()
                                            } else {
                                                val doctorName = "Unknown"
                                                reportsList.add(ReportItem(firstName, lastName, age, gender, doctorName, pdfUrl))
                                                reportsAdapter.notifyDataSetChanged()
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("Firestore", "Error getting doctor details: ", exception)
                                            Toast.makeText(context, "Failed to load doctor details: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Firestore", "Error getting patient details: ", exception)
                            Toast.makeText(context, "Failed to load patient details: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                Toast.makeText(context, "Failed to load reports: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}