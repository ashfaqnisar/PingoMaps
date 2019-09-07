package com.ezerka.pingo.adapters


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ezerka.pingo.fragments.TripsHistoryFragment
import com.ezerka.pingo.fragments.TripsUpcomingFragment

class TripsViewPagerAdapter(manager: FragmentManager, private var tabCount: Int) : FragmentPagerAdapter(manager) {


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