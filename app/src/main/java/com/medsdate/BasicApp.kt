package com.medsdate

/**
 * Icon credits Smashicons, Good Ware, smalllikeart, Freepik from flaticon.com
 */

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class BasicApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val core = CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build()
        //Fabric.with(this, Crashlytics.Builder().core(core).build())
        Stetho.initializeWithDefaults(this)
        Timber.plant(Timber.DebugTree())
    }
}
