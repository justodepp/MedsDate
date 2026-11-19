package com.medsdate

import android.app.Application
import com.google.firebase.FirebaseApp
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.data.repository.SettingsRepository
import com.medsdate.di.appModules
import com.medsdate.worker.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
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

            // Load modules
            modules(appModules)
        }

        // Re-create reminders at startup: covers medicines saved before WorkManager worked (<= 1.3)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationScheduler.rescheduleAll(
                    this@MedsDateApplication,
                    get<MedicineRepository>().getAllMedicines().first(),
                    get<SettingsRepository>().getSettings().first()
                )
            } catch (e: Exception) {
                Timber.e(e, "Error rescheduling notifications")
            }
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        Timber.d("MedsDateApplication initialized with Koin DI")
    }
}
