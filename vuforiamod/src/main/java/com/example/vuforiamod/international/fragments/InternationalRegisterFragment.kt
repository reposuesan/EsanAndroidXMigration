package com.example.vuforiamod.international.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vuforiamod.R


/**
 * Created by killerypa on 6/01/2019.
 * yahyrparedesarteaga@gmail.com
 */

class InternationalRegisterFragment : androidx.fragment.app.Fragment() {

    val TAG = "InternationalRegisterFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView( inflater: LayoutInflater,
                               container: ViewGroup?,
                               savedInstanceState: Bundle? ): View? {
        val view = inflater.inflate(R.layout.fragment_international_register, container, false)
        // ui(view)
        return view
    }
    /*

    companion object {
        fun newInstance(): InternationalRegisterFragment = InternationalRegisterFragment()
    }
    */

}
