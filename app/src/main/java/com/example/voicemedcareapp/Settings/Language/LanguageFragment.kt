package com.example.cocktailsapp.Setttings.Language

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.example.voicemedcareapp.R
import java.util.Locale


class LanguageFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_language, container, false)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val languageRadioGroup = view.findViewById<RadioGroup>(R.id.language_radio_group)


        val currentLanguage = sharedPreferences.getString("app_language", "en")
        when (currentLanguage) {
            "en" -> languageRadioGroup.check(R.id.radio_english)
            "ro" -> languageRadioGroup.check(R.id.radio_romanian)
        }

        languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_english -> setLocale("en")
                R.id.radio_romanian -> setLocale("ro")
            }
        }

        return view
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = requireContext().resources.configuration
        config.setLocale(locale)
        requireContext().createConfigurationContext(config)

        val editor = sharedPreferences.edit()
        editor.putString("app_language", languageCode)
        editor.apply()

        requireActivity().recreate()
    }
}