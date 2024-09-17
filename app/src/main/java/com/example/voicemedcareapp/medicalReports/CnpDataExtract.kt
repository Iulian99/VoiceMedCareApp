package com.example.voicemedcareapp.medicalReports

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.Period

class CnpDataExtract(private val cnp: String) {
    val info: Map<String, Any> = extractBirthInfo()

    private fun extractBirthInfo(): Map<String, Any> {
        if (cnp.length != 13) throw IllegalArgumentException("CNP invalid")

        val s = cnp[0].toString().toInt()  // Prima cifră pentru sex și secol
        val year = cnp.substring(1, 3).toInt()  // AA - ultimele 2 cifre ale anului nașterii
        val month = cnp.substring(3, 5).toInt()  // LL - luna nașterii
        val day = cnp.substring(5, 7).toInt()  // ZZ - ziua nașterii

        val (fullYear, sex) = when (s) {
            1 -> 1900 + year to "Masculin"
            2 -> 1900 + year to "Feminin"
            3 -> 1800 + year to "Masculin"
            4 -> 1800 + year to "Feminin"
            5 -> 2000 + year to "Masculin"
            6 -> 2000 + year to "Feminin"
            7 -> 1900 + year to "Masculin rezident"
            8 -> 1900 + year to "Feminin rezident"
            else -> throw IllegalArgumentException("CNP invalid")
        }
        return mapOf(
            "sex" to sex,
            "year" to fullYear,
            "month" to month,
            "day" to day
        )
    }

    fun getBirthDate(): String {
        val day = info["day"] as Int
        val month = info["month"] as Int
        val year = info["year"] as Int
        return String.format("%02d-%02d-%d", day, month, year)
    }

    fun getGender(): String {
        return info["sex"] as String
    }

    // Metodă pentru a calcula vârsta
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAge(): Int {
        val day = info["day"] as Int
        val month = info["month"] as Int
        val year = info["year"] as Int

        val birthDate = LocalDate.of(year, month, day)
        val currentDate = LocalDate.now()

        return Period.between(birthDate, currentDate).years
    }
}
