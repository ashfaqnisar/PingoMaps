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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class NavSettingsFragment : BottomSheetDialogFragment() {

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
        val view = inflater.inflate(R.layout.fragment_nav_settings, container, false)
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
            makeToast("Fragment is attached")
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
    private fun log(log: String) {
        Timber.d("Log: $log")
    }

    private fun logError(error: String) {
        Timber.e("Log Error: $error")
    }

    private fun makeToast(toast: String) {
        log("Toast: $toast")
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }

    private fun startTheActivity(mClass: Class<*>) {
        log("startTheActivity(): ${mClass.simpleName}.class Activity")
        val intent = Intent(context, mClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
    }

}
