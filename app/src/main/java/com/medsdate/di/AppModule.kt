package com.medsdate.di

import androidx.room.Room
import com.medsdate.billing.BillingManager
import com.medsdate.data.local.AppDatabase
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.data.repository.SettingsRepository
import com.medsdate.ui.add.AddEditViewModel
import com.medsdate.ui.detail.DetailViewModel
import com.medsdate.ui.home.HomeViewModel
import com.medsdate.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for database and data layer dependencies.
 */
val databaseModule = module {
    // Room Database (Singleton)
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
    }

    // DAOs
    single { get<AppDatabase>().medicineDao() }
    single { get<AppDatabase>().settingsDao() }
}

/**
 * Koin module for repository dependencies.
 */
val repositoryModule = module {
    // Repositories
    single { MedicineRepository(get()) }
    single { SettingsRepository(get()) }
}

/**
 * Koin module for ViewModel dependencies.
 */
val viewModelModule = module {
    // Home ViewModel
    viewModel { HomeViewModel(get()) }

    // Add/Edit ViewModel
    viewModel { AddEditViewModel(androidContext(), get(), get()) }

    // Detail ViewModel
    viewModel { DetailViewModel(androidContext(), get()) }

    // Settings ViewModel
    viewModel { SettingsViewModel(get()) }
}

/**
 * Koin module for billing dependencies.
 */
val billingModule = module {
    // Billing Manager (Singleton)
    single { BillingManager(androidContext()) }
}

/**
 * List of all Koin modules for the app.
 */
val appModules = listOf(
    databaseModule,
    repositoryModule,
    viewModelModule,
    billingModule
)
