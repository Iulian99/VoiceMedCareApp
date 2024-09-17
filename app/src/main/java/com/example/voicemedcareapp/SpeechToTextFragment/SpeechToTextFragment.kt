package com.example.voicemedcareapp.SpeechToTextFragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.voicemedcareapp.R
import com.microsoft.cognitiveservices.speech.PhraseListGrammar
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer


class SpeechToTextFragment : Fragment() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var startStopButton: Button
    private lateinit var textView: TextView
    private var isListening = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_speech_to_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textView = view.findViewById(R.id.editText)
        val subscriptionKey = "a12a634d352f4ecfa7ee80dee3ab9858"
        val region = "eastus"
        val config = SpeechConfig.fromSubscription(subscriptionKey, region)
        speechRecognizer = SpeechRecognizer(config)

        config.speechRecognitionLanguage = "ro-RO"
        speechRecognizer = SpeechRecognizer(config)

        val phraseListGrammar = PhraseListGrammar.fromRecognizer(speechRecognizer)
        phraseListGrammar.addPhrase("ECG")
        phraseListGrammar.addPhrase("Electrocardiogramă")
        phraseListGrammar.addPhrase("anteroseptal")  // Poți adăuga alte fraze sau cuvinte specifice

        startStopButton.setOnClickListener {
            if (isListening) {
                stopListening()
            } else {
                startListening()
            }
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    private fun startListening() {
        isListening = true
        startStopButton.text = "Stop listening"
        textView.text = "Listening..."

        speechRecognizer.startContinuousRecognitionAsync()

        speechRecognizer.recognizing.addEventListener { _, eventArgs ->
            activity?.runOnUiThread {
                textView.text = eventArgs.result.text
            }
        }

        speechRecognizer.recognized.addEventListener { _, eventArgs ->
            activity?.runOnUiThread {
                var recognizedText = eventArgs.result.text

                recognizedText = recognizedText.replace("cake", "ECG")
                recognizedText = recognizedText.replace("kaki", "ECG")
                recognizedText = recognizedText.replace("ECA K", "ECG")

                textView.text = recognizedText
            }
        }
    }

    private fun stopListening() {
        isListening = false
        println(textView.text)
        speechRecognizer.stopContinuousRecognitionAsync().get()
    }

    override fun onDestroyView() {
        speechRecognizer.close()
        super.onDestroyView()
    }
}
