package com.ezerka.pingo.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezerka.pingo.R
import com.ezerka.pingo.adapters.TripsUpcomingRecyclerAdapter
import com.ezerka.pingo.models.AddressData
import timber.log.Timber


class TripsUpcomingFragment : Fragment() {

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
        val view = inflater.inflate(R.layout.fragment_trips_upcoming, container, false)
        assignTheViews(view)
        assignTheLinks()
        assignTheMethods()
        return view
    }

    private fun assignTheViews(view: View?) {
        mRecyclerView = view!!.findViewById(R.id.id_Recycler_Upcoming_Content)

    }

    private fun assignTheLinks() {

    }

    private fun assignTheMethods() {
        getTheList()
    }

    private fun getTheList() {
        mRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val addresses = ArrayList<AddressData>()

        addresses.add(AddressData("Hyderabad", "Attapur"))
        addresses.add(AddressData("Charminar", "Telangana"))

        addresses.add(AddressData("CTC", "Aziz Nagar"))

        val adapterWithContent = TripsUpcomingRecyclerAdapter(context!!, addresses)
        mRecyclerView.adapter = adapterWithContent
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            log("onAttach(): Upcoming Fragment is attached")
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
