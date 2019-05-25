package com.ezerka.pingo.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.ezerka.pingo.fragments.TripsHistoryFragment
import com.ezerka.pingo.fragments.TripsUpcomingFragment

class TripsViewPagerAdapter(manager: FragmentManager, private var tabCount: Int) : FragmentStatePagerAdapter(manager) {

    override fun getCount(): Int {
        return tabCount
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                TripsHistoryFragment()
            }
            1 -> {
                TripsUpcomingFragment()
            }
            else -> {
                TripsHistoryFragment()
            }
        }
    }


}