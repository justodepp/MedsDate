package com.medsdate.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing user notification settings.
 *
 * This entity stores user preferences for medication expiry notifications and app language.
 *
 * @property id Primary key (always 1 as we only store one settings record)
 * @property enableNotifications Whether notifications are enabled
 * @property firstNotificationDays Days before expiry for first notification (1-30)
 * @property secondNotificationDays Days before expiry for second notification (1-30)
 * @property enableSecondNotification Whether the second notification is enabled
 * @property languageCode Language code (ISO 639-1, e.g., "it", "en")
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "enable_notifications")
    val enableNotifications: Boolean = true,

    @ColumnInfo(name = "first_notification_days")
    val firstNotificationDays: Int = 7,

    @ColumnInfo(name = "second_notification_days")
    val secondNotificationDays: Int = 2,

    @ColumnInfo(name = "enable_second_notification")
    val enableSecondNotification: Boolean = true,

    @ColumnInfo(name = "language_code")
    val languageCode: String = "it" // Default to Italian
)
