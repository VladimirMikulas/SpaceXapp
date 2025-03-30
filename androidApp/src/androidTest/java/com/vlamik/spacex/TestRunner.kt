package com.vlamik.spacex

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.vlamik.core.commons.Log.addLogger
import com.vlamik.spacex.utils.AndroidLogger
import dagger.hilt.android.testing.HiltTestApplication

class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        addLogger(AndroidLogger())
        return super.newApplication(cl, HiltTestApplication::class.java.canonicalName, context)
    }
}
