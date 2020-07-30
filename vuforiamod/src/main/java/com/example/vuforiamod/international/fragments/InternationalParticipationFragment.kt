package com.example.vuforiamod.international.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vuforiamod.R

import com.example.vuforiamod.international.activities.InternationalParticipationControllerActivity


class InternationalParticipationFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView( inflater: LayoutInflater,
                               container: ViewGroup?,
                               savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_international_participation, container, false)
        ui(view)
        return view
    }


    private fun ui(view: View) {
        view.findViewById<View>(R.id.viewRealidadAumentada_iwmas).setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View) {
                    val links = Intent().setClass(getActivity()!!, InternationalParticipationControllerActivity::class.java)
                    startActivity(links)
                }
            })
    }

    /*
    companion object {
        fun newInstance(): InternationalParticipationFragment = InternationalParticipationFragment()
    }
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


}
