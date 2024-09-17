package com.example.voicemedcareapp.Settings.Contact

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.voicemedcareapp.R

class ContactFragment : Fragment() {

    private lateinit var subjectEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneBrandEditText: EditText
    private lateinit var phoneModelEditText: EditText
    private lateinit var systemVersionEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var sendFeedbackButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contact, container, false)
        subjectEditText = view.findViewById(R.id.subject_edit_text)
        emailEditText = view.findViewById(R.id.email_edit_text)
        phoneBrandEditText = view.findViewById(R.id.phone_brand_edit_text)
        phoneModelEditText = view.findViewById(R.id.phone_model_edit_text)
        systemVersionEditText = view.findViewById(R.id.system_version_edit_text)
        messageEditText = view.findViewById(R.id.message_edit_text)
        sendFeedbackButton = view.findViewById(R.id.send_feedback_button)

        phoneBrandEditText.setText(Build.BRAND)
        phoneModelEditText.setText(Build.MODEL)
        systemVersionEditText.setText(Build.VERSION.RELEASE)


        sendFeedbackButton.setOnClickListener {
            sendFeedback()
        }

        return view
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun sendFeedback() {
        val subject = subjectEditText.text.toString()
        val email = emailEditText.text.toString()
        val phoneBrand = phoneBrandEditText.text.toString()
        val phoneModel = phoneModelEditText.text.toString()
        val systemVersion = systemVersionEditText.text.toString()
        val message = messageEditText.text.toString()

        val feedbackMessage = """
            Subject: $subject
            
            E-mail address: $email
            Phone brand: $phoneBrand
            Phone model: $phoneModel
            System version: $systemVersion
            
            Message: $message
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("your_email@example.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback from CocktailsApp")
            putExtra(Intent.EXTRA_TEXT, feedbackMessage)
        }

        Toast.makeText(requireContext(), "Email was send", Toast.LENGTH_SHORT).show()
    }
}