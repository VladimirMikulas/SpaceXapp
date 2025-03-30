package com.vlamik.spacex

import android.app.Application
import android.os.StrictMode
import com.vlamik.core.commons.Log.addLogger
import com.vlamik.spacex.utils.AndroidLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        setupLogger()
        setupStrictMode()
    }

    private fun setupStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
//                    .penaltyDeath()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }

    private fun setupLogger() {
        addLogger(AndroidLogger())
    }
}
