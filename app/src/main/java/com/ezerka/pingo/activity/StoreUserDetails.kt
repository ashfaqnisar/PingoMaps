package com.ezerka.pingo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ezerka.pingo.R
import com.ezerka.pingo.models.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber


class StoreUserDetails : AppCompatActivity() {

    //Constant Variables
    private lateinit var mContext: Context

    //Normal Variables
    private lateinit var mNameUserET: EditText
    private lateinit var mEmailUserET: EditText
    private lateinit var mMobileUserET: EditText
    private lateinit var mFinishButton: Button
    private lateinit var mStoreUserProgressBar: ProgressBar

    //Firebase Variables
    private lateinit var mAuth: FirebaseAuth
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mUser: FirebaseUser? = null
    private lateinit var mDatabase: FirebaseFirestore

    private lateinit var mUserData: UserData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        assignTheViews()
        assignTheLinks()
    }

    private fun assignTheViews() {
        mNameUserET = findViewById(R.id.id_ET_Store_User_Name)
        mEmailUserET = findViewById(R.id.id_ET_Store_User_Email)
        mMobileUserET = findViewById(R.id.id_ET_Store_User_Mobile)

        mFinishButton = findViewById(R.id.id_But_Store_User_Details)

        mStoreUserProgressBar = findViewById(R.id.id_PB_Store_User)

        mContext = applicationContext

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth.currentUser

        mDatabase = FirebaseFirestore.getInstance()

        mUserData = UserData()
    }

    private fun assignTheLinks() {
        mFinishButton.setOnClickListener {
            log("assignTheLinks(): mFinishButton: Registering the user")
            storeTheUserInfo()
        }
    }


    private fun storeTheUserInfo() {
        showLoadingBar("storeTheUserInfo()")

        val sEmail: String = mEmailUserET.text.toString().trim()
        val sName: String = mNameUserET.text.toString().trim()
        val sMobile: String = mMobileUserET.text.toString().trim()

        if (checkForErrors(sEmail, sName, sMobile)) {
            storeTheDataOnDB(sEmail, sName, sMobile)
        }

    }

    private fun storeTheDataOnDB(sEmail: String, sName: String, sMobile: String) {
        log("storeTheDataOnDB():init")
        val userId: String = mAuth.currentUser?.uid?:throw NullPointerException()

        /*val userDetails = HashMap<String, Any>()
        userDetails["avatar"] = ""
        userDetails["name"] = "Ashfaq"
        userDetails["email"] = mEmailRegisterET.text.toString().trim()
        userDetails["user_id"] = userId.trim()
        userDetails["mobile"] = mMobileUserET.text.toString().trim()*/

        mUserData.user_id = userId
        mUserData.name = sName
        mUserData.email = sEmail
        mUserData.mobile = sMobile

        val userRef = mDatabase.collection("Users").document(userId)

        userRef.set(mUserData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                closeLoadingBar("storeTheDataOnDB()")
                log("storeTheDataOnDB():Task Success(): Data has been successfully stored ")
                makeToast("Data Stored Successfully")
                startTheActivity(MainActivity::class.java)

            } else {
                logError("storeTheDataOnDB():Task Failed: Unable to store the data " + task.exception)
                makeToast("Error: " + task.exception.toString())
            }
        }

    }

    private fun checkForErrors(Email: String, Name: String, Mobile: String): Boolean {
        closeLoadingBar("checkForErrors()")
        if (Email.isEmpty()) {
            mEmailUserET.error = "Please Enter The Email Id"
            return false
        }

        if (Name.isEmpty()) {
            mNameUserET.error = "Please, Enter The Name"
            return false
        }

        if (Mobile.isEmpty() && Mobile.toInt() <= 10) {
            mMobileUserET.error = "Please, Enter the Mobile"
            return false
        }
        return true
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

    private fun showLoadingBar(method: String) {
        log("showLoadingBar(): $method")
        mStoreUserProgressBar.visibility = View.VISIBLE
    }

    private fun closeLoadingBar(method: String) {
        log("closeLoadingBar(): $method")
        mStoreUserProgressBar.visibility = View.GONE
    }

    override fun onStart() {
        super.onStart()
        log("onStart(): Init")
    }

    override fun onStop() {
        super.onStop()
        mAuth?.removeAuthStateListener { mAuthListener!! }
        log("onStop(): Init")
    }

}
