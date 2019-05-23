package com.ezerka.pingo.other

import android.util.Log
import timber.log.Timber

class TimberReleaseTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG) {
            return
        }
    }

}