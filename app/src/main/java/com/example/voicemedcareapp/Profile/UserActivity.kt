package com.example.voicemedcareapp.Profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.voicemedcareapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserActivity : AppCompatActivity() {

    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        userName = findViewById(R.id.userName)
        userEmail = findViewById(R.id.userEmail)
        editButton = findViewById(R.id.edit)
        fetchUserInfo()

        editButton.setOnClickListener {
            replaceFragment(EditProfileFragment())
        }
    }

    private fun fetchUserInfo() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val uid = auth.currentUser?.uid

        uid?.let {
            db.collection("doctors").document(it).get()
                .addOnSuccessListener { doctorDocument ->
                    if (doctorDocument.exists()) {
                        val name = "${doctorDocument.getString("firstName")} ${doctorDocument.getString("lastName")}"
                        val email = doctorDocument.getString("email")

                        Log.d("UserActivity", "Name: $name, Email: $email, Role: Doctor")
                        userName.text = name
                        userEmail.text = email
                    } else {
                        db.collection("patients").document(it).get()
                            .addOnSuccessListener { patientDocument ->
                                if (patientDocument.exists()) {
                                    val name = "${patientDocument.getString("firstName")} ${patientDocument.getString("lastName")}"
                                    val email = patientDocument.getString("email")

                                    Log.d("UserActivity", "Name: $name, Email: $email, Role: Patient")

                                    userName.text = name
                                    userEmail.text = email
                                } else {
                                    Log.d("UserActivity", "No document found in both collections")
                                    userName.text = "Name not found"
                                    userEmail.text = "Email not found"
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("UserActivity", "Error fetching patient data", exception)
                                userName.text = "Error: ${exception.message}"
                                userEmail.text = "Error: ${exception.message}"
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("UserActivity", "Error fetching doctor data", exception)
                    userName.text = "Error: ${exception.message}"
                    userEmail.text = "Error: ${exception.message}"
                }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        findViewById<ConstraintLayout>(R.id.fieldsLayout).visibility = View.GONE
        findViewById<LinearLayout>(R.id.linearLayout2).visibility = View.GONE

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            findViewById<ConstraintLayout>(R.id.fieldsLayout).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.linearLayout2).visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }
}