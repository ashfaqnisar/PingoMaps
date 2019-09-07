package com.ezerka.pingo.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.ezerka.pingo.R
import com.ezerka.pingo.models.UserData
import com.ezerka.pingo.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber


class RegisterActivity : AppCompatActivity() {

    //Constant Variables
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

    private lateinit var mUserData: UserData

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

        mUserData = UserData()
    }

    private fun assignTheLinks() {
        mRegisterButton.setOnClickListener {
            log("assignTheLinks(): mRegisterButton: Registering the user")
            registerTheUser()
        }

        mBackToLoginText.setOnClickListener {
            log("assignTheLinks():mBackToLoginText: Starting the LoginActivity")
            startTheActivity(LoginActivity::class.java,mContext)
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
                    log("registerTheUser: Task Successful: User Registered: ${mAuth!!.uid}")
                    storeTheDataOnDB()
                    makeToast("Registered  Successfully ",mContext)
                } else {
                    closeLoadingBar("registerTheUser: Failure Listener")
                    logError("registerTheUser(): Task Unsuccessful: ${task.exception}")
                    makeToast("Unable to Register the user, Please Try Again.",mContext)
                }
            }

        }

    }

    private fun storeTheDataOnDB() {
        log("storeTheDataOnDB():init")
        val userId: String = mAuth!!.currentUser!!.uid

        /*val userDetails = HashMap<String, Any>()
        userDetails["avatar"] = ""
        userDetails["name"] = "Ashfaq"
        userDetails["email"] = mEmailRegisterET.text.toString().trim()
        userDetails["user_id"] = userId.trim()
        userDetails["mobile"] = mMobileRegisterET.text.toString().trim()*/

        mUserData.name = "Ashfaq"
        mUserData.email = mEmailRegisterET.text.toString().trim()
        mUserData.user_id = userId
        mUserData.mobile = mMobileRegisterET.text.toString().trim()


        val userRef = mDatabase!!.collection("Users").document(userId)

        userRef.set(mUserData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                closeLoadingBar("storeTheDataOnDB()")
                log("storeTheDataOnDB():Task Success(): Data has been successfully stored ")
                makeToast("Data Stored Successfully",mContext)
                mAuth!!.signOut()
                log("registerTheUser: Signing out the user.")
                startTheActivity(LoginActivity::class.java,mContext)

            } else {
                logError("storeTheDataOnDB():Task Failed: Unable to store the data " + task.exception)
                makeToast("Error: " + task.exception.toString(),mContext)
            }
        }

    }

    private fun checkForErrors(Email: String, Pass: String, Mobile: String): Boolean {
        closeLoadingBar("checkForErrors()")
        if (Email.isEmpty()) {
            mEmailRegisterET.error = "Please Enter The Email Id"
            return false
        }

        if (Pass.isEmpty()) {
            mPassRegisterET.error = "Please, Enter The Password"
            return false
        }

        if (Mobile.isEmpty() && Mobile.toInt() <= 10) {
            mMobileRegisterET.error = "Please, Enter the Mobile"
            return false
        }
        return true
    }


    private fun showLoadingBar(method: String) {
        log("showLoadingBar(): $method")
        mRegisterProgressBar.visibility = View.VISIBLE
    }

    private fun closeLoadingBar(method: String) {
        log("closeLoadingBar(): $method")
        mRegisterProgressBar.visibility = View.GONE
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
