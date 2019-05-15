package com.ezerka.googlemaps.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ezerka.googlemaps.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    //Constant Variables
    private val TAG: String = "MapsActivity: "
    private var mContext: Context? = null

    //Normal Variables
    private lateinit var mInputAddress: EditText
    private lateinit var mSendButton: Button

    //Map Variables
    private lateinit var mMap: GoogleMap
    private lateinit var mMapFragment: SupportMapFragment
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLastLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        assignTheViews()
        assignTheMethods()
        assignTheLinks()
    }

    private fun assignTheViews() {
        mContext = applicationContext

        mInputAddress = findViewById(R.id.id_ET_Input_Address)
        mSendButton = findViewById(R.id.id_But_Send)

        mMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mMapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun assignTheMethods() {

    }

    private fun assignTheLinks() {
        mSendButton.setOnClickListener {
            val address: String = mInputAddress.text.toString()
            makeToast(address)
            getLocationForTheAddress(address)

        }
    }

    private fun getLocationForTheAddress(address: String) {

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val hyderabad = LatLng(17.3850, 78.4867)
        mMap.addMarker(MarkerOptions().position(hyderabad).title("Marker in Hyderabad"))

        mMap.moveCamera(CameraUpdateFactory.newLatLng(hyderabad))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hyderabad, 12.0f))

        mMap.getUiSettings().setZoomControlsEnabled(true)
        mMap.setOnMarkerClickListener(this)

        setUpTheMap()


    }

    private fun setUpTheMap() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            logError("Unable to assign the permissions")
            makeToast("Please provide the permission to make the  application work")
        }


        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

        mFusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
                mLastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }

        }

    }

    override fun onMarkerClick(p0: Marker?) = false

    private fun placeMarkerOnMap(location: LatLng) {

        val markerOptions = MarkerOptions().position(location)

        val titleAddress = getAddress(location)

        markerOptions.title(titleAddress)

        markerOptions.icon(
            BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)
            )
        )

        mMap.addMarker(markerOptions)
    }

    private fun getAddress(latlng: LatLng): String {
        val mGeocoder = Geocoder(this)
        val mAddresses: List<Address>?
        val mAddress: Address?

        var mAddressText = ""

        try {
            mAddresses = mGeocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)

            if (!(mAddresses == null || mAddresses.isEmpty())) {
                mAddress = mAddresses[0]
                for (i in 0 until mAddress.maxAddressLineIndex) {
                    mAddressText += if (i == 0) {
                        mAddress.getAddressLine(i)
                    } else {
                        "\n" + mAddress.getAddressLine(i)
                    }
                    log(mAddressText)
                }
            }

        } catch (error: IOException) {
            logError("Error:${error.localizedMessage}")
        }

        return mAddressText

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

    /*
    private fun requestTheMapPermission() {
        log("Requesting the Map Permissions")
        Dexter.withActivity(this)

            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE)

            .withListener(object: MultiplePermissionsListener {

                override fun onPermissionsChecked(reportResult: MultiplePermissionsReport) {
                    log("Checking Whether all the permissions are granted")

                    if (reportResult.areAllPermissionsGranted()) {
                        log("All permissions are granted")
                        makeToast("All Permissions Are Granted")
                        returnTheValue(1)
                    }

                    if (reportResult.isAnyPermissionPermanentlyDenied) {
                        logError("(reportResult)+Unable to grant all the permission")
                        makeToast("Unable to provide all the permissions")
                        returnTheValue(0)
                    }

                }

                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {

                    token.continuePermissionRequest()
                }
            })

            .withErrorListener{error ->
                logError("A error has been occured: $error")
                makeToast("Error Occured")
            }

            .onSameThread()
            .check()

    }
*/

    /*
    fun returnTheValue(value: Int): Boolean{
        requestTheMapPermission()
        return value == 1
    }
    */
}
