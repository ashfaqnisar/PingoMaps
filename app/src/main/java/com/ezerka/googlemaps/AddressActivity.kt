package com.ezerka.googlemaps

//Normal Imports

//Maps Imports
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.IOException
import java.util.*


class AddressActivity : AppCompatActivity(), OnMapReadyCallback {

    //Constant variables
    private val TAG: String = "AddressActivity: "
    private val mAutocompleteRequestCode: Int = 1
    private lateinit var mContext: Context

    //Normal Variables
    private lateinit var mKey: String
    private lateinit var mAddress: TextView
    private lateinit var mCardView: CardView

    //Map Variables
    private lateinit var mMapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var mCenter: LatLng
    private lateinit var mPlacesClient: PlacesClient
    private lateinit var mFields: List<Place.Field>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaddress)

        assignTheViews()
        assignTheLinks()
    }

    private fun assignTheViews() {
        mContext = applicationContext

        mAddress = findViewById(R.id.txtLocationAddress)
        mAddress.ellipsize = TextUtils.TruncateAt.MARQUEE
        mAddress.marqueeRepeatLimit = -1
        mAddress.isSelected = true
        mAddress.setSingleLine(true)

        mKey = getString(R.string.google_maps_key)
        mCardView = findViewById(R.id.cardView)


        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, mKey)
        }
        mMapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mMapFragment.getMapAsync(this)
        mPlacesClient = Places.createClient(this)
        mFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

    }

    private fun assignTheLinks() {
        mCardView.setOnClickListener {
            openAutoComplete()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        isCameraIdle()
    }

    private fun openAutoComplete() {
        val intent: Intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, mFields).build(mContext)
        startActivityForResult(intent, mAutocompleteRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == mAutocompleteRequestCode) {
            when (resultCode) {
                RESULT_OK -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    makeToast("Place:" + place.name + place.id)

                    if (!place.address.toString().contains(place.name.toString())) {
                        log("onActivityResult():The address contains the name")
                        val address: String = place.name.toString() + place.address
                        mAddress.text = address
                    }

                    updateTheCamera(place.latLng)
                    createMarker(place.latLng)

                    makeToast("location is" + place.latLng)
                }
                RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    makeToast("Status: " + status.statusMessage)
                }
                RESULT_CANCELED -> makeToast("User Cancelled the operation")
            }
        }
    }


    private fun isCameraIdle() {
        mMap.setOnCameraIdleListener {
            mCenter = mMap.cameraPosition.target
            log("cameraIdle(): Getting the data from the mCenter ")
            log("Center Details: " + mCenter.latitude + "," + mCenter.longitude)
            getAddressFromLocation(mCenter.latitude, mCenter.longitude)
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(mContext, Locale.ENGLISH)

        try {

            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
            log("getAddressFromLocation():The addresses are $addresses")

            if (addresses.isNotEmpty()) {
                val fetchedAddress: Address = addresses[0]
                log("getAddressFromLocation():The fetched address is $fetchedAddress")

                val strAddress: StringBuilder = StringBuilder()
                strAddress.append(fetchedAddress.getAddressLine(0)).append(" ")
                log("getAddressFromLocation(): The address is $strAddress")

                mAddress.text = strAddress.toString()
                log("getAddressFromLocation(): Current Address is ${mAddress.text}")
            } else {
                log("getAddressFromLocation(): searching the current address")
                mAddress.text = getString(R.string.searching_address)
            }

        } catch (error: IOException) {
            error.stackTrace
            logError("IOException: Error: $error")
            makeToast("Could Not Get Address $error")
        }
    }


    private fun createMarker(latLng: LatLng?) {
        val marker: LatLng? = latLng
        mMap.addMarker(MarkerOptions().position(marker!!))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 12.0f))
    }

    private fun updateTheCamera(latLng: LatLng?) {
        val updateCamera: CameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12f)
        log("updateTheCamera: Updating the camera")
        mMap.animateCamera(updateCamera)
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
