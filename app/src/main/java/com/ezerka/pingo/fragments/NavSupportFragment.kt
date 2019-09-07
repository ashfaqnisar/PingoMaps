package com.ezerka.pingo.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ezerka.pingo.R
import com.ezerka.pingo.util.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class NavSupportFragment : BottomSheetDialogFragment() {

    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate():Init")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        log("onCreateView(): Fragment is created")
        val view = inflater.inflate(R.layout.fragment_nav_support, container, false)
        assignTheViews(view)
        assignTheLinks()
        assignTheMethods()
        return view
    }

    private fun assignTheViews(view: View?) {

    }

    private fun assignTheLinks() {

    }

    private fun assignTheMethods() {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            log("onAttach():Fragment is attached")
            makeToast("Fragment is attached",context)
        } else {
            //throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        log("onDetach():Fragment is detached")

    }


    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

}
