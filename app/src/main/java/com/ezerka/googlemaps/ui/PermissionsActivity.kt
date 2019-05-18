package com.ezerka.googlemaps.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.ezerka.googlemaps.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class PermissionsActivity : AppCompatActivity() {

    //Constant Variables
    private val TAG: String = "PermissionsActivity: "
    private lateinit var mContext: Context

    //Normal Variables
    private lateinit var mProvideButton: Button
    private lateinit var mOpenNavigationButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        assignTheViews()
        assignTheLinks()
        checkThePermissions()

    }

    private fun checkThePermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startTheActivity(AddressActivity::class.java)
            makeToast("Permissions Granted")
        } else {
            makeToast("Permissions Not Granted")
        }
    }

    private fun assignTheViews() {
        mContext = applicationContext

        mProvideButton = findViewById(R.id.id_But_Provide_Permissions)
        mOpenNavigationButton = findViewById(R.id.id_But_Open_Navigation_Drawer)

    }

    private fun assignTheLinks() {
        mProvideButton.setOnClickListener {
            requestTheMapPermission()
        }

        mOpenNavigationButton.setOnClickListener {
            log("assignTheLinks():Starting the Navigation Activity")
            startTheActivity(NavigationActivity::class.java)
        }

    }

    private fun requestTheMapPermission() {
        log("Requesting the Map Permissions")
        Dexter.withActivity(this)

            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE
            )

            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(reportResult: MultiplePermissionsReport) {
                    log("Checking Whether all the permissions are granted")

                    if (reportResult.areAllPermissionsGranted()) {
                        log("All permissions are granted")
                        makeToast("All Permissions Are Granted")
                        startTheActivity(AddressActivity::class.java)
                    }

                    if (reportResult.isAnyPermissionPermanentlyDenied) {
                        logError("(reportResult)+Unable to grant all the permission")
                        makeToast("Unable to provide all the permissions")
                        startTheActivity(ProvidePermissionActivity::class.java)
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {

                    token.continuePermissionRequest()
                }
            })

            .withErrorListener { error ->
                logError("A error has been occured: $error")
            }

            .onSameThread()
            .check()

    }


    private fun startTheActivity(mClass: Class<*>) {
        val intent = Intent(mContext, mClass)
        startActivity(intent)
        finish()
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
}
