package com.ezerka.pingo.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class TripsViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    override fun getCount(): Int {
        return mFragmentList.size
    }

    private val mFragmentList: ArrayList<Fragment> = ArrayList()
    private val mFragmentTitleList: ArrayList<String> = ArrayList()

    override fun getItem(position: Int): Fragment {
        return mFragmentList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mFragmentTitleList[position]
    }
}