package com.example.voicemedcareapp.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.voicemedcareapp.ForgotPassword.ForgotPasswordActivity
import com.example.voicemedcareapp.MainActivity
import com.example.voicemedcareapp.R
import com.example.voicemedcareapp.Register.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerTextView: TextView
    private lateinit var forgotPassword : TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var loginButton1: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        forgotPassword = findViewById(R.id.forgotPassword)
        registerTextView = findViewById(R.id.textSignIn)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val db = FirebaseFirestore.getInstance()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Please enter email and password")
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Obține utilizatorul autentificat
                            val user = auth.currentUser
                            val uid = user?.uid
                            uid?.let { userId ->
                                db.collection("doctors").document(userId).get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val role = "doctor"
                                            navigateToMainActivity(role, userId)
                                        } else {
                                            db.collection("patients").document(userId).get()
                                                .addOnSuccessListener { patientDoc ->
                                                    if (patientDoc != null && patientDoc.exists()) {
                                                        val role = "patient"
                                                        navigateToMainActivity(role, userId)
                                                    } else {
                                                        showToast("User data not found in doctors or patients.")
                                                    }
                                                }
                                                .addOnFailureListener { exception ->
                                                    showToast("Error fetching patient data: ${exception.message}")
                                                }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        showToast("Error fetching doctor data: ${exception.message}")
                                    }
                            }
                        } else {
                            showToast("Authentication failed: ${task.exception?.message}")
                        }
                    }

            }
        }


        registerTextView.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        forgotPassword.setOnClickListener{
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMainActivity(role: String?, userId: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("role", role)
        intent.putExtra("userId", userId)
        Log.d("MainActivity", "Received role: $role, userId: $userId")
        startActivity(intent)
        finish()
    }


}


