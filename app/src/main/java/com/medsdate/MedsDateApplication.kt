package com.medsdate

import android.app.Application
import com.google.firebase.FirebaseApp
import com.medsdate.data.local.AppDatabase
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.data.repository.SettingsRepository
import timber.log.Timber

/**
 * Application class for MedsDate.
 *
 * Initializes app-wide dependencies including:
 * - Timber logging
 * - Firebase
 * - Room database
 * - Repositories
 */
class MedsDateApplication : Application() {

    // Database instance
    private val database by lazy { AppDatabase.getInstance(this) }

    // Repositories
    val medicineRepository by lazy { MedicineRepository(database.medicineDao()) }
    val settingsRepository by lazy { SettingsRepository(database.settingsDao()) }

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        Timber.d("MedsDateApplication initialized")
    }
}
