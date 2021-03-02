package com.sinch.rtc.voicevideo.features.calls.new

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sinch.rtc.voicevideo.R
import kotlinx.android.synthetic.main.fragment_new_call.*

class NewCallFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_new_call, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_newCallFragment_to_outgoingCallFragment)
        }
    }

}