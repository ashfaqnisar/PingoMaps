package com.ezerka.pingo.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast

import com.ezerka.pingo.R
import com.ezerka.pingo.adapters.TripsHistoryRecyclerAdapter
import com.ezerka.pingo.models.AddressData
import timber.log.Timber


class TripsHistoryFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate():Init")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        log("onCreateView(): Fragment is created")
        val view = inflater.inflate(R.layout.fragment_trips_history, container, false)
        assignTheViews(view)
        assignTheLinks()
        assignTheMethods()
        return view
    }

    private fun assignTheViews(view: View?) {
        mRecyclerView = view!!.findViewById(R.id.id_Recycler_History_Content)
//        mRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun assignTheLinks() {

    }

    private fun assignTheMethods() {
        getTheList()
    }

    private fun getTheList() {
        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        val addresses = ArrayList<AddressData>()

        addresses.add(AddressData("Hyderabad", "Attapur"))
        addresses.add(AddressData("Charminar", "Telangana"))

        val adapterWithContent = TripsHistoryRecyclerAdapter(context!!, addresses)
        mRecyclerView.adapter = adapterWithContent
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            log("onAttach():Fragment is attached")
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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
        log("Making a toast of $toast")
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }

    private fun startTheActivity(mClass: Class<*>) {
        val intent = Intent(context, mClass)
        startActivity(intent)
    }


}
