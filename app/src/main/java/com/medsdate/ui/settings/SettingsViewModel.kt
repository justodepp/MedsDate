package com.medsdate.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsdate.data.repository.MedicineRepository
import com.medsdate.data.repository.SettingsRepository
import com.medsdate.domain.model.NotificationSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.medsdate.worker.NotificationScheduler
import timber.log.Timber

/**
 * ViewModel for Settings screen.
 *
 * Manages notification preferences including alert timing and toggle options.
 */
class SettingsViewModel(
    private val context: Context,
    private val repository: SettingsRepository,
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    // Settings state
    val settings: StateFlow<NotificationSettings> = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationSettings()
        )

    /**
     * Updates the first notification days.
     */
    fun onFirstNotificationDaysChange(days: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(firstNotificationDays = days)
            repository.updateSettings(updated)
            rescheduleNotifications()
            Timber.d("First notification days updated: $days")
        }
    }

    /**
     * Updates the second notification days.
     */
    fun onSecondNotificationDaysChange(days: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(secondNotificationDays = days)
            repository.updateSettings(updated)
            rescheduleNotifications()
            Timber.d("Second notification days updated: $days")
        }
    }

    /**
     * Toggles whether second notification is enabled.
     */
    fun onSecondNotificationToggle(enabled: Boolean) {
        viewModelScope.launch {
            val updated = settings.value.copy(enableSecondNotification = enabled)
            repository.updateSettings(updated)
            rescheduleNotifications()
            Timber.d("Second notification enabled: $enabled")
        }
    }

    /**
     * Toggles whether all notifications are enabled.
     */
    fun onNotificationsToggle(enabled: Boolean) {
        viewModelScope.launch {
            val updated = settings.value.copy(enableNotifications = enabled)
            repository.updateSettings(updated)
            rescheduleNotifications()
            Timber.d("Notifications enabled: $enabled")
        }
    }

    fun onExpiringSoonDaysChange(days: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(expiringSoonDays = days)
            repository.updateSettings(updated)
            Timber.d("Expiring soon days updated: $days")
        }
    }

    /** Existing medicines keep their old reminders unless they are re-created. */
    private suspend fun rescheduleNotifications() {
        NotificationScheduler.rescheduleAll(
            context,
            medicineRepository.getAllMedicines().first(),
            repository.getSettings().first()
        )
    }
}
