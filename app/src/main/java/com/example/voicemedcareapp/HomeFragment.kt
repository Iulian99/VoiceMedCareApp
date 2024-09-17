package com.example.voicemedcareapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val button1: Button
        button1 = view.findViewById(R.id.button1)
        button1.setOnClickListener {
            Toast.makeText(requireContext(),"Butonul a fost apasat!!!!!!!!!",Toast.LENGTH_SHORT).show()
        }
        return view
    }

}