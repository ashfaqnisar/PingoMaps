package com.ezerka.pingo.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.Toast
import com.ezerka.pingo.R
import com.ezerka.pingo.adapters.TripsViewPagerAdapter
import com.ezerka.pingo.fragments.TripsHistoryFragment
import com.ezerka.pingo.fragments.TripsUpcomingFragment
import timber.log.Timber

class TripsActivity : AppCompatActivity(), TripsHistoryFragment.OnFragmentInteractionListener,
    TripsUpcomingFragment.OnFragmentInteractionListener {


    private lateinit var mContext: Context
    private lateinit var mToolbar: Toolbar
    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var mViewPagerAdapter: TripsViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips)

        assignTheViews()
        assignTheLinks()

    }


    override fun onFragmentInteraction(uri: Uri) {
        log("onFragmentInteraction(): Init")
    }


    private fun assignTheViews() {
        mContext = applicationContext

        mToolbar = findViewById(R.id.id_Toolbar_TripsActivity)
        setSupportActionBar(mToolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)


        mViewPager = findViewById(R.id.id_ViewPager)
        setupViewPager(mViewPager)

        mTabLayout = findViewById(R.id.id_Tabs_TripActivity)
        mTabLayout.setupWithViewPager(mViewPager)
    }

    private fun assignTheLinks() {
        mToolbar.setNavigationOnClickListener {
            startTheActivity(MainActivity::class.java)
        }
    }

    private fun setupViewPager(mViewPager: ViewPager?) {
        mViewPagerAdapter = TripsViewPagerAdapter(supportFragmentManager)

        mViewPagerAdapter.addFragment(TripsHistoryFragment(), "History")
        mViewPagerAdapter.addFragment(TripsUpcomingFragment(), "Upcoming")

        mViewPager!!.adapter = mViewPagerAdapter
    }

    private fun log(log: String) {
        Timber.d("Log: $log")
    }

    private fun logError(error: String) {
        Timber.e("Log Error: $error")
    }

    private fun makeToast(toast: String) {
        log("Toast: $toast")
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    private fun startTheActivity(mClass: Class<*>) {
        log("startTheActivity(): ${mClass.simpleName}.class Activity")
        val intent = Intent(mContext, mClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
    }

}