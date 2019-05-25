package com.ezerka.pingo.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ezerka.pingo.R
import com.ezerka.pingo.adapters.TripsViewPagerAdapter
import timber.log.Timber

class NavTripsFragment : BottomSheetDialogFragment(), TripsHistoryFragment.OnFragmentInteractionListener,
    TripsUpcomingFragment.OnFragmentInteractionListener {


    private lateinit var mContext: Context
    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var mViewPagerAdapter: TripsViewPagerAdapter
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate():Init")
        retainInstance = true
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        log("onCreateView(): Fragment is created")
        val view = inflater.inflate(R.layout.fragment_nav_trips, container, false)
        assignTheViews(view)
        assignTheLinks()
        assignTheMethods()
        return view
    }


    override fun onFragmentInteraction(uri: Uri) {
    }

    private fun assignTheViews(view: View) {
        mContext = context!!


        mViewPager = view.findViewById(R.id.id_ViewPager)


        mTabLayout = view.findViewById(R.id.id_Tabs_TripActivity)
        mTabLayout.tabGravity = TabLayout.GRAVITY_FILL
        setupViewPager(mViewPager)

        mTabLayout.setupWithViewPager(mViewPager)


    }

    private fun setupViewPager(mViewPager: ViewPager?) {
        mViewPagerAdapter = TripsViewPagerAdapter(fragmentManager!!)
        mViewPagerAdapter.addFragment("History")
        mViewPagerAdapter.addFragment("Upcoming")


        if (::mViewPagerAdapter.isInitialized) {
            mViewPager!!.adapter = mViewPagerAdapter
        }
    }

    private fun assignTheLinks() {
        mViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))

        mTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                mViewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private fun assignTheMethods() {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            log("onAttach():Fragment is attached")
            makeToast("Fragment is attached")
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

    override fun onResume() {
        super.onResume()
        log("onResume():init")
    }
}
