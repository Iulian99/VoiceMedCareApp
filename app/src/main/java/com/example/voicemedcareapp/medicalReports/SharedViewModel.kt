package com.example.voicemedcareapp.medicalReports

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> get() = _userId

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    fun setUserId(id: String) {
        _userId.value = id
    }

    fun setRole(role: String) {
        _role.value = role
    }
}
