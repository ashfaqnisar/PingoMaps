package com.ezerka.googlemaps.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.ezerka.googlemaps.R
import com.ezerka.googlemaps.fragments.BottomSheetFragment

class BookingInputsActivity : AppCompatActivity(), BottomSheetFragment.OnFragmentInteractionListener {


    private val TAG: String = "BookingInputsActivity: "
    private lateinit var mContext: Context

    private lateinit var mButtonBottomSheet: Button
    private lateinit var mOpenBottomSheetFragment: Button
    private lateinit var mBottomLinearLayout: LinearLayout
    private lateinit var mBottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_inputs)

        assignTheViews()
        assignTheLinks()
    }

    override fun onFragmentInteraction(uri: Uri) {
        log("onFragmentInteraction():Init")
    }

    private fun assignTheViews() {
        log("assignTheViews():Init")
        mContext = applicationContext

        mButtonBottomSheet = findViewById(R.id.id_But_BottomSheet)
        mOpenBottomSheetFragment = findViewById(R.id.id_But_BottomSheetFragment)

        mBottomLinearLayout = findViewById(R.id.id_Layout_bottom_sheet)

        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomLinearLayout)
        mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        makeToast("State Collapsed")
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        makeToast("Slide Up = State Expanded")
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }
        })
    }

    private fun assignTheLinks() {
        log("assignTheLinks():Init")

        mButtonBottomSheet.setOnClickListener {
            changeTheBottomState()
        }

        mOpenBottomSheetFragment.setOnClickListener {
            val bottomSheetFragment = BottomSheetFragment()
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }


    }

    private fun changeTheBottomState() {
        if (mBottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            mButtonBottomSheet.text = getString(R.string.close_sheet)
        } else {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            mButtonBottomSheet.text = getString(R.string.expand_sheet)
        }
    }

    private fun log(log: String) {
        Log.v(TAG, "Log: $log")

    }

    private fun logError(error: String) {
        Log.e(TAG, "Log: $error")
    }

    private fun makeToast(toast: String) {
        log("Making a toast of $toast")
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    private fun startTheActivity(mClass: Class<*>) {
        val intent = Intent(mContext, mClass)
        startActivity(intent)
    }
}
