package com.example.voicemedcareapp.medicalReports.PacientDataFragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R
import com.example.voicemedcareapp.medicalReports.CnpDataExtract
import com.example.voicemedcareapp.medicalReports.SharedViewModel
import com.example.voicemedcareapp.medicalReports.VerticalSpaceItemDecoration
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class PacientDataFragment : Fragment() {
    private lateinit var patientsRecyclerView: RecyclerView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: PatientsAdapter
    private lateinit var searchView: SearchView
    private lateinit var buttonNextFragment: Button
    private var selectedPatient: Patient? = null
    private lateinit var progressBar: ProgressBar  // Declară ProgressBar
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pacient_data, container, false)

        patientsRecyclerView = view.findViewById(R.id.patientsRecyclerView)
        searchView = view.findViewById(R.id.searchView)
        buttonNextFragment = view.findViewById(R.id.button_next)

        progressBar = view.findViewById(R.id.progressBar)

        firestore = FirebaseFirestore.getInstance()
        patientsRecyclerView.layoutManager = LinearLayoutManager(context)
        val verticalSpaceItemDecoration = VerticalSpaceItemDecoration(16)
        patientsRecyclerView.addItemDecoration(verticalSpaceItemDecoration)
        val doctorId = sharedViewModel.userId.value ?: ""
        adapter = PatientsAdapter(emptyList()) { selectedPatient ->
            onPatientSelected(selectedPatient)
        }
        patientsRecyclerView.adapter = adapter

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar2)
        toolbar?.visibility = View.GONE

        val doctorId = sharedViewModel.userId.value ?: ""
        adapter = PatientsAdapter(emptyList()) { selectedPatient ->
            onPatientSelected(selectedPatient)
        }
        patientsRecyclerView.adapter = adapter
        progressBar.visibility = View.VISIBLE
        val toolbar2 = view.findViewById<Toolbar>(R.id.toolbarPatients)

        toolbar2.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        buttonNextFragment.setOnClickListener {
            selectedPatient?.let { patient ->
                navigateToNextFragment(patient,doctorId)
            } ?: run {
                Toast.makeText(context, "Please select a patient first.", Toast.LENGTH_SHORT).show()
            }
        }
        patientsRecyclerView.adapter = adapter

        fetchAllPatients()
        setupSearchView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchAllPatients() {
        firestore.collection("patients")
            .get()
            .addOnSuccessListener { documents ->
                val patientsList = documents.map { doc ->
                    val patient = doc.toObject(Patient::class.java)
                    patient.patientId = doc.id
                    if (patient.cnp.length == 13) {
                        try {
                            val cnpData = CnpDataExtract(patient.cnp)
                            patient.age =
                                cnpData.getAge()
                        } catch (e: IllegalArgumentException) {
                            patient.age = -1
                        }
                    }

                    patient
                }
                adapter.updateList(patientsList)
                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to load patients: ${exception.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun onPatientSelected(patient: Patient) {
        selectedPatient = patient
        Toast.makeText(context, "Patient selected: ${patient.firstName} ${patient.lastName}", Toast.LENGTH_SHORT).show()

    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    performSearch(newText)
                }
                return true
            }
        })
    }

    private fun performSearch(query: String) {
        val firstNameQuery = firestore.collection("patients")
            .whereGreaterThanOrEqualTo("firstName", query)
            .whereLessThanOrEqualTo("firstName", query + '\uf8ff')
            .get()

        val cnpQuery = firestore.collection("patients")
            .whereGreaterThanOrEqualTo("cnp", query)
            .whereLessThanOrEqualTo("cnp", query + '\uf8ff')
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(firstNameQuery, cnpQuery)
            .addOnSuccessListener { results ->
                val combinedPatientsList = mutableListOf<Patient>()
                results.forEach { snapshot ->
                    snapshot.documents.forEach { doc ->
                        val patient = doc.toObject(Patient::class.java)
                        if (patient != null) {
                            combinedPatientsList.add(patient)
                        }
                    }
                }

                val uniquePatientsList = combinedPatientsList.distinctBy { it.cnp }
                adapter.updateList(uniquePatientsList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Search failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun navigateToNextFragment(patient: Patient, doctorId : String) {
        Log.d("Navigation", "Navigating with patientId: ${patient.patientId}, doctorId: $doctorId")
        val action = PacientDataFragmentDirections.actionPacientDataFragmentToNextFragment(
            firstName = patient.firstName,
            lastName = patient.lastName,
            cnp = patient.cnp,
            phone = patient.phone,
            email = patient.email,
            patientId = patient.patientId,
            doctorId = doctorId
        )
        findNavController().navigate(action)
    }

}