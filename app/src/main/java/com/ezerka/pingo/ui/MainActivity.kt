package com.ezerka.pingo.ui

//Normal Imports

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import com.ezerka.pingo.fragments.*
import com.ezerka.pingo.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener, NavHomeFragment.OnFragmentInteractionListener,
    NavTripsFragment.OnFragmentInteractionListener, TripsHistoryFragment.OnFragmentInteractionListener,
    TripsUpcomingFragment.OnFragmentInteractionListener {

    private var mNavViewItemIndex: Int = 0
    private lateinit var mNavTitleArray: Array<String>
    private var TAG_HOME: String = "Home"
    private var TAG_TRIPS: String = "Trips"
    private var TAG_PAYMENTS: String = "Payment"
    private var TAG_NOTIFICATIONS: String = "Notifications"
    private var TAG_SETTINGS: String = "Settings"
    private var TAG_SUPPORT: String = "Support"
    private lateinit var mNavActivityTag: String

    private lateinit var mHandler: Handler
    private lateinit var mActiveFragment: Fragment
    private lateinit var mContext: Context
    private lateinit var mUserData: UserData

    //Normal Variables
    private lateinit var mThumbnailImageView: ImageView
    private lateinit var mNameTextView: TextView
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mNavigationView: NavigationView
    private lateinit var mNavigationHeader: View
    private lateinit var mToggle: ActionBarDrawerToggle
    private lateinit var mToolBar: Toolbar

    //Firebase Variables
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mUser: FirebaseUser? = null
    private lateinit var mDatabase: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        assignTheViews()
        assignTheLinks()
        assignTheMethods()

        if (savedInstanceState == null) {
            mNavViewItemIndex = 0
            mNavActivityTag = TAG_HOME
            loadTheFetchedFragment()
        }
    }

    private fun assignTheViews() {
        mContext = applicationContext

        mToolBar = findViewById(R.id.id_Toolbar_MainActivity)
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
        mNameTextView = mNavigationHeader.findViewById(R.id.id_Text_Username)
        mHandler = Handler()

        mActiveFragment = getActiveFragment()

        mNavTitleArray = resources.getStringArray(R.array.nav_item_activity_titles)

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

        mUserData = UserData()

        mDatabase = FirebaseFirestore.getInstance()

    }

    private fun assignTheLinks() {


    }

    private fun assignTheMethods() {
        fetchUserData()
    }


    override fun onFragmentInteraction(uri: Uri) {

    }


    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
            log("onBackPressed():Closed the drawer")
        }
        if (mNavViewItemIndex != 0) {
            mNavViewItemIndex = 0
            mNavActivityTag = TAG_HOME
            loadTheFetchedFragment()
            return
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

                mNavViewItemIndex = 0
                mNavActivityTag = TAG_HOME
                checkTheFragmentIsActive(menuItem, TAG_HOME)
                log("Home Clicked")
            }

            R.id.nav_trips -> {
                log("onNavigationItemSelected():Trips Clicked:")
                mNavViewItemIndex = 1
                mNavActivityTag = TAG_TRIPS

                checkTheFragmentIsActive(menuItem, TAG_TRIPS)

                log("Trips Clicked")
            }

            R.id.nav_payments -> {
                log("onNavigationItemSelected():Payments Clicked:")
                mNavViewItemIndex = 2
                mNavActivityTag = TAG_PAYMENTS
                checkTheFragmentIsActive(menuItem, TAG_PAYMENTS)

            }

            R.id.nav_notifications -> {
                log("onNavigationItemSelected():Notifications Clicked:")
                mNavViewItemIndex = 3
                mNavActivityTag = TAG_NOTIFICATIONS
                checkTheFragmentIsActive(menuItem, TAG_NOTIFICATIONS)


            }

            R.id.nav_settings -> {
                log("onNavigationItemSelected():Settings Clicked:")
                mNavViewItemIndex = 4
                mNavActivityTag = TAG_SETTINGS
                checkTheFragmentIsActive(menuItem, TAG_SETTINGS)

            }

            R.id.nav_support -> {
                log("onNavigationItemSelected():Support  Clicked:")
                mNavViewItemIndex = 5
                mNavActivityTag = TAG_SUPPORT
                checkTheFragmentIsActive(menuItem, TAG_SUPPORT)

            }

            R.id.nav_logout -> {
                log("onNavigationItemSelected():Logout Clicked: Logging out the user")
                logoutTheUser()
            }

            else -> {
                log("onNavigationItemSelected():Default : User To Home")
                mNavViewItemIndex = 0
                mNavActivityTag = TAG_HOME
                checkTheFragmentIsActive(menuItem, TAG_HOME)

            }

        }

        mDrawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun checkTheFragmentIsActive(menuItem: MenuItem, TAG: String) {
        if (menuItem.isChecked) {
            log("onNavigationItemSelected():$TAG :It is already enabled")
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START)
                log("onBackPressed():Closed the drawer")
            }
        } else {
            log("It is not enabled")
            log("onNavigationItemSelected():$TAG Clicked:")
            loadTheFetchedFragment()
        }

    }

    private fun loadTheFetchedFragment() {
        mNavigationView.menu.getItem(mNavViewItemIndex).isChecked = true

        supportActionBar!!.title = mNavTitleArray[mNavViewItemIndex]

        if (supportFragmentManager.findFragmentByTag(mNavActivityTag) != null) {//Same menu is selected, close the drawers
            mDrawerLayout.closeDrawers()
        }

        val mRunnable = Runnable {
            val fragment = getActiveFragment()
            val fragmentTransaction = supportFragmentManager.beginTransaction()

            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.id_Frame_NavigationView, fragment, mNavActivityTag)
            fragmentTransaction.commitAllowingStateLoss()
        }

        mHandler.post(mRunnable)

        mDrawerLayout.closeDrawers()

        invalidateOptionsMenu()

    }

    private fun getActiveFragment(): Fragment {
        log("getActiveFragment():init")

        return when (mNavViewItemIndex) {
            0 -> {
                NavHomeFragment()
            }

            1 -> {
                NavTripsFragment()
            }

            2 -> {
                NavPaymentFragment()
            }

            3 -> {
                NavNotificationsFragment()
            }

            4 -> {
                NavSettingsFragment()
            }

            5 -> {
                NavSupportFragment()
            }

            else -> {
                NavHomeFragment()
            }
        }
    }

    private fun fetchUserData() {
        val ref = mDatabase.collection("Users").document(FirebaseAuth.getInstance().uid!!)
        log("fetchUserData():FirebaseAuth.getInstance().toString()")
        ref.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                log("fetchUserDetails():OnComplete:Success")

                val fetchedUser: UserData = task.result?.toObject(UserData::class.java) as UserData
                log("fetchedUserData: $fetchedUser")
                mUserData = fetchedUser
                setupTheNavigationHeader()
            } else {
                logError("fetchUserDetails: Error: {$task.exception}")
            }
        }
    }



    private fun setupTheNavigationHeader() {
        Glide.with(this)
            .load(mUserData.avatar.toString())
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
                    log("setupTheNavigationHeader():OnResourceReady: Image Fetched Successfully")
                    return false
                }

            })
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.ic_detective))
            .thumbnail(0.5f)
            .into(mThumbnailImageView)
        log("Glide: ${mUserData.avatar.toString()}")
        mNavigationView.menu.getItem(3).setActionView(R.layout.comp_dot) //Placing dot on the notifications fragment

        mNameTextView.text = mUserData.name
    }


    private fun logoutTheUser() {
        log("logoutTheUser():User Logged out Successfully")
        mAuth!!.signOut()
        log("logoutTheUser():Starting the LoginActivity")
        startTheActivity(LoginActivity::class.java)
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
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
    }

    override fun onResume() {
        super.onResume()
        log("onResume():init")
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    override fun onStart() {
        super.onStart()
        log("onStart():Starting the Activity")
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy(): Activity Destroy")
        mAuth!!.removeAuthStateListener(mAuthListener!!)
    }
}