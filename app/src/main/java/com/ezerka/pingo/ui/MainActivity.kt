package com.ezerka.pingo.ui

//Normal Imports

import android.Manifest
import android.animation.AnimatorInflater
import android.animation.StateListAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.ezerka.pingo.R
import com.ezerka.pingo.models.PolylineData
import com.ezerka.pingo.util.Constants.ERROR_REQUEST
import com.ezerka.pingo.util.Constants.PERMISSIONS_ENABLE_GPS_REQUEST
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
import com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.*
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnPolylineClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    //Constant variables
    private val mPickupRequestCode: Int = 1
    private val mDestinationRequestCode: Int = 2
    private lateinit var mContext: Context
    private var mLocationPermissionGranted: Boolean = true
    private lateinit var mGoogleApiAvailability: GoogleApiAvailability

    //Normal Variables
    private lateinit var mKey: String
    private lateinit var mThumbnailImageView: ImageView
    private lateinit var mPickupAddressText: TextView
    private lateinit var mPickupCardView: CardView
    private lateinit var mDestinationAddressText: TextView
    private lateinit var mDestinationCardView: CardView
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mNavigationView: NavigationView
    private lateinit var mNavigationHeader: View
    private lateinit var mToggle: ActionBarDrawerToggle
    private lateinit var mToolBar: Toolbar
    private lateinit var mPlaceTheRideButton: Button
    private lateinit var mGetMyLocationButton: FloatingActionButton
    private lateinit var mPolylineDataList: ArrayList<PolylineData>
    private lateinit var mStateListAnimator: StateListAnimator

    //Map Variables
    private var mPickupMarker: Marker? = null
    private var mDestinationMarker: Marker? = null
    private var mGeoApiContext: GeoApiContext? = null

    private lateinit var mMapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var mCenterLatLng: LatLng
    private lateinit var mPlacesClient: PlacesClient
    private lateinit var mFields: List<Place.Field>
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mLastLocation: Location

    //Firebase Variables
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignTheViews()
        assignTheLinks()
        assignTheMethods()
    }

    private fun assignTheViews() {
        mContext = applicationContext

        mToolBar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolBar)

        mDrawerLayout = findViewById(R.id.id_Layout_DrawerLayout)
        mNavigationView = findViewById(R.id.id_View_NavigationView)

        mToggle =
            ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.open_the_drawer, R.string.close_the_drawer)
        mDrawerLayout.addDrawerListener(mToggle)
        mToggle.syncState()


        mNavigationView.setNavigationItemSelectedListener(this)

        mNavigationHeader = mNavigationView.getHeaderView(0)

        mThumbnailImageView = mNavigationHeader.findViewById(R.id.id_Image_Thumbnail)

        mPickupAddressText = findViewById(R.id.id_text_pickup_address)
        mPickupAddressText.ellipsize = TextUtils.TruncateAt.MARQUEE
        mPickupAddressText.marqueeRepeatLimit = 1
        mPickupAddressText.isSelected = true
        mPickupAddressText.setSingleLine(true)

        mDestinationAddressText = findViewById(R.id.id_text_destination_address)
        mDestinationAddressText.ellipsize = TextUtils.TruncateAt.MARQUEE
        mDestinationAddressText.marqueeRepeatLimit = 1
        mDestinationAddressText.isSelected = true
        mDestinationAddressText.setSingleLine(true)

        mPlaceTheRideButton = findViewById(R.id.id_But_PlaceThePickup)
        mGetMyLocationButton = findViewById(R.id.id_Float_But_GetMyLocation)

        mKey = getString(R.string.google_maps_key)
        mPickupCardView = findViewById(R.id.id_cardview_pickup)
        mDestinationCardView = findViewById(R.id.id_cardview_destination)

        mStateListAnimator = AnimatorInflater.loadStateListAnimator(mContext, R.animator.lift_on_touch)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, mKey)
        }
        mMapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mMapFragment.getMapAsync(this)
        mPlacesClient = Places.createClient(this)
        mFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

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

    }

    private fun assignTheLinks() {
        mPickupCardView.setOnClickListener {
            log("assignTheLinks: mPickupCardView Listener")
            openPickupAutocomplete()
        }

        mDestinationCardView.setOnClickListener {
            log("assignTheLinks(): mDestinationCardview Listener")
            openDestAutocomplete()
        }

        mGetMyLocationButton.setOnClickListener {
            log("assignTheLinks(): mGetMyLocationButton")
            getTheUserLocation()
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
        setupTheNavigationHeader()

        setupTheNavigationView()

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
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
            log("onBackPressed():Closed the drawer")
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_settings -> {
                makeToast("Clicked Settings")
                true
            }
            R.id.action_list -> {
                makeToast("Clicked List")
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_home -> {
                makeToast("Home Clicked")
            }
            R.id.nav_trips -> {
                makeToast("Trips Clicked")
            }
            R.id.nav_logout -> {
                log("onNavigationItemSelected():Logout Clicked: Logging out the user")
                logoutTheUser()

            }
        }
        mDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupTheNavigationHeader() {
        Glide.with(this)
            .load("https://avatars1.githubusercontent.com/u/20638539?s=460&v=4")
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    logError("setupTheNavigationHeader(): onLoadFailed: Unable to fetch the image")
                    logError("setupTheNavigationHeader():Error: $e")
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    log("setupTheNavigationHeader():OnResourceReady: Image Fetched Succesfully")
                    return false
                }

            })
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.ic_detective))
            .thumbnail(0.5f)
            .into(mThumbnailImageView)
    }

    private fun setupTheNavigationView() {

    }

    private fun logoutTheUser() {
        log("logoutTheUser():User Logged out Successfully")
        mAuth!!.signOut()
        log("logoutTheUser():Starting the LoginActivity")
        startTheActivity(LoginActivity::class.java)
    }

    private fun placeTheDirections() {
        if (mPickupMarker != null && mDestinationMarker != null) {
            makeToast("assignTheMethods(): Calculating Directions")
            calculateDirections(mPickupMarker, mDestinationMarker)

            val mPickupMarkerLatLng = LatLng(mPickupMarker!!.position.latitude, mPickupMarker!!.position.longitude)
            val mDestinationMarkerLatLng =
                LatLng(mDestinationMarker!!.position.latitude, mDestinationMarker!!.position.longitude)

            adjustTheCameraToBounds(mPickupMarkerLatLng, mDestinationMarkerLatLng)
        }
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
        Handler(Looper.getMainLooper()).post {
            log("addPolyLinesToTheMap(): Run: Result Routes: ${result.routes}")

            removeThePreviousPolylines()
            log("Routes: ${result.routes[0].legs[0].distance}")

            val route: DirectionsRoute = findTheShortestRoute(result.routes)

            log("addPolyLinesToTheMap(): The route is $route")

            val decodedPath: List<com.google.maps.model.LatLng> =
                PolylineEncoding.decode(route.overviewPolyline.encodedPath)

            val newDecodedPath: MutableList<LatLng> = ArrayList()

            for (latlng: com.google.maps.model.LatLng in decodedPath) {
                log("addPolyLinesToTheMap(): Run: ForLoop: latlng: $latlng")

                newDecodedPath.add(LatLng(latlng.lat, latlng.lng))
            }

            val polyline: Polyline = mMap.addPolyline(PolylineOptions().addAll(newDecodedPath))
            polyline.color = getColor(R.color.colorPrimary)
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

        val cameraUpdate: CameraUpdate = (CameraUpdateFactory.newLatLngBounds(latlngBounds, width, height, padding))
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
            else -> {
                logError("isGoogleServicesInstalled():User can't make the request")
                makeToast("isServicesOK():You can't make services request")
            }
        }
        return false
    }

    private fun isGpsEnabled(): Boolean {
        val manager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            logError("isGpsEnabled():GPS is not active")
            buildAlertMessageNoGps()
            return false
        }
        return true

    }

    private fun buildAlertMessageNoGps() {
        val alertBuilder: AlertDialog.Builder = AlertDialog.Builder(this)


        alertBuilder
            .setTitle("No Location Access")
            .setMessage("Without the GPS, we will not be able to find your location. Do you want to enable it?")
            .setNegativeButton("No") { _, _ ->
                log("buildAlertMessageNoGps():User clicked No")
            }
            .setPositiveButton("Yes") { _, _ ->
                //Here dialog and the marker are present
                val openSettingsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivityForResult(openSettingsIntent, PERMISSIONS_ENABLE_GPS_REQUEST)
            }

        val alertDialog: AlertDialog = alertBuilder.create()

        alertDialog.setOnCancelListener {
            alertDialog.dismiss()
        }

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorPrimary))
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorPrimary))

        }
        alertDialog.show()
    }

    override fun onPolylineClick(polyline: Polyline?) {
        for ((index, polylineData: PolylineData) in mPolylineDataList.withIndex()) {

            log("onPolylineClick(): $polylineData")

            if (polyline!!.id == polylineData.polyline.id) {
                polylineData.polyline.color = getColor(R.color.colorAccent)
                polylineData.polyline.zIndex = 1F

                val polylineLatLng = LatLng(polylineData.leg.endLocation.lat, polylineData.leg.endLocation.lng)

                placeMarkerWithData(polylineLatLng, index + 1, polylineData.leg.duration, polylineData.leg.distance)
            } else {
                polylineData.polyline.color = getColor(R.color.colorPrimary)
                polylineData.polyline.zIndex = 0F
            }
        }
    }

    private fun getTheUserLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = false
            logError("getTheUserLocation(): Unable to assign the permissions")
            makeToast("Please provide the permission to make the  application work")
        }
        if (!isGpsEnabled()) {
            logError("getTheUserLocation():GPs is unabled")
            makeToast("Please enable the GPS to find your location")
        } else {
            mMap.isMyLocationEnabled = true

            mFusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->
                if (location != null) {
                    mLastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
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

                    val address: String = place.address.toString()
                    mPickupAddressText.text = address


                    updateTheCamera(place.latLng, 12F)
                    placePickupMarker(place.latLng)

                    if (mPickupMarker != null && mDestinationMarker != null) {
                        removePolylinesPresent()
                    }
                    makeToast("onActivityResult(): Pickup: Location is" + place.latLng)
                    placeTheDirections()
                }
                RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    logError("onActivityResult(): Pickup: Status: " + status.statusMessage)
                }
                RESULT_CANCELED -> logError("onActivityResult(): Pickup: User Cancelled the operation")
            }
        }

        if (requestCode == mDestinationRequestCode) {
            when (resultCode) {
                RESULT_OK -> {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    makeToast("Place:" + place.name + place.id)

                    val address: String = place.address.toString()
                    mDestinationAddressText.text = address


                    updateTheCamera(place.latLng, 12F)
                    placeDestinationMarker(place.latLng)

                    if (mPickupMarker != null && mDestinationMarker != null) {
                        removePolylinesPresent()
                    }

                    log(" onActivityResult(): Destination: Location is" + place.latLng)
                    placeTheDirections()

                }
                RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    logError(" onActivityResult(): Destination: Status:" + status.statusMessage)
                    makeToast("Error")
                }

                RESULT_CANCELED -> {
                    logError("onActivityResult(): Destination: User Cancelled the operation")
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
            var address: String = getAddressFromLocation(mCenterLatLng.latitude, mCenterLatLng.longitude)
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(mContext, Locale.ENGLISH)

        try {

            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
            log("getAddressFromLocation():The addresses are $addresses")

            return if (addresses.isNotEmpty()) {
                val fetchedAddress: Address = addresses[0]
                log("getAddressFromLocation():The fetched address is $fetchedAddress")

                val strAddress: StringBuilder = StringBuilder()
                strAddress.append(fetchedAddress.getAddressLine(0)).append(" ")
                log("getAddressFromLocation(): The address is $strAddress")

                strAddress.toString()

            } else {
                log("getAddressFromLocation(): searching the current address")
                getString(R.string.searching_address)
            }

        } catch (error: IOException) {
            error.stackTrace
            logError("getAddressFromLocation():IOException: Error: $error")
            makeToast("Could Not Get Address $error")
            return "Could Not Get the Address"
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

    private fun placeMarkerWithData(latLng: LatLng?, index: Int, duration: Duration, distance: Distance) {
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
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    private fun startTheActivity(mClass: Class<*>) {
        log("startTheActivity(): ${mClass.simpleName}.class Activity")
        val intent = Intent(mContext, mClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
    }

    override fun onResume() {
        super.onResume()
        if (checkMapServices()) {
            placeTheDirections()
            log("onResume(): GPS Enabled")
        }
    }

    override fun onStart() {
        super.onStart()
        log("onStart():Starting the Activity")
    }
}
