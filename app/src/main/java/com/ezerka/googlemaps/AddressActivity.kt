package com.ezerka.googlemaps

//Normal Imports

//Maps Imports
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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
    private val mPickupRequestCode: Int = 1
    private val mDestinationRequestCode: Int = 2
    private lateinit var mContext: Context
    private var mDestinationCount: Int = 0
    private var mPickupCount: Int = 0

    //Normal Variables
    private lateinit var mKey: String
    private lateinit var mPickupAddress: TextView
    private lateinit var mPickupCardView: CardView
    private lateinit var mDestinationAddress: TextView
    private lateinit var mDestinationCardView: CardView

    //Map Variables
    private lateinit var mMapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var mCenter: LatLng
    private lateinit var mPlacesClient: PlacesClient
    private lateinit var mFields: List<Place.Field>
    private lateinit var mPickupMarker: Marker
    private lateinit var mDestinationMarker: Marker
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLastLocation: Location



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaddress)

        assignTheViews()
        assignTheLinks()
    }

    private fun assignTheViews() {
        mContext = applicationContext

        mPickupAddress = findViewById(R.id.id_text_pickup_address)
        mPickupAddress.ellipsize = TextUtils.TruncateAt.MARQUEE
        mPickupAddress.marqueeRepeatLimit = -1
        mPickupAddress.isSelected = true
        mPickupAddress.setSingleLine(true)

        mDestinationAddress = findViewById(R.id.id_text_destination_address)
        mDestinationAddress.ellipsize = TextUtils.TruncateAt.MARQUEE
        mDestinationAddress.marqueeRepeatLimit = -1
        mDestinationAddress.isSelected = true
        mDestinationAddress.setSingleLine(true)

        mKey = getString(R.string.google_maps_key)
        mPickupCardView = findViewById(R.id.id_cardview_pickup)
        mDestinationCardView = findViewById(R.id.id_cardview_destination)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, mKey)
        }
        mMapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mMapFragment.getMapAsync(this)
        mPlacesClient = Places.createClient(this)
        mFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    private fun assignTheLinks() {
        mPickupCardView.setOnClickListener {
            openPickupAutocomplete()
        }
        mDestinationCardView.setOnClickListener {
            openDestAutocomplete()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        /*try{
            val success:Boolean = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.maps_custom))

            if(!success){
                makeToast("Unable to customize the  map")
            }
        }
        catch (error: Resources.NotFoundException){
            logError("Error: $error")
        }
*/
        //isCameraIdle()
    }

    private fun openPickupAutocomplete() {
        val intent: Intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, mFields).build(mContext)
        startActivityForResult(intent, mPickupRequestCode)
    }

    private fun openDestAutocomplete() {
        val intent: Intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, mFields).build(mContext)
        startActivityForResult(intent, mDestinationRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == mPickupRequestCode) {
            when (resultCode) {
                RESULT_OK -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    makeToast("Place:" + place.name + place.id)

                    val address: String = place.name.toString() + place.address
                    mPickupAddress.text = address


                    updateTheCamera(place.latLng)
                    placePickupMarker(place.latLng)

                    makeToast("Log: onActivityResult(): Pickup: Location is" + place.latLng)
                }
                RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    log("Log: onActivityResult(): Pickup: Status: " + status.statusMessage)
                }
                RESULT_CANCELED -> log("Log: onActivityResult(): Pickup: User Cancelled the operation")
            }
        }

        if (requestCode == mDestinationRequestCode) {
            when (resultCode) {
                RESULT_OK -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    makeToast("Place:" + place.name + place.id)

                    val address: String = place.address.toString()
                    mDestinationAddress.text = address


                    updateTheCamera(place.latLng)
                    placeDestinationMarker(place.latLng)

                    log("Log: onActivityResult(): Destination: Location is" + place.latLng)

                }
                RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    log("Log: onActivityResult(): Destination: Status:" + status.statusMessage)
                    makeToast("Error")
                }

                RESULT_CANCELED -> {
                    log("Log: onActivityResult(): Destination: User Cancelled the operation")
                }
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

                mPickupAddress.text = strAddress.toString()
                log("getAddressFromLocation(): Current Address is ${mPickupAddress.text}")
            } else {
                log("getAddressFromLocation(): searching the current address")
                mPickupAddress.text = getString(R.string.searching_address)
            }

        } catch (error: IOException) {
            error.stackTrace
            logError("IOException: Error: $error")
            makeToast("Could Not Get Address $error")
        }
    }


    private fun placePickupMarker(latLng: LatLng?) {

        if (mPickupCount > 0) {
            if (mPickupMarker != null) {
                mPickupMarker.remove()
            }
        }

        mPickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng!!)
                .title("Pickup Address")
                .visible(true)
        )
        mPickupMarker.position = latLng
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))

        mPickupCount++
    }

    private fun placeDestinationMarker(latLng: LatLng?) {

        if (mDestinationCount > 0) {
            if (mDestinationMarker != null) {
                mDestinationMarker.remove()
            }
        }

        mDestinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng!!)
                .title("Destination Address")
                .visible(true)
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))
        mDestinationCount++
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
