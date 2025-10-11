package com.medsdate.data.repository

import com.medsdate.data.local.dao.SettingsDao
import com.medsdate.data.local.entity.SettingsEntity
import com.medsdate.domain.model.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for settings data operations.
 *
 * Manages user notification preferences and provides a clean interface
 * for the UI layer to interact with settings data.
 */
class SettingsRepository(private val settingsDao: SettingsDao) {

    /**
     * Retrieves the current notification settings.
     */
    fun getSettings(): Flow<NotificationSettings> {
        return settingsDao.getSettings().map { entity ->
            entity?.toDomainModel() ?: NotificationSettings()
        }
    }

    /**
     * Updates the notification settings.
     */
    suspend fun updateSettings(settings: NotificationSettings) {
        settingsDao.insertSettings(settings.toEntity())
    }
}

/**
 * Extension function to convert SettingsEntity to NotificationSettings domain model.
 */
private fun SettingsEntity.toDomainModel(): NotificationSettings {
    return NotificationSettings(
        enableNotifications = this.enableNotifications,
        firstNotificationDays = this.firstNotificationDays,
        secondNotificationDays = this.secondNotificationDays,
        enableSecondNotification = this.enableSecondNotification
    )
}

/**
 * Extension function to convert NotificationSettings domain model to SettingsEntity.
 */
private fun NotificationSettings.toEntity(): SettingsEntity {
    return SettingsEntity(
        id = 1,
        enableNotifications = this.enableNotifications,
        firstNotificationDays = this.firstNotificationDays,
        secondNotificationDays = this.secondNotificationDays,
        enableSecondNotification = this.enableSecondNotification
    )
}
