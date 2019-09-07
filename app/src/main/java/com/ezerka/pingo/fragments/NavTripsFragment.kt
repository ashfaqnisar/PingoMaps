package com.ezerka.pingo.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.ezerka.pingo.R
import com.ezerka.pingo.adapters.TripsViewPagerAdapter
import com.ezerka.pingo.util.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import timber.log.Timber

class NavTripsFragment : BottomSheetDialogFragment(), TripsHistoryFragment.OnFragmentInteractionListener,
    TripsUpcomingFragment.OnFragmentInteractionListener {


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

        mViewPager = view.findViewById(R.id.id_ViewPager)

        mTabLayout = view.findViewById(R.id.id_Tabs_TripActivity)
        mTabLayout.tabGravity = TabLayout.GRAVITY_FILL

        mTabLayout.addTab(mTabLayout.newTab().setText("History"))
        mTabLayout.addTab(mTabLayout.newTab().setText("Upcoming"))
        mTabLayout.setTabTextColors(
            ContextCompat.getColor(context!!, R.color.divider),
            ContextCompat.getColor(context!!, R.color.icons)
        )

        setupViewPager()

    }

    private fun setupViewPager() {
        mViewPagerAdapter = TripsViewPagerAdapter(fragmentManager!!, mTabLayout.tabCount)
        mViewPager.adapter = mViewPagerAdapter
    }

    private fun assignTheLinks() {
        mViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))

        mTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mViewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

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


    override fun onResume() {
        super.onResume()
        log("onResume():init")
    }
}
