package com.ezerka.pingo.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.ezerka.pingo.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import timber.log.Timber


class LoginActivity : AppCompatActivity() {

    //Constant Variables
    private lateinit var mContext: Context

    //Normal Variables
    private lateinit var mEmailET: EditText
    private lateinit var mPassET: EditText
    private lateinit var mLoginButton: Button
    private lateinit var mLoginRegisterButton: Button
    private lateinit var mSkipLoginText: TextView
    private lateinit var mGoogleSignInButton: SignInButton
    private lateinit var mLoginProgressBar: ProgressBar

    //Firebase Variables
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mUser: FirebaseUser? = null

    //Google Variables
    private var mGoogleClient: GoogleSignInClient? = null
    private val mGoogleAuthRequestCode: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        assignTheViews()
        assignTheLinks()
    }


    private fun assignTheViews() {
        mEmailET = findViewById(R.id.id_ET_Login_Email)
        mPassET = findViewById(R.id.id_ET_Login_Pass)

        mLoginButton = findViewById(R.id.id_But_Login_User)
        mLoginRegisterButton = findViewById(R.id.id_But_Login_GoToRegister)
        mGoogleSignInButton = findViewById(R.id.id_But_Login_Google)

        mSkipLoginText = findViewById(R.id.id_Text_SkipLogin)

        mLoginProgressBar = findViewById(R.id.id_PB_Login)

        mContext = applicationContext

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

        val mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.id_token_google))
            .requestEmail()
            .build()

        mGoogleClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)

    }

    private fun assignTheLinks() {
        mLoginButton.setOnClickListener {
            log("mLoginButton: OnClick: Logging in the user")
            loginTheUser()
        }

        mLoginRegisterButton.setOnClickListener {
            log("mLoginRegisterButton: OnClick: Starting The Register Activity")
            startTheActivity(RegisterActivity::class.java)
        }

        mSkipLoginText.setOnClickListener {
            log("mSkipLoginText: OnClick: Starting The Permissions Activity")
            startTheActivity(MainActivity::class.java)
        }
        mGoogleSignInButton.setOnClickListener {
            log("mGoogleSignInButton: OnClick: Logging in the user")
            showLoadingBar("mGoogleSignInButton")
            firebaseGoogleLogIn()
        }
    }


    private fun firebaseGoogleLogIn() {
        val signInIntent: Intent = mGoogleClient!!.signInIntent
        startActivityForResult(signInIntent, mGoogleAuthRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == mGoogleAuthRequestCode) {
            val task: Task<GoogleSignInAccount>? = GoogleSignIn.getSignedInAccountFromIntent(data)

            log("onActivityResult(): mGoogleAuthRequestCode: Init")
            try {
                val mAccount = task!!.getResult(ApiException::class.java)
                log("onActivityResult(): mGoogleAuthRequestCode: Fetched the Account from the intent")
                firebaseAuthWithGoogle(mAccount!!)
            } catch (e: ApiException) {
                logError("Error: $e")
                closeLoadingBar("handleSignInResult()")
                makeToast("Unable to fetch the account")
            }
        }
    }


    private fun firebaseAuthWithGoogle(mAccount: GoogleSignInAccount) {

        val mCredential = GoogleAuthProvider.getCredential(mAccount.idToken, null)

        mAuth!!.signInWithCredential(mCredential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                closeLoadingBar("firebaseAuthWithGoogle()")
                log("firebaseAuthWithGoogle():signWithCredential(): User is successfully stored")
                makeToast("User is successfully signed in!! ")
                startTheActivity(MainActivity::class.java)
            } else {
                closeLoadingBar("firebaseAuthWithGoogle()")
                makeToast("Unable to sign in the user, Please try again")
                logError("firebaseAuthWithGoogle(): Error: " + task.exception)
            }
        }
    }


    private fun loginTheUser() {
        showLoadingBar("loginTheUser")
        val sEmail: String = mEmailET.text.toString().trim()
        val sPass: String = mPassET.text.toString().trim()

        if (checkForErrors(sEmail, sPass)) {
            mAuth!!.signInWithEmailAndPassword(sEmail, sPass).addOnCompleteListener { Task ->
                if (Task.isSuccessful) {
                    closeLoadingBar("loginTheUser: signInWithEmailAndPassword(): Successful")
                    log("Successfully Logged In with user: " + mAuth!!.uid)
                    makeToast("Signed In  Successfully ")

                    startTheActivity(MainActivity::class.java)
                } else {
                    closeLoadingBar("loginTheUser: signInWithEmailAndPassword(): Failure Listener")
                    makeToast("Unable to Sign in, Please Try Again " + Task.exception.toString())
                    logError("Error: " + Task.exception.toString())
                }
            }

        }

    }

    private fun checkForErrors(Email: String, Pass: String): Boolean {
        if (Email.isEmpty()) {
            closeLoadingBar("checkForErrors")
            mEmailET.error = "Please Enter The Email Id"
            return false
        }

        if (Pass.isEmpty()) {
            closeLoadingBar("checkForErrors")
            mPassET.error = "Please, Enter The Password"
            return false
        }
        return true
    }

    private fun checkForAlreadySignedInUser() {
        if (mUser != null) {
            log("checkForAlreadySignedInUser():User is logged in with Id: ${mUser!!.uid}, ${mUser!!.displayName}")
            makeToast("User Has Been Already Logged In")
            startTheActivity(MainActivity::class.java)
        }

    }

    private fun log(log: String) {
        Timber.d("Log: $log")
    }

    private fun logError(error: String) {
        Timber.e("Log Error: $error")
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
        mLoginProgressBar.visibility = View.VISIBLE
    }

    private fun closeLoadingBar(method: String) {
        log("closeLoadingBar(): $method")
        mLoginProgressBar.visibility = View.GONE
    }

    private fun makeToast(toast: String) {
        log("Toast: $toast")
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener { mAuthListener }
        checkForAlreadySignedInUser()
    }

    override fun onStop() {
        super.onStop()
        mAuth!!.removeAuthStateListener { mAuthListener }
    }

}
