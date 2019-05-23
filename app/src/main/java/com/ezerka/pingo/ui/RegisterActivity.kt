package com.ezerka.pingo.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.ezerka.pingo.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class RegisterActivity : AppCompatActivity() {

    //Constant Variables
    private val TAG: String = "RegisterActivity: "
    private lateinit var mContext: Context

    //Normal Variables
    private lateinit var mEmailRegisterET: EditText
    private lateinit var mPassRegisterET: EditText
    private lateinit var mMobileRegisterET: EditText
    private lateinit var mRegisterButton: Button
    private lateinit var mBackToLoginText: TextView
    private lateinit var mRegisterProgressBar: ProgressBar

    //Firebase Variables
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mUser: FirebaseUser? = null
    private var mDatabase: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        assignTheViews()
        assignTheLinks()
    }

    private fun assignTheViews() {
        mEmailRegisterET = findViewById(R.id.id_ET_Register_Email)
        mPassRegisterET = findViewById(R.id.id_ET_Register_Pass)
        mMobileRegisterET = findViewById(R.id.id_ET_Register_Mobile)

        mRegisterButton = findViewById(R.id.id_But_Register_User)

        mBackToLoginText = findViewById(R.id.id_Text_BackToLogin)

        mRegisterProgressBar = findViewById(R.id.id_PB_Register)

        mContext = applicationContext

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser

        mDatabase = FirebaseFirestore.getInstance()
    }

    private fun assignTheLinks() {
        mRegisterButton.setOnClickListener {
            log("assignTheLinks(): mRegisterButton: Registering the user")
            registerTheUser()
        }

        mBackToLoginText.setOnClickListener {
            log("assignTheLinks():mBackToLoginText: Starting the LoginActivity")
            startTheActivity(LoginActivity::class.java)
        }
    }


    private fun registerTheUser() {
        showLoadingBar("RegisterTheUser")

        val sEmail: String = mEmailRegisterET.text.toString().trim()
        val sPass: String = mPassRegisterET.text.toString().trim()
        val sMobile: String = mMobileRegisterET.text.toString().trim()


        if (checkForErrors(sEmail, sPass, sMobile)) {

            mAuth!!.createUserWithEmailAndPassword(sEmail, sPass).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    closeLoadingBar("registerTheUser()")
                    log("registerTheUser: Task Successful: User Registered: ${mAuth!!.uid}")

                    mAuth!!.signOut()
                    log("registerTheUser: Signing out the user.")
                    startTheActivity(LoginActivity::class.java)
                    makeToast("Registered  Successfully ")
                } else {
                    closeLoadingBar("registerTheUser: Failure Listener")
                    logError("registerTheUser(): Task Unsuccessful: ${task.exception}")
                    makeToast("Unable to Register the user, Please Try Again.")
                }
            }

        }

    }

    private fun storeTheDataOnDB() {
        val userId: String = mAuth!!.currentUser!!.uid

        val userDetails = HashMap<String, Any>()
        userDetails["Email Id"] = "Ashfaq"
        userDetails["Password"] = "Hello world"
        userDetails["Mobile"] = "8328277518"

        mDatabase!!.collection("Users")
            .document("drivers")
            .collection(userId)
            .add(userDetails)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    log("storeTheDataOnDB():Task Success(): Data has been successfully stored ")
                    makeToast("Data Stored Successfully")
                } else {
                    logError("storeTheDataOnDB():Task Failed: Unable to store the data " + task.exception)
                    makeToast("Error: " + task.exception.toString())
                }
            }
    }

    private fun checkForErrors(Email: String, Pass: String, Mobile: String): Boolean {
        if (Email.isEmpty()) {
            mEmailRegisterET.error = "Please Enter The Email Id"
            return false
        }

        if (Pass.isEmpty()) {
            mPassRegisterET.error = "Please, Enter The Password"
            return false
        }

        if (Mobile.isEmpty()) {
            mMobileRegisterET.error = "Please, Enter the Mobile"
            return false
        }
        return true
    }

    private fun log(log: String) {
        Log.v(TAG, "Log: $log")
    }

    private fun logError(error: String) {
        Log.e(TAG, "Log Error: $error")
    }

    private fun startTheActivity(mClass: Class<*>) {
        log("startTheActivity(): Starting the ${mClass.simpleName}.class Activity")
        val intent = Intent(mContext, mClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
        finish()
    }

    private fun showLoadingBar(method: String) {
        log("showLoadingBar(): $method")
        mRegisterProgressBar.visibility = View.VISIBLE
    }

    private fun closeLoadingBar(method: String) {
        log("closeLoadingBar(): $method")
        mRegisterProgressBar.visibility = View.GONE
    }


    private fun makeToast(toast: String) {
        log("Toast: $toast")
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        log("onStart(): Init")
    }

    override fun onStop() {
        super.onStop()
        log("onStop(): Init")
    }

}
