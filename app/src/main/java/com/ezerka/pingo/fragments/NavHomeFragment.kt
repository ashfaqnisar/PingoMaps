package com.ezerka.pingo.fragments

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import com.ezerka.pingo.R
import com.ezerka.pingo.models.AddressData
import com.ezerka.pingo.models.PolylineData
import com.ezerka.pingo.models.SingletonObject.addressDataSingleton
import com.ezerka.pingo.models.SingletonObject.userSingleton
import com.ezerka.pingo.models.UserLocationData
import com.ezerka.pingo.activity.BookingInputsActivity
import com.ezerka.pingo.util.Constants
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class NavHomeFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener {


    private val mPickupRequestCode: Int = 1
    private val mDestinationRequestCode: Int = 2
    private var mLocationPermissionGranted: Boolean = false
    private lateinit var mKey: String
    private lateinit var mAddressData: AddressData


    private lateinit var mPickupAddressText: TextView
    private lateinit var mPickupCardView: CardView
    private lateinit var mDestinationAddressText: TextView
    private lateinit var mDestinationCardView: CardView
    private lateinit var mPlaceTheRideButton: Button
    private lateinit var mGetMyLocationButton: FloatingActionButton
    private lateinit var mBottomSheetView: View
    private lateinit var mPolylineDataList: ArrayList<PolylineData>
    private lateinit var mStateListAnimator: StateListAnimator

    //Map Variables
    private var mPickupMarker: Marker? = null
    private var mDestinationMarker: Marker? = null
    private var mGeoApiContext: GeoApiContext? = null
    private var mUserLocation: UserLocationData? = null


    private lateinit var mMapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var mCenterLatLng: LatLng
    private lateinit var mPlacesClient: PlacesClient
    private lateinit var mFields: List<Place.Field>//Output fields
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient//Location Finder
    private lateinit var mLastLocation: Location
    private lateinit var mGoogleApiAvailability: GoogleApiAvailability


    //Firebase Variables
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mUser: FirebaseUser? = null
    private lateinit var mDatabase: FirebaseFirestore


    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate(): init")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        log("onCreateView: init")
        val view = inflater.inflate(R.layout.fragment_nav_home, container, false)
        assignTheViews(view)
        assignTheLinks()
        assignTheMethods()
        return view
    }

    private fun assignTheViews(view: View) {
        mPickupAddressText = view.findViewById(R.id.id_text_pickup_address)
        mPickupAddressText.ellipsize = TextUtils.TruncateAt.MARQUEE
        mPickupAddressText.marqueeRepeatLimit = 1
        mPickupAddressText.isSelected = true
        mPickupAddressText.setSingleLine(true)

        mDestinationAddressText = view.findViewById(R.id.id_text_destination_address)
        mDestinationAddressText.ellipsize = TextUtils.TruncateAt.MARQUEE
        mDestinationAddressText.marqueeRepeatLimit = 1
        mDestinationAddressText.isSelected = true
        mDestinationAddressText.setSingleLine(true)

        mPickupCardView = view.findViewById(R.id.id_Cardview_Pickup)
        mDestinationCardView = view.findViewById(R.id.id_Cardview_Destination)

        mBottomSheetView = view.findViewById(R.id.id_Include_Bottom_Sheet)
        mPlaceTheRideButton = view.findViewById(R.id.id_But_PlaceThePickup) as Button

        mGetMyLocationButton = view.findViewById(R.id.id_Float_But_GetMyLocation)

        mKey = getString(R.string.google_maps_key)

        mStateListAnimator =
            AnimatorInflater.loadStateListAnimator(context, R.animator.lift_on_touch)

        if (!Places.isInitialized()) {
            Places.initialize(context!!, mKey)
        }

        mMapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mMapFragment.getMapAsync(this)
        mPlacesClient = Places.createClient(context!!)
        mFields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)



        mGoogleApiAvailability = GoogleApiAvailability.getInstance()

        mPolylineDataList = ArrayList()

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            mUser = firebaseAuth.currentUser
            if (mUser != null) {
                log("User is signed in with id:  " + mUser!!.uid)
            } else {
                log("User is signed out")
            }
        }
        mDatabase = FirebaseFirestore.getInstance()

        mAddressData = AddressData()
    }

    private fun assignTheLinks() {
        mPickupCardView.setOnClickListener {
            log("assignTheLinks: mPickupCardView Listener")
            openPickupAutocomplete()
        }

        mDestinationCardView.setOnClickListener {
            log("assignTheLinks(): mDestinationCardView Listener")
            openDestAutocomplete()
        }
        mGetMyLocationButton.setOnClickListener {
            log("assignTheLinks(): mGetMyLocationButton")
            if (mLocationPermissionGranted) {
                getTheUserLocation()
            } else {
                requestTheMapPermission()
            }
        }

        mPlaceTheRideButton.setOnClickListener {
            startTheActivity(BookingInputsActivity::class.java)

            /*if (mPickupMarker != null && mDestinationMarker != null) {
                startTheActivity(BookingInputsActivity::class.java)
            } else {
                logError("assignTheLinks():mPlaceTheRideButton: Didn't provide the pickup and destination")
                makeToast("Please provide the pickup and destination address")
            }*/

        }
    }

    private fun assignTheMethods() {
        requestTheMapPermission()

        getUserDetails()

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        val hyderabad = LatLng(17.3850, 78.4867)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hyderabad, 12.0f))

        if (mGeoApiContext == null) {
            mGeoApiContext = GeoApiContext.Builder()//Used for fetching the directions
                .apiKey(mKey)
                .build()
        }

        mMap.setOnPolylineClickListener(this)
        mMap.uiSettings.isZoomControlsEnabled = false
        if (mLocationPermissionGranted){
            mMap.isMyLocationEnabled = true
        }else{
            requestTheMapPermission()
        }
        isCameraIdle()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
            log("onAttach():Fragment is attached")
        } else {
            logError("onAttach(): Implement the fragment interaction listener")
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        log("onDetach():Fragment is detached")
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    private fun requestTheMapPermission(): Boolean {
        log("requestTheMapPermission():Requesting the Map Permissions")
        Dexter.withActivity(activity)

            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE
            )

            .withListener(object : MultiplePermissionsListener {

                override fun onPermissionsChecked(reportResult: MultiplePermissionsReport) {
                    log("requestTheMapPermission(): OnPermissionChecked(): Checking Whether all the permissions are granted")

                    if (reportResult.areAllPermissionsGranted()) {
                        log("requestTheMapPermission(): OnPermissionChecked(): PermissionsGranted: All permissions are granted")
                        makeToast("All Permissions Are Granted")
                        mLocationPermissionGranted = true
                    }

                    if (reportResult.isAnyPermissionPermanentlyDenied) {
                        logError("requestTheMapPermission(): OnPermissionChecked(): Denied: ${reportResult.deniedPermissionResponses}")
                        makeToast("Unable to provide all the permissions")
                        mLocationPermissionGranted = false
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
                logError("requestTheMapPermission(): ErrorListener: Error: $error")
            }

            .onSameThread()
            .check()
        return mLocationPermissionGranted
    }

    private fun placeTheDirections() {
        if (mPickupMarker != null && mDestinationMarker != null) {
            log("assignTheMethods(): Calculating Directions")
            calculateDirections(mPickupMarker, mDestinationMarker)

            val mPickupMarkerLatLng =
                LatLng(mPickupMarker!!.position.latitude, mPickupMarker!!.position.longitude)
            val mDestinationMarkerLatLng = LatLng(
                mDestinationMarker!!.position.latitude,
                mDestinationMarker!!.position.longitude
            )

            adjustTheCameraToBounds(mPickupMarkerLatLng, mDestinationMarkerLatLng)
        }
    }

    private fun calculateDirections(pickup_marker: Marker?, destination_marker: Marker?) {
        log("calculateDirections():Calculating the directions")

        val pickupLatLng =
            com.google.maps.model.LatLng(
                pickup_marker!!.position.latitude,
                pickup_marker.position.longitude
            )
        val destinationLatLng =
            com.google.maps.model.LatLng(
                destination_marker!!.position.latitude,
                destination_marker.position.longitude
            )

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
        Handler(Looper.getMainLooper()).post {
            log("addPolyLinesToTheMap(): Run: Result Routes: ${result.routes}")

            removeThePreviousPolylines()

            val route: DirectionsRoute = findTheShortestRoute(result.routes)
            mAddressData.distance = route.legs[0].distance
            mAddressData.duration = route.legs[0].duration

            addressDataSingleton = mAddressData

            log("Object: $addressDataSingleton♠")

            log("addPolyLinesToTheMap(): The route is $route")

            val decodedPath: List<com.google.maps.model.LatLng> =
                PolylineEncoding.decode(route.overviewPolyline.encodedPath)

            val newDecodedPath: MutableList<LatLng> = ArrayList()

            for (latlng: com.google.maps.model.LatLng in decodedPath) {
                log("addPolyLinesToTheMap(): Run: ForLoop: latlng: $latlng")

                newDecodedPath.add(LatLng(latlng.lat, latlng.lng))
            }

            val polyline: Polyline = mMap.addPolyline(PolylineOptions().addAll(newDecodedPath))
            polyline.color = getColor(context!!, R.color.colorPrimary)
            polyline.isClickable = true
            mPolylineDataList.add(PolylineData(polyline, route.legs[0]))
        }
    }

    private fun findTheShortestRoute(routes: Array<out DirectionsRoute>?): DirectionsRoute {
        log("findTheShortestRoute():init")

        val distanceList: ArrayList<Distance>? = ArrayList()

        for (i in routes!!) {
            for (j in i.legs) {
                distanceList!!.addAll(listOf(j.distance))
            }
        }

        log("findTheShortestRoute(): $distanceList")

        var count = 0
        val temp: Distance = distanceList!![0]//9.5
        for ((i, small) in distanceList.withIndex()) {

            if (small.inMeters < temp.inMeters) {//
                temp.inMeters = small.inMeters
                count = i
            }
        }
        return routes[count]
    }

    private fun removeThePreviousPolylines() {
        if (mPolylineDataList.size > 0) {//Checking whether the polyline was created before or not
            for (polylineData: PolylineData in mPolylineDataList) {
                polylineData.polyline.remove()
            }
            mPolylineDataList.clear()
            mPolylineDataList = ArrayList()
        }
    }

    private fun adjustTheCameraToBounds(mPickupLatLng: LatLng, mDestinationLatLng: LatLng) {
        val builder = LatLngBounds.builder()

        //Calculating the min and max bound
        builder.include(mPickupLatLng)
        builder.include(mDestinationLatLng)

        val latlngBounds = builder.build()

        val width: Int = resources.displayMetrics.widthPixels
        val height: Int = resources.displayMetrics.heightPixels

        val padding: Int = (width * 0.20).toInt()

        val cameraUpdate: CameraUpdate =
            (CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, padding))
        mMap.animateCamera(cameraUpdate)
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

        val available = mGoogleApiAvailability.isGooglePlayServicesAvailable(context!!)

        when {
            available == ConnectionResult.SUCCESS -> {
                log("isServicesOK():The Google Play Services are working")
                return true
            }
            mGoogleApiAvailability.isUserResolvableError(available) -> {
                log("isServicesOK(): Error can be solved by the user")

                val dialog: Dialog = mGoogleApiAvailability.getErrorDialog(
                    activity,
                    available,
                    Constants.ERROR_REQUEST
                )
                dialog.show()
            }
            else -> {
                logError("isGoogleServicesInstalled():User can't make the request")
                makeToast("isServicesOK():You can't make services request")
            }
        }
        return false
    }

    private fun isGpsEnabled(): Boolean {
        val manager: LocationManager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            logError("isGpsEnabled():GPS is not active")
            buildAlertMessageNoGps()
            return false
        }
        return true

    }

    private fun buildAlertMessageNoGps() {
        val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(context!!)


        alertBuilder
            .setTitle("No Location Access")
            .setMessage("Without the GPS, we will not be able to find your location. Do you want to enable it?")
            .setNegativeButton("No") { _, _ ->
                log("buildAlertMessageNoGps():User clicked No")
            }
            .setPositiveButton("Yes") { _, _ ->
                //Here dialog and the marker are present
                val openSettingsIntent =
                    Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(openSettingsIntent, Constants.PERMISSIONS_ENABLE_GPS_REQUEST)
            }

        val alertDialog: AlertDialog = alertBuilder.create()

        alertDialog.setOnCancelListener {
            alertDialog.dismiss()
        }

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(getColor(context!!, R.color.colorPrimary))
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getColor(context!!, R.color.colorPrimary))

        }
        alertDialog.show()
    }

    override fun onPolylineClick(polyline: Polyline?) {
        for ((index, polylineData: PolylineData) in mPolylineDataList.withIndex()) {

            log("onPolylineClick(): $polylineData")

            if (polyline!!.id == polylineData.polyline.id) {
                polylineData.polyline.color = getColor(context!!, R.color.colorAccent)
                polylineData.polyline.zIndex = 1F

                val polylineLatLng = LatLng(polylineData.leg.endLocation.lat, polylineData.leg.endLocation.lng)

                placeMarkerWithData(
                    polylineLatLng,
                    index + 1,
                    polylineData.leg.duration,
                    polylineData.leg.distance
                )

            } else {
                polylineData.polyline.color = getColor(context!!, R.color.colorPrimary)
                polylineData.polyline.zIndex = 0F
            }
        }
    }

    private fun getUserDetails() {
        if (mUserLocation == null) {
            mUserLocation = UserLocationData()

            mUserLocation?.user = userSingleton

            /*mDatabase.collection("Users").document(mUser?.uid.toString()).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        log("getUserDetails():OnComplete:Success")
                        mUserLocation?.userSingleton = task.result!!.toObject(UserData::class.java)
                        log("mUserLocation: $mUserLocation")
                        log("User: ${mUserLocation?.userSingleton}")

                    } else {
                        logError("getUserDetails: Error: {$task.exception}")
                    }
                }*/
        }

    }

    private fun getTheUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = false
            logError("getTheUserLocation(): Unable to assign the permissions")
            makeToast("Please provide the permission to make the  application work")
        }
        if (!isGpsEnabled()) {
            logError("getTheUserLocation():GPS is not enabled")
            makeToast("Please enable the GPS to find your location")
        } else {
            getUserDetails()

            mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    mLastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    val userGeoPoint = GeoPoint(location.latitude, location.longitude)

                    mUserLocation?.geoPoint = userGeoPoint
                    mUserLocation?.timestamp = null
                    storeTheUserLocation()


                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }

    }

    private fun storeTheUserLocation() {
        log("storeTheUserLocation():init")

        mDatabase.collection("Users_Location").document(mAuth?.currentUser?.uid.toString())
            .set(mUserLocation!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    log("storeTheUserLocation():onComplete:Success: Data Store Successfully")
                    log("Latitude: ${mUserLocation?.geoPoint?.latitude}")
                    log("Longitude: ${mUserLocation?.geoPoint?.longitude}")
                } else {
                    log("Error: ${task.exception}")
                }
            }
    }

    private fun openPickupAutocomplete() {
        val intent: Intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, mFields).build(context!!)
        startActivityForResult(intent, mPickupRequestCode)
    }

    private fun openDestAutocomplete() {
        val intent: Intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, mFields).build(context!!)
        startActivityForResult(intent, mDestinationRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == mPickupRequestCode) {
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    makeToast("Place:" + place.name + place.id)

                    mPickupAddressText.text = place.address.toString()
                    mAddressData.pickupAddress = mPickupAddressText.text as String
                    mAddressData.pickuplatlng  = place.latLng

                    updateTheCamera(place.latLng, 12F)
                    placePickupMarker(place.latLng)

                    if (mPickupMarker != null && mDestinationMarker != null) {
                        removePolylinesPresent()
                    }
                    makeToast("onActivityResult(): Pickup: Location is" + place.latLng)
                    placeTheDirections()
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    logError("onActivityResult(): Pickup: Status: " + status.statusMessage)
                }
                AppCompatActivity.RESULT_CANCELED -> logError("onActivityResult(): Pickup: User Cancelled the operation")
            }
        }

        if (requestCode == mDestinationRequestCode) {
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    makeToast("Place:" + place.name + place.id)

                    mDestinationAddressText.text = place.address.toString()

                    mAddressData.destAddress = mDestinationAddressText.text as String
                    mAddressData.destLatLng = place.latLng

                    updateTheCamera(place.latLng, 12F)
                    placeDestinationMarker(place.latLng)

                    if (mPickupMarker != null && mDestinationMarker != null) {
                        removePolylinesPresent()
                    }

                    log(" onActivityResult(): Destination: Location is" + place.latLng)
                    placeTheDirections()

                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    logError(" onActivityResult(): Destination: Status:" + status.statusMessage)
                    makeToast("Error")
                }

                AppCompatActivity.RESULT_CANCELED -> {
                    logError("onActivityResult(): Destination: User Cancelled the operation")
                }
            }
        }

        if (requestCode == Constants.PERMISSIONS_ENABLE_GPS_REQUEST) {
            if (mLocationPermissionGranted) {
                log("onActivityResult():Permissions Granted")
            } else {
                log("onActivityResult(): ${Constants.PERMISSIONS_ENABLE_GPS_REQUEST} Permissions Not Provided")
            }
        }
    }

    private fun removePolylinesPresent() {
        log("removePolyLinesPresent(): Removing the polylines present")
        if (mPolylineDataList.size > 0) {
            for (polylineData: PolylineData in mPolylineDataList) {
                polylineData.polyline.remove()
            }
            mPolylineDataList.clear()
            mPolylineDataList = ArrayList()
        }
    }

    private fun isCameraIdle() {
        mMap.setOnCameraIdleListener {
            mCenterLatLng = mMap.cameraPosition.target
            log("cameraIdle(): Getting the data from the mCenterLatLng ")
            log("Center Details: " + mCenterLatLng.latitude + "," + mCenterLatLng.longitude)
            getAddressFromMarker(mCenterLatLng.latitude, mCenterLatLng.longitude)
        }
    }

    private fun getAddressFromMarker(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.ENGLISH)

        try {

            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
            log("getAddressFromMarker():The addresses are $addresses")

            return if (addresses.isNotEmpty()) {
                val fetchedAddress: Address = addresses[0]
                log("getAddressFromMarker():The fetched address is $fetchedAddress")

                val strAddress: StringBuilder = StringBuilder()
                strAddress.append(fetchedAddress.getAddressLine(0)).append(" ")
                log("getAddressFromMarker(): The address is $strAddress")

                strAddress.toString()

            } else {
                log("getAddressFromMarker(): searching the current address")
                getString(R.string.searching_address)
            }

        } catch (error: IOException) {
            error.stackTrace
            logError("getAddressFromMarker():IOException: Error: $error")
            makeToast("Could Not Get BothAddress $error")
            return "Could Not Get the BothAddress"
        }
    }

    private fun placePickupMarker(latLng: LatLng?) {

        if (mPickupMarker != null) {
            mPickupMarker!!.remove()
        }

        mPickupMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng!!)
                .title("Pickup")
                .visible(true)
        )
        updateTheCamera(latLng, 15F)

    }

    private fun placeDestinationMarker(latLng: LatLng?) {

        if (mDestinationMarker != null) {
            mDestinationMarker!!.remove()
        }

        mDestinationMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng!!)
                .title("Destination")
                .visible(true)
        )

        updateTheCamera(latLng, 15F)
    }


    private fun placeMarkerWithData(
        latLng: LatLng?,
        index: Int,
        duration: Duration,
        distance: Distance
    ) {
        var marker: Marker? = null

        when {
            marker != null -> marker.remove()
        }

        marker = mMap.addMarker(
            MarkerOptions()
                .title("Trip : $index")
                .snippet("Duration: $duration, Distance: $distance ")
                .position(latLng!!)
                .draggable(true)
                .visible(true)
        )
        updateTheCamera(latLng, 15F)
        marker!!.showInfoWindow()
    }

    private fun updateTheCamera(latLng: LatLng?, zoom: Float) {
        val updateCamera: CameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        log("updateTheCamera: Updating the camera")
        mMap.animateCamera(updateCamera)
    }

    private fun log(log: String) {
        Timber.d("Log: $log")
    }

    private fun logError(error: String) {
        Timber.e("Log Error: $error")
    }

    private fun makeToast(toast: String) {
        log("Toast: $toast")
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }

    private fun startTheActivity(mClass: Class<*>) {
        log("startTheActivity(): ${mClass.simpleName}.class Activity")
        val intent = Intent(context, mClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
    }

    override fun onResume() {
        super.onResume()
        if (checkMapServices()) {
            if (mLocationPermissionGranted) {
                placeTheDirections()
                log("onResume(): GPS Enabled")
            } else {
                requestTheMapPermission()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        log("onStart():Starting the Activity")
        if (mLocationPermissionGranted) {
            log("onStart(): Permissions are granted")
        } else {
            requestTheMapPermission()
        }
    }

}
