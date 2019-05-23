package com.ezerka.pingo.other

import android.app.Application
import com.ezerka.pingo.BuildConfig
import timber.log.Timber

class TimberMainTree : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(TimberReleaseTree())
        }
    }
}