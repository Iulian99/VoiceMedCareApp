package com.example.voicemedcareapp.medicalReports.SpeechToTextFragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.voicemedcareapp.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.microsoft.cognitiveservices.speech.PhraseListGrammar
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SpeechToTextFragment : Fragment() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var startStopButton: Button
    private lateinit var buttonGeneratePDF: Button
    private lateinit var editText: EditText
    private lateinit var namePatient: TextView
    private lateinit var agePatientTextView: TextView
    private lateinit var cnpPatientTextView: TextView
    private lateinit var imageview: ImageView
    private var isListening = false
    private var text1 : String = ""
    private lateinit var progressBar: ProgressBar
    private lateinit var resultTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_speech_to_text, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar2)
        toolbar?.visibility = View.GONE
        editText = view.findViewById(R.id.editText)
        val imageViewSave = view.findViewById<ImageView>(R.id.imageViewSave)
        imageViewSave.visibility = View.GONE
        resultTextView = view.findViewById(R.id.resultTextView)

        imageViewSave.setOnClickListener {
            val patientName = arguments?.getString("firstName") + " " + arguments?.getString("lastName")
            val patientAge = arguments?.getString("age")
            val patientCNP = arguments?.getString("cnp")
            val patientPhone = arguments?.getString("phone")
            val patientEmail = arguments?.getString("email")
            val patientId = arguments?.getString("patientId") ?: "unknown_patient" // Verifică dacă argumentul este corect
            val doctorId = arguments?.getString("doctorId") ?: "unknown_doctor"   // Verifică dacă argumentul este corect

            val pdfGenerator = PDFGenerator(
                context = requireContext(),
                textView = editText,
                patientName = patientName,
                patientAge = patientAge,
                patientCNP = patientCNP,
                patientPhone = patientPhone,
                patientEmail = patientEmail,
                resultNER = resultTextView.text.toString()
            )

            val pdfFile = pdfGenerator.generatePDF()
            if (pdfFile != null) {
                uploadPdfToFirebase(patientId, doctorId)  // Transmite doctorId și patientId corecte
            }
        }
        val toolbar2 = view.findViewById<Toolbar>(R.id.toolbarPatients)
        toolbar2.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        namePatient = view.findViewById(R.id.namePatientTextView)
        agePatientTextView = view.findViewById(R.id.agePatientTextView)
        cnpPatientTextView = view.findViewById(R.id.cnpPatientTextView)
        imageview = view.findViewById(R.id.imageViewIcon)


        patientData(arguments)
        val subscriptionKey = "a12a634d352f4ecfa7ee80dee3ab9858"
        val region = "eastus"
        val config = SpeechConfig.fromSubscription(subscriptionKey, region)
        speechRecognizer = SpeechRecognizer(config)

        config.speechRecognitionLanguage = "ro-RO"
        speechRecognizer = SpeechRecognizer(config)

        val phraseListGrammar = PhraseListGrammar.fromRecognizer(speechRecognizer)
        phraseListGrammar.addPhrase("ECG")
        phraseListGrammar.addPhrase("Electrocardiogramă")
        phraseListGrammar.addPhrase("anteroseptal")


        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                1
            )
        }
        var switchNumber = 0
        imageview.setOnClickListener {
            if (switchNumber == 0 ) {
                imageview.setImageDrawable(resources.getDrawable(R.drawable.avd_play_to_pause, null))
                val drawable = imageview.drawable
                startListening()

                when (drawable) {
                    is AnimatedVectorDrawableCompat -> {
                        drawable.start()
                    }
                    is AnimatedVectorDrawable -> {
                        drawable.start()
                    }
                }

                switchNumber++
            } else {
                imageview.setImageDrawable(resources.getDrawable(R.drawable.avd_pause_to_play, null))
                val drawable = imageview.drawable
                stopListening()
                imageViewSave.visibility = View.VISIBLE

                when (drawable) {
                    is AnimatedVectorDrawableCompat -> {
                        drawable.start()
                    }
                    is AnimatedVectorDrawable -> {
                        drawable.start()
                    }
                }

                switchNumber--
            }
        }
    }

    private fun patientData(arguments: Bundle?) {
        namePatient.text = arguments?.getString("firstName") + " " + arguments?.getString("lastName")
        agePatientTextView.text = "19"
        cnpPatientTextView.text = arguments?.getString("cnp")
    }


    private fun uploadPdfToFirebase(patientId: String, doctorId: String) {
        val pdfFile = File(requireContext().filesDir, "Patient_Report.pdf")

        if (!pdfFile.exists()) {
            Toast.makeText(context, "PDF file does not exist!", Toast.LENGTH_SHORT).show()
            return
        }

        val storageReference = FirebaseStorage.getInstance().reference
        val pdfPath = "patient_reports/${System.currentTimeMillis()}_medical_report.pdf"
        val pdfRef = storageReference.child(pdfPath)
        val fileUri = Uri.fromFile(pdfFile)

        pdfRef.putFile(fileUri)
            .addOnSuccessListener {
                Log.d("UploadPDF", "PDF uploaded successfully")
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    val pdfUrl = uri.toString()
                    Log.d("UploadPDF", "Download URL: $pdfUrl")
                    savePdfUrlToFirestore(pdfUrl, patientId, doctorId)  // Salvează detaliile PDF-ului în Firestore
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UploadPDF", "Failed to upload PDF: ${exception.message}")
                Toast.makeText(
                    context,
                    "Failed to upload PDF: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun savePdfUrlToFirestore(pdfUrl: String, patientId: String, doctorId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val reportData = mapOf(
            "pdfUrl" to pdfUrl,
            "patientId" to patientId,
            "doctorId" to doctorId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("medical_reports")
            .add(reportData)
            .addOnSuccessListener {
                Log.d("FirestoreSave", "PDF URL and report details saved to Firestore")
                Toast.makeText(
                    context,
                    "PDF URL and report details saved to Firestore",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreSave", "Failed to save PDF details: ${exception.message}")
                Toast.makeText(
                    context,
                    "Failed to save PDF details: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }



    private fun startListening() {
        isListening = true

        if (editText.text.isEmpty()) {
            editText.setText("Listening...")
        }

        speechRecognizer.startContinuousRecognitionAsync()

        speechRecognizer.recognized.addEventListener { _, eventArgs ->
            activity?.runOnUiThread {
                var recognizedText = eventArgs.result.text
                Log.d("SpeechRecognition", "Recognized text: $recognizedText")

                recognizedText = formatPercentage(recognizedText)
                recognizedText = replaceMmColoanaDeMercur(recognizedText)
                recognizedText = formatLitersPerMinute(recognizedText)
                recognizedText = formatFractionalNumbers(recognizedText)
                recognizedText = formatMmColoanaDeMercur(recognizedText)

                // Verifică dacă propoziția nu a fost deja adăugată
                if (!text1.endsWith(recognizedText.trim())) {
                    text1 += " $recognizedText"
                    editText.setText(text1)
                }

                // Perform NLP processing in real-time
                performNLPProcessing(text1)
            }
        }
    }
    private fun replaceMmColoanaDeMercur(text: String): String {
        return text.replace(Regex("\\bmm coloana de mercur\\b", RegexOption.IGNORE_CASE), "mmHg")
    }

    private fun stopListening() {
        isListening = false
        Log.d("SpeechRecognition", "Stopping listening.")

        // Use coroutine to handle asynchronous task
        CoroutineScope(Dispatchers.IO).launch {
            Log.d("CoroutineScope", "Entered CoroutineScope block.")
            try {
                speechRecognizer.stopContinuousRecognitionAsync().get()
                Log.d("SpeechRecognition", "Recognition stopped asynchronously.")

                withContext(Dispatchers.Main) {
                    Log.d("MainContext", "Entered Main Context.")

                    speechRecognizer.recognized.addEventListener { _, eventArgs ->
                        Log.d("SpeechRecognition", "Recognized event triggered.")
                        var finalText = eventArgs.result.text
                        Log.d("SpeechRecognition", "Final recognized text: $finalText")

                        if (finalText.isNotEmpty()) {
                            finalText = formatPercentage(finalText)
                            finalText = replaceMmColoanaDeMercur(finalText)
                            Log.d("TextFormatting", "Formatted text: $finalText")
                            text1 += " $finalText"
                        } else {
                            Log.d("SpeechRecognition", "No text recognized.")
                        }
                    }
                    speechRecognizer.sessionStopped.addEventListener { _, _ ->
                        Log.d("SpeechRecognition", "Session stopped event triggered.")
                        if (text1.isNotEmpty()) {
                            performNLPProcessing(text1)
                        }
                    }
                    speechRecognizer.stopContinuousRecognitionAsync().get()
                }
            } catch (ex: Exception) {
                Log.e("SpeechRecognition", "Failed to stop recognition: ${ex.message}")
            }
        }
    }
    private fun performNLPProcessing(text: String) {
        Log.d("NLPProcessing", "Performing NLP on text: $text")

        val textInference = SpeechToTextExtractText(requireContext(), text)
        val result: String = textInference.runInference()

        Log.d("NLPProcessing", "Full NLP result: $result")

        val entityPairs = mapOf(
            "FEG_STANGA" to "VALUE_FEG_STANGA",
            "FEG_DREAPTA" to "VALUE_FEG_DREAPTA",
            "VOLUM_ATRIAL_STANG" to "VALUE_VOLUM_ATRIAL_STANG",
            "VOLUM_ATRIAL_DREPT" to "VALUE_VOLUM_ATRIAL_DREPT",
            "DIAMETRU_SEPTAL" to "VALUE_DIAMETRU_SEPTAL",
            "GROSIME_PERETE" to "VALUE_GROSIME_PERETE",
            "DEBIT_CARDIAC" to "VALUE_DEBIT_CARDIAC",
            "PRESIUNE_ARTERELOR" to "VALUE_PRESIUNE_ARTERELOR"
        )

        val detectedEntities = mutableListOf<String>()

        // Split result by lines and try to match entities and their values
        val lines = result.lines()

        for ((entity, valueEntity) in entityPairs) {
            var entityValue: String? = null
            var valueEntityValue: String? = null

            for (line in lines) {
                if (line.contains(entity)) {
                    entityValue = line.substringAfter("=").trim()
                }
                if (line.contains(valueEntity)) {
                    valueEntityValue = line.substringBefore("=").trim()
                }
            }

            Log.d(
                "NLPProcessing",
                "Extracted values: $entity = $entityValue, $valueEntity = $valueEntityValue"
            )

            if (entityValue != null && valueEntityValue != null) {
                detectedEntities.add("$entity: $entityValue, $valueEntity: $valueEntityValue")
            }
        }

        if (detectedEntities.isNotEmpty()) {
            resultTextView.text = detectedEntities.joinToString(separator = "\n")
        } else {
            resultTextView.text = "Nu au fost detectate entități."
        }

        resultTextView.visibility = View.VISIBLE

        textInference.closeInterpreter()
    }


    private fun formatLitersPerMinute(text: String): String {
        return text.replace(Regex("(\\d+)\\s*l\\s*pe\\s*minut", RegexOption.IGNORE_CASE), "$1 l/min")
    }
    fun formatPercentage(text: String): String {
        return text.replace(Regex("(\\d+)\\s%"), "$1%")
    }
    private fun formatFractionalNumbers(text: String): String {
        return text.replace(Regex("(\\d+),(\\d+)"), "$1.$2")
    }
    private fun formatMmColoanaDeMercur(text: String): String {
        return text.replace(Regex("\\bmm\\s*coloană\\s*de\\s*mercur\\b", RegexOption.IGNORE_CASE), "mmHg")
    }
    override fun onDestroyView() {
        speechRecognizer.close()
        super.onDestroyView()
    }
}