package com.example.voicemedcareapp.medicalReports.SpeechToTextFragment

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel



class SpeechToTextExtractText(context: Context, private val inputText: String) {
    private lateinit var interpreter: Interpreter
    private val wordIndex: Map<String, Int> = loadWordIndex(context)
    private val maxLen = 213 // Update to match the model output
    private val NUMBER_OF_LABELS = 17 // Update to match the model output

    init {
        try {
            interpreter = Interpreter(loadModelFile(context, "model_final.tflite"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadWordIndex(context: Context): Map<String, Int> {
        val inputStream = context.assets.open("word_index_final.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val gson = Gson()
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    fun runInference(): String {
        val (inputTensor, tokenizedWords) = preprocessInput(inputText)
        if (inputTensor.isEmpty() || inputTensor[0].isEmpty()) {
            Log.e("TFLite", "Array de intrare este gol.")
            return "Eroare: Date de intrare invalide"
        }
        val outputTensor = Array(1) { Array(maxLen) { FloatArray(NUMBER_OF_LABELS) } }
        interpreter.run(inputTensor, outputTensor)
        return postProcessOutput(outputTensor, tokenizedWords)
    }

    private fun preprocessInput(text: String): Pair<Array<FloatArray>, List<String>> {
        val tokenizedWords = text.split(" ")
        val tokenizedText = tokenizedWords.map { word ->
            wordIndex[word.lowercase()] ?: 0
        }.toIntArray()

        val paddedText = IntArray(maxLen) { 0 }
        System.arraycopy(tokenizedText, 0, paddedText, 0, tokenizedText.size.coerceAtMost(maxLen))

        val inputTensor = Array(1) { FloatArray(maxLen) }
        for (i in paddedText.indices) {
            inputTensor[0][i] = paddedText[i].toFloat()
        }
        return Pair(inputTensor, tokenizedWords)
    }
private fun postProcessOutput(outputTensor: Array<Array<FloatArray>>, tokenizedWords: List<String>): String {
    val labelMap = arrayOf(
        "O", "FEG_STANGA", "VALUE_FEG_STANGA", "FEG_DREAPTA", "VALUE_FEG_DREAPTA",
        "VOLUM_ATRIAL_STANG", "VALUE_VOLUM_ATRIAL_STANG", "VOLUM_ATRIAL_DREPT", "VALUE_VOLUM_ATRIAL_DREPT",
        "DIAMETRU_SEPTAL", "VALUE_DIAMETRU_SEPTAL", "GROSIME_PERETE", "VALUE_GROSIME_PERETE",
        "DEBIT_CARDIAC", "VALUE_DEBIT_CARDIAC", "PRESIUNE_ARTERELOR", "VALUE_PRESIUNE_ARTERELOR"
    )

    val predictions = outputTensor[0]
    val result = StringBuilder()

    var currentLabel = ""
    var currentPhrase = StringBuilder()

    for (i in predictions.indices) {
        val maxIndex = predictions[i].indices.maxByOrNull { predictions[i][it] } ?: -1
        val label = if (maxIndex != -1) labelMap[maxIndex] else "O"
        val word = tokenizedWords.getOrNull(i) ?: ""

        if (label.startsWith("VALUE_")) {
            if (label == currentLabel) {
                currentPhrase.append(" ").append(word)
            } else {
                if (currentPhrase.isNotEmpty()) {
                    result.append("${currentPhrase.toString().trim()} = '$currentLabel'\n")
                }
                currentLabel = label
                currentPhrase = StringBuilder(word)
            }
        } else {
            if (currentPhrase.isNotEmpty()) {
                result.append("${currentPhrase.toString().trim()} = '$currentLabel'\n")
                currentPhrase = StringBuilder()
            }
            currentLabel = ""
        }
    }

    if (currentPhrase.isNotEmpty()) {
        result.append("${currentPhrase.toString().trim()} = '$currentLabel'\n")
    }
    return result.toString()
}
    fun closeInterpreter() {
        interpreter.close()
    }
}
