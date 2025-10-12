package com.medsdate.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medsdate.data.repository.SettingsRepository
import com.medsdate.domain.model.NotificationSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for Settings screen.
 *
 * Manages notification preferences including alert timing and toggle options.
 */
class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    // Settings state
    val settings: StateFlow<NotificationSettings> = repository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationSettings()
        )

    // UI State for save feedback
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    /**
     * Updates the first notification days.
     */
    fun onFirstNotificationDaysChange(days: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(firstNotificationDays = days)
            repository.updateSettings(updated)
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
            Timber.d("Notifications enabled: $enabled")
        }
    }

    /**
     * Updates the app language.
     */
    fun onLanguageChange(languageCode: String) {
        viewModelScope.launch {
            val updated = settings.value.copy(languageCode = languageCode)
            repository.updateSettings(updated)
            Timber.d("Language updated: $languageCode")
        }
    }

    /**
     * Saves settings and shows feedback.
     */
    fun saveSettings() {
        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                // Settings are already saved on each change, so just show success
                _saveState.value = SaveState.Success
                Timber.d("Settings saved successfully")

                // Reset state after a delay
                kotlinx.coroutines.delay(2000)
                _saveState.value = SaveState.Idle
            } catch (e: Exception) {
                Timber.e(e, "Error saving settings")
                _saveState.value = SaveState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

/**
 * State for save operation.
 */
sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
