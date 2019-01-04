package com.medsdate

/**
 * Icon credits Smashicons, Good Ware, smalllikeart, Freepik from flaticon.com
 */

import android.app.Application

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.facebook.stetho.Stetho
import com.medsdate.data.db.AppDatabase
import com.medsdate.data.db.MedsRepository
import com.medsdate.utils.AppExecutors

import io.fabric.sdk.android.Fabric
import timber.log.Timber

class BasicApp : Application() {

    private var mAppExecutors: AppExecutors? = null

    val database: AppDatabase
        get() = AppDatabase.getInstance(this, mAppExecutors)

    val repository: MedsRepository
        get() = MedsRepository.getInstance(database)

    override fun onCreate() {
        super.onCreate()

        val core = CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build()
        Fabric.with(this, Crashlytics.Builder().core(core).build())
        Stetho.initializeWithDefaults(this)
        Timber.plant(Timber.DebugTree())
        mAppExecutors = AppExecutors.getInstance()
    }
}
