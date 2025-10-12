package com.medsdate.domain.model

/**
 * Domain model representing notification settings and app preferences.
 *
 * @property enableNotifications Whether notifications are enabled
 * @property firstNotificationDays Days before expiry for first notification
 * @property secondNotificationDays Days before expiry for second notification
 * @property enableSecondNotification Whether second notification is enabled
 * @property languageCode Language code (ISO 639-1, e.g., "it", "en")
 */
data class NotificationSettings(
    val enableNotifications: Boolean = true,
    val firstNotificationDays: Int = 7,
    val secondNotificationDays: Int = 2,
    val enableSecondNotification: Boolean = true,
    val languageCode: String = "it" // Default to Italian
) {
    /**
     * Validates that notification days are within acceptable range (1-30).
     */
    fun isValid(): Boolean {
        return firstNotificationDays in 1..30 &&
                secondNotificationDays in 1..30 &&
                firstNotificationDays > secondNotificationDays
    }
}
