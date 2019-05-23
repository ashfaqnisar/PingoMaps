package com.ezerka.pingo.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.ezerka.pingo.R
import com.ezerka.pingo.fragments.BottomSheetFragment
import timber.log.Timber

class BookingInputsActivity : AppCompatActivity(), BottomSheetFragment.OnFragmentInteractionListener {


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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
    }
}
