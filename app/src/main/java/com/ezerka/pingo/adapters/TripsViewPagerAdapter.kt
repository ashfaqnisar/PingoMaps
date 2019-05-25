package com.ezerka.pingo.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.ezerka.pingo.fragments.TripsHistoryFragment
import com.ezerka.pingo.fragments.TripsUpcomingFragment

class TripsViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager) {

    private var mTabNameList: ArrayList<String> = ArrayList()
    override fun getCount(): Int {
        return mTabNameList.size
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

    override fun getPageTitle(position: Int): CharSequence? {
        return mTabNameList[position]
    }

    fun addFragment(tabName: String) {
        mTabNameList.add(tabName)
    }


}