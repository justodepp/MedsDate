package com.medsdate

import android.app.Application
import com.google.firebase.FirebaseApp
import com.medsdate.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

/**
 * Application class for MedsDate.
 *
 * Initializes app-wide dependencies including:
 * - Koin dependency injection
 * - Timber logging
 * - Firebase
 */
class MedsDateApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Koin for dependency injection
        startKoin {
            // Log Koin into Android logger
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)

            // Reference Android context
            androidContext(this@MedsDateApplication)

            // Setup WorkManager factory for Koin
            workManagerFactory()

            // Load modules
            modules(appModules)
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        Timber.d("MedsDateApplication initialized with Koin DI")
    }
}
