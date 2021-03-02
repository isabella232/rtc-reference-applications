package com.sinch.rtc.voicevideo.features.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sinch.rtc.voicevideo.R

class ContactsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_contacts, container, false)

}