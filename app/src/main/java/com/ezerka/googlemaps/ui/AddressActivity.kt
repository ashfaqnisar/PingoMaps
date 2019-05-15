package com.ezerka.googlemaps.ui

//Normal Imports

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.ezerka.googlemaps.R
import com.ezerka.googlemaps.util.Constants.Companion.ERROR_REQUEST
import com.ezerka.googlemaps.util.Constants.Companion.PERMISSIONS_ENABLE_GPS_REQUEST
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.TravelMode
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class AddressActivity : AppCompatActivity(), OnMapReadyCallback {

    //Constant variables
    private val TAG: String = "AddressActivity: "
    private val mPickupRequestCode: Int = 1
    private val mDestinationRequestCode: Int = 2
    private lateinit var mContext: Context
    private var mLocationPermissionGranted: Boolean = true
    private lateinit var mGoogleApiAvailability: GoogleApiAvailability

    //Normal Variables
    private lateinit var mKey: String
    private lateinit var mPickupAddress: TextView
    private lateinit var mPickupCardView: CardView
    private lateinit var mDestinationAddress: TextView
    private lateinit var mDestinationCardView: CardView
    private lateinit var mPlaceThePickup: Button
    private lateinit var mGetMyLocationButton: FloatingActionButton

    //Map Variables
    private var mPickupMarker: Marker? = null
    private var mDestinationMarker: Marker? = null
    private var mGeoApiContext: GeoApiContext? = null

    private lateinit var mMapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var mCenter: LatLng
    private lateinit var mPlacesClient: PlacesClient
    private lateinit var mFields: List<Place.Field>
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLastLocation: Location


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaddress)

        assignTheViews()
        assignTheLinks()
        assignTheMethods()
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

        mPlaceThePickup = findViewById(R.id.id_But_PlaceThePickup)
        mGetMyLocationButton = findViewById(R.id.id_Float_But_GetMyLocation)

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

        mGoogleApiAvailability = GoogleApiAvailability.getInstance()


    }

    private fun assignTheLinks() {
        mPickupCardView.setOnClickListener {
            openPickupAutocomplete()
        }

        mDestinationCardView.setOnClickListener {
            openDestAutocomplete()
        }

        mGetMyLocationButton.setOnClickListener {
            getTheUserLocation()
        }

        mPlaceThePickup.setOnClickListener {
            if (mPickupMarker != null && mDestinationMarker != null) {
                makeToast("Calculating Directions")
                calculateDirections(mPickupMarker, mDestinationMarker)
            } else {
                makeToast("Please provide the pickup and destination address")
            }

        }

    }

    private fun assignTheMethods() {
        checkMapServices()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val hyderabad = LatLng(17.3850, 78.4867)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hyderabad, 12.0f))

        if (mGeoApiContext == null) {
            mGeoApiContext = GeoApiContext.Builder()
                .apiKey(mKey)
                .build()
        }
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


    private fun checkMapServices(): Boolean {
        if (isGoogleServicesInstalled()) {
            if (isGpsEnabled()) {
                return true
            }
        }
        return false
    }

    private fun isGoogleServicesInstalled(): Boolean {
        log("isServicesOK():Checking google services version")

        val available = mGoogleApiAvailability.isGooglePlayServicesAvailable(mContext)

        when {
            available == ConnectionResult.SUCCESS -> {
                log("isServicesOK():The Google Play Services are working")
                return true
            }
            mGoogleApiAvailability.isUserResolvableError(available) -> {
                log("isServicesOK(): Error can be solved by the user")

                val dialog: Dialog = mGoogleApiAvailability.getErrorDialog(this, available, ERROR_REQUEST)
                dialog.show()
            }
            else -> makeToast("isServicesOK():You can't make services request")
        }
        return false
    }

    private fun isGpsEnabled(): Boolean {
        val manager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
            return false
        }
        return true

    }

    private fun buildAlertMessageNoGps() {
        val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(this)

        alertBuilder.setMessage("This App requires GPS to work, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                //Here dialog and the marker are present
                val openSettingsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(openSettingsIntent, PERMISSIONS_ENABLE_GPS_REQUEST)
            }

        val alert: AlertDialog = alertBuilder.create()
        alert.show()
    }

    private fun calculateDirections(pickup_marker: Marker?, destination_marker: Marker?) {
        log("calculateDirections():Calculating the directions")

        val pickupLatLng =
            com.google.maps.model.LatLng(pickup_marker!!.position.latitude, pickup_marker.position.longitude)
        val destinationLatLng =
            com.google.maps.model.LatLng(destination_marker!!.position.latitude, destination_marker.position.longitude)

        val directions = DirectionsApiRequest(mGeoApiContext)

        directions.alternatives(true)
        directions.mode(TravelMode.DRIVING)

        directions.origin(pickupLatLng)
        directions.destination(destinationLatLng)
            .setCallback(object : PendingResult.Callback<DirectionsResult> {

            override fun onResult(result: DirectionsResult?) {
                log("calculateDirections(): Result is successful")

                log("calculateDirections(): Different Routes: ${result!!.routes[0]}")
                log("calculateDirections(): Duration : ${result.routes[0].legs[0].duration}")
                log("calculateDirections(): Distance: ${result.routes[0].legs[0].distance}")
                log("calculateDirections(): Geocoded Waypoints: ${result.geocodedWaypoints[0]}")

                addPolyLinesToTheMap(result)
            }

            override fun onFailure(error: Throwable?) {
                logError("calculateDirections():Error: ${error!!.localizedMessage}")
            }
        })

    }

    private fun addPolyLinesToTheMap(result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post(object : Runnable {
            override fun run() {
                log("addPolyLinesToTheMap(): Run: Result Routes: ${result.routes}")

                for (route: DirectionsRoute in result.routes) {
                    log("addPolyLinesToTheMap(): Run: ForLoop: Legs: ${route.legs[0]}")

                    val decodedPath: List<com.google.maps.model.LatLng> =
                        PolylineEncoding.decode(route.overviewPolyline.encodedPath)

                    val newDecodedPath: MutableList<LatLng> = ArrayList()

                    for (latlng: com.google.maps.model.LatLng in decodedPath) {
                        log("addPolyLinesToTheMap(): Run: ForLoop: laltng: $latlng")

                        newDecodedPath.add(LatLng(latlng.lat, latlng.lng))
                    }

                    val polyline: Polyline = mMap.addPolyline(PolylineOptions().addAll(newDecodedPath))
                    polyline.color = getColor(R.color.colorAccent)
                    polyline.isClickable = true

                }
            }
        })
    }

    private fun getTheUserLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = false
            logError("getTheUserLocation(): Unable to assign the permissions")
            makeToast("Please provide the permission to make the  application work")
        }

        mMap.isMyLocationEnabled = true

        mFusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                mLastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
            }
        }

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

                    makeToast("onActivityResult(): Pickup: Location is" + place.latLng)
                }
                RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    log("onActivityResult(): Pickup: Status: " + status.statusMessage)
                }
                RESULT_CANCELED -> log("onActivityResult(): Pickup: User Cancelled the operation")
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

                    log(" onActivityResult(): Destination: Location is" + place.latLng)

                }
                RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    log(" onActivityResult(): Destination: Status:" + status.statusMessage)
                    makeToast("Error")
                }

                RESULT_CANCELED -> {
                    log("onActivityResult(): Destination: User Cancelled the operation")
                }
            }
        }

        if (requestCode == PERMISSIONS_ENABLE_GPS_REQUEST) {
            if (mLocationPermissionGranted) {
                makeToast("Permissions Granted")
            } else {
                makeToast("Permissions Not Provided")
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
            logError("getAddressFromLocation():IOException: Error: $error")
            makeToast("Could Not Get Address $error")
        }
    }

    private fun placePickupMarker(latLng: LatLng?) {

        if (mPickupMarker != null) {
            mPickupMarker!!.remove()
        }

        mPickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng!!)
                .title("Pickup Address")
                .visible(true)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))

    }

    private fun placeDestinationMarker(latLng: LatLng?) {

        if (mDestinationMarker != null) {
            mDestinationMarker!!.remove()
        }

        mDestinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng!!)
                .title("Destination Address")
                .visible(true)
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))
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

    override fun onResume() {
        super.onResume()
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                makeToast("Permissions Granted")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        log("onStart():Starting the Activity")
    }
}
