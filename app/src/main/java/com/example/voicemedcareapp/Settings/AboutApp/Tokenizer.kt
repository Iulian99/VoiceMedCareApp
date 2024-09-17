package com.example.voicemedcareapp.Settings.AboutApp

import android.content.Context
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.io.InputStreamReader

class Tokenizer1(private val wordIndex: Map<String, Int>) {

    fun textsToSequences(texts: List<String>): List<List<Int>> {
        return texts.map { text ->
            text.split(" ").map { word ->
                wordIndex[word] ?: 0
            }
        }
    }
}

fun loadTokenizer(context: Context): Tokenizer1 {
    val inputStream = context.assets.open("tokenizer.json")
    val json = InputStreamReader(inputStream).use { it.readText() }
    val type = object : TypeToken<Map<String, Any>>() {}.type
    val tokenizerMap: Map<String, Any> = Gson().fromJson(json, type)

    val wordIndex = tokenizerMap["word_index"] as Map<String, Double>
    val wordIndexInt = wordIndex.mapValues { it.value.toInt() }

    return Tokenizer1(wordIndexInt)
}