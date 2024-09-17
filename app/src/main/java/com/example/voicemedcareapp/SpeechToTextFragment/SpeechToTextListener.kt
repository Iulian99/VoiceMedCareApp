package com.example.voicemedcareapp.SpeechToTextFragment

interface SpeechToTextListener {
    fun onSpeechRecognized(text: String?)
}