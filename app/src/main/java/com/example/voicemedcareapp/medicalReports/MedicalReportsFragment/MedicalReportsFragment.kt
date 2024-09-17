package com.example.voicemedcareapp.medicalReports.MedicalReportsFragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R
import com.example.voicemedcareapp.medicalReports.CnpDataExtract
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MedicalReportsFragment : Fragment() {

    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var recyclerViewReports: RecyclerView
    private lateinit var medicalReportAdapter: MedicalReportAdapter
    private val reportsList = mutableListOf<Map<String, Any>>()
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medical_reports, container, false)

        floatingActionButton = view.findViewById(R.id.floatingActionButton)
        searchView = view.findViewById(R.id.search_view_reports)
        recyclerViewReports = view.findViewById(R.id.recycler_view_reports)
        progressBar = view.findViewById(R.id.progressBar2)

        recyclerViewReports.layoutManager = LinearLayoutManager(context)
        medicalReportAdapter = MedicalReportAdapter(requireContext(), reportsList)
        recyclerViewReports.adapter = medicalReportAdapter

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar2)

        searchView.setQueryHint("Search reports")
        floatingActionButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.pacientDataFragment)
            Toast.makeText(context, "Create Report clicked", Toast.LENGTH_SHORT).show()
        }

        // Obține doctorId și patientId din argumente
        val doctorId = arguments?.getString("doctorId")
        val patientId = arguments?.getString("patientId")

        // Încarcă rapoartele bazate pe doctorId sau patientId
        if (!doctorId.isNullOrEmpty()) {
            loadReportsFromFirestore(doctorId)
        } else if (!patientId.isNullOrEmpty()) {
            loadReportsForPatient(patientId)
        } else {
            Log.e("MedicalReportsFragment", "Neither doctorId nor patientId provided in arguments.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadReportsFromFirestore(doctorId: String) {
        progressBar.visibility = View.VISIBLE

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("medical_reports")
            .whereEqualTo("doctorId", doctorId)
            .get()
            .addOnSuccessListener { documents ->
                reportsList.clear()

                for (document in documents) {
                    val reportData = document.data
                    val patientId = reportData["patientId"] as? String
                    if (patientId != null) {
                        getPatientName(patientId) { patientName, cnp, age ->
                            reportData["patientName"] = patientName
                            reportData["cnp"] = cnp ?: "Unknown CNP"
                            reportData["age"] = age // Adaugă vârsta în reportData
                            getDoctorName(doctorId) { doctorName ->
                                reportData["doctorName"] = doctorName
                                reportsList.add(reportData)
                                medicalReportAdapter.notifyDataSetChanged()
                                progressBar.visibility = View.GONE
                            }
                        }
                    } else {
                        getDoctorName(doctorId) { doctorName ->
                            reportData["doctorName"] = doctorName
                            reportsList.add(reportData)
                            medicalReportAdapter.notifyDataSetChanged()
                            progressBar.visibility = View.GONE
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                Toast.makeText(context, "Failed to load reports: ${exception.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadReportsForPatient(patientId: String) {
        progressBar.visibility = View.VISIBLE

        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("medical_reports")
            .whereEqualTo("patientId", patientId)
            .get()
            .addOnSuccessListener { documents ->
                reportsList.clear()

                for (document in documents) {
                    val reportData = document.data
                    val doctorId = reportData["doctorId"] as? String
                    getPatientName(patientId) { patientName, cnp, age ->
                        reportData["patientName"] = patientName
                        reportData["cnp"] = cnp ?: "Unknown CNP"
                        reportData["age"] = age
                        if (doctorId != null) {
                            getDoctorName(doctorId) { doctorName ->
                                reportData["doctorName"] = doctorName
                                reportsList.add(reportData)
                                medicalReportAdapter.notifyDataSetChanged()
                                progressBar.visibility = View.GONE
                            }
                        } else {
                            reportsList.add(reportData)
                            medicalReportAdapter.notifyDataSetChanged()
                            progressBar.visibility = View.GONE
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
                Toast.makeText(context, "Failed to load reports: ${exception.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun getDoctorName(doctorId: String, callback: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("doctors").document(doctorId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val doctorName = document.getString("name") ?: "Unknown Doctor"
                    callback(doctorName)
                } else {
                    Log.d("MedicalReportsFragment", "No such document for doctorId: $doctorId")
                    callback("Unknown Doctor")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MedicalReportsFragment", "Error fetching doctor data: ", exception)
                callback("Unknown Doctor")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPatientName(patientId: String, callback: (String, String?, Int) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("patients").document(patientId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val patientName = "${document.getString("firstName")} ${document.getString("lastName")}"
                    val cnpDataExtract = CnpDataExtract(document.getString("cnp") ?: "")
                    val age = cnpDataExtract.getAge()
                    val cnp = document.getString("cnp") ?: "Unknown CNP"
                    callback(patientName, cnp, age)
                } else {
                    Log.d("MedicalReportsFragment", "No such document for patientId: $patientId")
                    callback("Unknown Patient", null, -1)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MedicalReportsFragment", "Error fetching patient data: ", exception)
                callback("Unknown Patient", null, -1)
            }
    }
}