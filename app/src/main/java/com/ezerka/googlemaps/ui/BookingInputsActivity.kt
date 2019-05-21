package com.ezerka.googlemaps.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.ezerka.googlemaps.R

class BookingInputsActivity : AppCompatActivity() {

    private val TAG: String = "BookingInputsActivity: "
    private lateinit var mContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_inputs)

        assignTheViews()
        assignTheLinks()
    }

    private fun assignTheViews() {
        log("assignTheViews():Init")
        mContext = applicationContext
    }

    private fun assignTheLinks() {
        log("assignTheLinks():Init")

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
