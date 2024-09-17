package com.example.voicemedcareapp.Register

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.voicemedcareapp.Login.LoginActivity
import com.example.voicemedcareapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class DoctorRegistrationFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doctor_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext())
        }
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val first_name = view.findViewById<TextInputEditText>(R.id.first_name)
        val last_name = view.findViewById<TextInputEditText>(R.id.last_name)
        val cnp = view.findViewById<TextInputEditText>(R.id.cnp)
        val email = view.findViewById<TextInputEditText>(R.id.email)
        val specialization = view.findViewById<TextInputEditText>(R.id.specialization)
        val license_number = view.findViewById<TextInputEditText>(R.id.license_number)
        val password = view.findViewById<TextInputEditText>(R.id.password)
        val password2 = view.findViewById<TextInputEditText>(R.id.password2)
        val register_button = view.findViewById<Button>(R.id.register_button)
        val signInTextView = view.findViewById<TextView>(R.id.textSignIn)

        register_button.setOnClickListener {
            val firstName = first_name.text.toString()
            val lastName = last_name.text.toString()
            val cnp = cnp.text.toString()
            val email = email.text.toString()
            val specialization = specialization.text.toString()
            val licenseNumber = license_number.text.toString()
            val password = password.text.toString()
            val confirmPassword = password2.text.toString()

            if (password == confirmPassword) {
                registerDoctor(
                    firstName,
                    lastName,
                    cnp,
                    email,
                    specialization,
                    licenseNumber,
                    password,
                    "doctor"
                )
            } else {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }

        signInTextView.setOnClickListener {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun registerDoctor(firstName: String,lastName: String,cnp: String,email: String,
                               specialization: String,licenseNumber: String, password: String, role: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        val doctor = mapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "cnp" to cnp,
                            "email" to email,
                            "specialization" to specialization,
                            "licenseNumber" to licenseNumber,
                            "role" to role
                        )
                        firestore.collection("doctors").document(userId).set(doctor)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    "Doctor registered successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Failed to register doctor",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
