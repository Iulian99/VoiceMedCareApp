package com.example.voicemedcareapp.Profile

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.voicemedcareapp.R
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileFragment : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var saveButton: Button

    private val userId = "rcYSYuKouQQ3DXac4THtFtzxpd83"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEditText = view.findViewById(R.id.editTextName)
        emailEditText = view.findViewById(R.id.editTextEmail)
        phoneEditText = view.findViewById(R.id.editTextPhone)
        saveButton = view.findViewById(R.id.saveButton)

        loadUserProfile(userId)
        saveButton.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserProfile(userId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("patients")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    nameEditText.setText(document.getString("firstName") + " " + document.getString("lastName"))
                    emailEditText.setText(document.getString("email"))
                    phoneEditText.setText(document.getString("phone"))
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load user profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile() {
        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val names = name.split(" ")
        val firstName = names.firstOrNull() ?: ""
        val lastName = names.drop(1).joinToString(" ")

        val firestore = FirebaseFirestore.getInstance()
        val userUpdates = mapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "phone" to phone
        )

        firestore.collection("patients")
            .document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()

                activity?.onBackPressed()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}