package com.example.voicemedcareapp.Settings.AboutApp

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.json.JSONObject
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer


class TextClassifier(context: Context) {
    private var interpreter: Interpreter
    private var wordIndex: Map<String, Int> = emptyMap()
    private val maxLen = 69 // Lungimea maximă a secvenței

    init {
        val model = loadModelFile(context, "model111_select_ops.tflite")
        interpreter = Interpreter(model, Interpreter.Options().apply {
            setUseXNNPACK(false)
        })

        val jsonString = context.assets.open("word_index.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        wordIndex = jsonObject.keys().asSequence().associateWith { jsonObject.getInt(it) }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        return FileUtil.loadMappedFile(context, modelPath)
    }

    fun classify(text: String): List<Pair<String, String>> {
        val tokens = tokenize(text)
        val input = preprocess(tokens)

        Log.d("TextClassifier", "Input shape: ${input.shape.joinToString(", ")}")
        Log.d("TextClassifier", "Input data: ${input.floatArray.joinToString(", ")}")

        val output = TensorBuffer.createFixedSize(intArrayOf(1, maxLen, 21), org.tensorflow.lite.DataType.FLOAT32)
        Log.d("TextClassifier", "Output shape: ${output.shape.joinToString(", ")}")
        Log.d("TextClassifier", "Before running the interpreter")
        try {
            interpreter.run(input.buffer, output.buffer.rewind())
            Log.d("TextClassifier", "After running the interpreter")
        } catch (e: Exception) {
            Log.e("TextClassifier", "Error running interpreter: ${e.message}")
        }
        return interpretPredictions(tokens, output.floatArray)
    }
    private fun tokenize(text: String): List<Int> {
        return text.split(" ").map { wordIndex[it] ?: 0 }
    }

    private fun preprocess(tokens: List<Int>): TensorBuffer {
        val tensorBuffer = TensorBuffer.createFixedSize(intArrayOf(1, maxLen), org.tensorflow.lite.DataType.FLOAT32)
        val paddedTokens = tokens.take(maxLen).plus(List(maxLen - tokens.size) { 0 })
        tensorBuffer.loadArray(paddedTokens.toIntArray())
        return tensorBuffer
    }

    private fun interpretPredictions(tokens: List<Int>, predictions: FloatArray): List<Pair<String, String>> {
        val labelMap = mapOf(
            0 to "O",
            1 to "Left_Ejection_Fraction",
            2 to "Right_Ejection_Fraction",
        )
        return tokens.zip(predictions.toList().chunked(21).map { it.indexOf(it.maxOrNull()!!) }).map {
            Pair(it.first.toString(), labelMap[it.second] ?: "O")
        }
    }
    fun close() {
        interpreter.close()
    }
}
