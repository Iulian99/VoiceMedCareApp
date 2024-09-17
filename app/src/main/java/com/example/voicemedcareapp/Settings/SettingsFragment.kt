package com.example.voicemedcareapp.Settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.voicemedcareapp.R
import com.example.voicemedcareapp.Register.RegisterActivity

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val listView = view.findViewById<ListView>(R.id.settings_list_view)
        val settingsItems = listOf(
            "Language",
            "Contact",
            "Questions",
            "About Application",
            "Logout"
        )
        val settingsDescriptions = listOf(
            "Change language settings",
            "Contact support",
            "Frequently asked questions",
            "Information about the app",
            "Sign out of the application"
        )

        val adapter = SettingsAdapter(requireContext(), settingsItems, settingsDescriptions)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> findNavController().navigate(R.id.languageFragment)
                1 -> findNavController().navigate(R.id.contactFragment)
                2 -> findNavController().navigate(R.id.questionsFragment)
                3 -> findNavController().navigate(R.id.aboutFragment)
                4 -> performLogout()
            }
        }

        return view
    }

    private fun performLogout() {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        val intent = Intent(requireContext(), RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}

class SettingsAdapter(context: Context, private val titles: List<String>, private val descriptions: List<String>) :
    ArrayAdapter<String>(context, 0, titles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_settings, parent, false)
        val titleTextView = view.findViewById<TextView>(R.id.settings_item_title)
        val descriptionTextView = view.findViewById<TextView>(R.id.settings_item_description)

        titleTextView.text = titles[position]
        descriptionTextView.text = descriptions[position]
        if (titles[position] == "Logout") {
            titleTextView.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
        } else {
            titleTextView.setTextColor(ContextCompat.getColor(context, R.color.gray4))
        }

        return view
    }
}
