package com.example.voicemedcareapp.Settings.AboutApp

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.voicemedcareapp.R
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class AboutFragment : Fragment() {

    private lateinit var interpreter: Interpreter
    private lateinit var resultTextView: TextView
    private lateinit var predictButton: Button
    private lateinit var wordIndex: JSONObject

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_about, container, false)
        return view
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val assetFileDescriptor = requireContext().assets.openFd(modelName)
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadJSONFromAsset(fileName: String): JSONObject {
        val json: String
        try {
            val inputStream: InputStream = requireContext().assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charsets.UTF_8)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return JSONObject()
        }
        return JSONObject(json)
    }

    private fun textToSequence(text: String, wordIndex: JSONObject): IntArray {
        val words = text.split(" ")
        val sequence = IntArray(words.size)
        for (i in words.indices) {
            val word = words[i]
            sequence[i] = wordIndex.optInt(word, 0)
        }
        return sequence
    }

    private fun runModel(inputText: String): String {
        val inputSequence = textToSequence(inputText, wordIndex)

        val maxLen = 100
        val paddedInput = IntArray(maxLen)
        System.arraycopy(inputSequence, 0, paddedInput, 0, inputSequence.size.coerceAtMost(maxLen))

        val inputFloat = Array(1) { FloatArray(maxLen) }
        for (i in paddedInput.indices) {
            inputFloat[0][i] = paddedInput[i].toFloat()
        }
        val output = Array(1) { Array(99) { FloatArray(21) } }  // Conform formei [1, 69, 21]
        interpreter.run(inputFloat, output)

        val labelMap = mapOf(
            0 to "O",
            1 to "Left_Ejection_Fraction",
            2 to "Right_Ejection_Fraction",
            3 to "FEG",
            4 to "VALUE",
            5 to "FEG_STANGA",
            6 to "VALUE_FEG_STANGA",
            7 to "FEG_DREAPTA",
            8 to "VALUE_FEG_DREAPTA",
            9 to "VOLUM_ATRIAL_STANG",
            10 to "VALUE_VOLUM_ATRIAL_STANG",
            11 to "VOLUM_ATRIAL_DREPT",
            12 to "VALUE_VOLUM_ATRIAL_DREPT",
            13 to "DIAMETRU_SEPTAL",
            14 to "VALUE_DIAMETRU_SEPTAL",
            15 to "GROSIME_PERETE",
            16 to "VALUE_GROSIME_PERETE",
            17 to "DEBIT_CARDIAC",
            18 to "VALUE_DEBIT_CARDIAC",
            19 to "PRESIUNE_ARTERELOR",
            20 to "VALUE_PRESIUNE_ARTERELOR"
        )

        val detectedEntities = mutableListOf<Pair<String, String>>()
        val words = inputText.split(" ")
        var currentEntity = StringBuilder()
        var currentLabel = "O"

        for (i in output[0].indices) {
            val maxLabelIndex = output[0][i].indexOfFirst { it == output[0][i].maxOrNull() }
            val label = labelMap[maxLabelIndex] ?: "O"

            if (label == "O") {
                if (currentLabel != "O") {
                    detectedEntities.add(currentEntity.toString().trim() to currentLabel)
                    currentEntity = StringBuilder()
                }
                currentLabel = "O"
            } else {
                if (label == currentLabel) {
                    currentEntity.append(words.getOrNull(i) ?: "").append(" ")
                } else {
                    if (currentLabel != "O") {
                        detectedEntities.add(currentEntity.toString().trim() to currentLabel)
                    }
                    currentLabel = label
                    currentEntity = StringBuilder(words.getOrNull(i) ?: "").append(" ")
                }
            }
        }

        if (currentLabel != "O") {
            detectedEntities.add(currentEntity.toString().trim() to currentLabel)
        }
        return detectedEntities.joinToString(", ") { "${it.first}: ${it.second}" }
    }
}