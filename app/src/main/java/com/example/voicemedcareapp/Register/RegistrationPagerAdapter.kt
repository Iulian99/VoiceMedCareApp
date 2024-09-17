package com.example.voicemedcareapp.Register

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class RegistrationPagerAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): androidx.fragment.app.Fragment {
        return when (position) {
            0 -> PatientRegistrationFragment()
            1 -> DoctorRegistrationFragment()
            else -> PatientRegistrationFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2 // Number of fragments (Patient and Doctor)
    }
}