package com.example.voicemedcareapp.medicalReports.PacientDataFragment

data class Patient(
    val cnp: String = "",
    val email: String = "",
    var age: Int = -1 ,
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val role: String = "",
    var patientId: String = ""
)