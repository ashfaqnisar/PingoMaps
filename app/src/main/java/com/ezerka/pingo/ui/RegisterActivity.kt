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
    private var mContext: Context? = null

    //Normal Variables
    private var mEmailRegisterET: EditText? = null
    private var mPassRegisterET: EditText? = null
    private var mMobileRegisterET: EditText? = null
    private var mRegisterButton: Button? = null
    private var mBackToLoginText: TextView? = null
    private var mRegisterProgressBar: ProgressBar? = null

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

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            mUser = firebaseAuth.currentUser
            if (mUser != null) {
                log("ModelUser is signed in with " + mUser!!.uid)
            } else {
                log("ModelUser is signed out")
            }
        }

        mDatabase = FirebaseFirestore.getInstance()
    }

    private fun assignTheLinks() {
        mRegisterButton!!.setOnClickListener {
            log("Registering the user")
            registerTheUser()
        }

        mBackToLoginText!!.setOnClickListener {
            log("Starting The Login Activity")
            startTheActivity(LoginActivity::class.java)
        }
    }


    private fun registerTheUser() {
        showLoadingBar("RegisterTheUser")

        val sEmail: String = mEmailRegisterET!!.text.toString().trim()
        val sPass: String = mPassRegisterET!!.text.toString().trim()
        val sMobile: String = mMobileRegisterET!!.text.toString().trim()


        if (checkForErrors(sEmail, sPass, sMobile)) {

            mAuth!!.createUserWithEmailAndPassword(sEmail, sPass).addOnCompleteListener { Task ->
                if (Task.isSuccessful) {

                    closeLoadingBar("registerTheUser: Successfull listener")
                    log("Successfully Registered the user: " + mAuth!!.uid)

                    mAuth!!.signOut()

                    startTheActivity(LoginActivity::class.java)
                    makeToast("Registered  Successfully ")
                } else {
                    closeLoadingBar("registerTheUser: Failure Listener")
                    makeToast("Unable to Register the user, Please Try Again, " + Task.exception.toString())
                    logError("Error: " + Task.exception.toString())
                }
            }

        }

    }

    private fun storeTheDataOnDB() {
        val user_id: String = mAuth!!.currentUser!!.uid

        val user_details = HashMap<String, Any>()
        user_details["Email Id"] = "Ashfaq"
        user_details["Password"] = "Helloworld"
        user_details["Mobile"] = "8328277518"

        mDatabase!!.collection("Users")
            .document("drivers")
            .collection(user_id)
            .add(user_details)
            .addOnCompleteListener { Task ->
                if (Task.isSuccessful) {
                    log("Data has been successfully stored ")
                    makeToast("Data Stored Successfully")
                } else {
                    logError("Unable to store the data " + Task.exception)
                    makeToast("Error: " + Task.exception.toString())
                }
            }
    }

    private fun checkForErrors(Email: String, Pass: String, Mobile: String): Boolean {
        if (Email.isEmpty()) {
            mEmailRegisterET!!.error = "Please Enter The Email Id"
            return false
        }

        if (Pass.isEmpty()) {
            mPassRegisterET!!.error = "Please, Enter The Password"
            return false
        }

        if (Mobile.isEmpty()) {
            mMobileRegisterET!!.error = "Please, Enter the Mobile"
            return false
        }
        return true
    }

    private fun log(log: String) {
        Log.d(TAG, log)
    }

    private fun logError(error: String) {
        Log.w(TAG, error)
    }

    private fun startTheActivity(mClass: Class<*>) {
        log("Starting the $mClass.class Activity")
        val intent = Intent(mContext, mClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        log("Opened the $mClass.class Activity")
        finish()
    }

    private fun showLoadingBar(method: String) {
        log("Loading Bar has been started by $method")
        mRegisterProgressBar!!.visibility = View.VISIBLE
    }

    private fun closeLoadingBar(method: String) {
        log("Loading Bar has been closed by $method")
        mRegisterProgressBar!!.visibility = View.GONE
    }

    private fun makeToast(toast: String) {
        log("Making a toast of $toast")
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener { mAuthListener }
    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener { mAuthListener }
    }

}
