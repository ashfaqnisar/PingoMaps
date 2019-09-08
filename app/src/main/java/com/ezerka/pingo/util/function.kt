package com.ezerka.pingo.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import timber.log.Timber


fun log(log: String) {
    Timber.d("Log: $log")
}

fun logError(error: String) {
    Timber.e("Log Error: $error")
}

fun makeToast(toast: String, context: Context?) {
    log("Toast: $toast")
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}

fun startTheActivity(mClass: Class<*>, context: Context?) {
    log("startTheActivity(): ${mClass.simpleName}.class Activity")
    val intent = Intent(context, mClass)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context?.startActivity(intent)
    log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
}

private fun startTheActivitywithClearFlag(mClass: Class<*>, context: Context) {
    log("startTheActivity(): ${mClass.simpleName}.class Activity")
    val intent = Intent(context, mClass)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
    log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
}

fun startTheActivity(mClass: Class<*>, context: Context, withIntent: Boolean) {
    log("startTheActivity(): ${mClass.simpleName}.class Activity")
    val intent = Intent(context, mClass)
    context.startActivity(intent)
    log("startTheActivity(): Opened the ${mClass.simpleName}.class Activity")
}
