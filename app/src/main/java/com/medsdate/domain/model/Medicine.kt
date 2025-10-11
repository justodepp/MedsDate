package com.medsdate.domain.model

import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Domain model representing a medicine.
 *
 * This is the business logic representation of a medicine, separate from
 * the database entity to keep the domain layer independent.
 *
 * @property id Unique identifier
 * @property name Medicine name
 * @property expiryDate Expiration date
 * @property quantity Amount of medicine
 * @property imagePath Path to medicine image
 * @property notes Additional notes
 * @property createdAt Creation timestamp
 * @property updatedAt Last update timestamp
 */
data class Medicine(
    val id: Int = 0,
    val name: String,
    val expiryDate: Date,
    val quantity: Int = 1,
    val imagePath: String? = null,
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) {
    /**
     * Checks if the medicine is expired.
     */
    fun isExpired(): Boolean {
        return expiryDate.before(Date())
    }

    /**
     * Gets the number of days until expiry (negative if expired).
     */
    fun daysUntilExpiry(): Long {
        val diff = expiryDate.time - Date().time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
    }

    /**
     * Gets the expiry status for UI display.
     */
    fun getExpiryStatus(): ExpiryStatus {
        val days = daysUntilExpiry()
        return when {
            days < 0 -> ExpiryStatus.EXPIRED
            days <= 7 -> ExpiryStatus.EXPIRING_SOON
            else -> ExpiryStatus.VALID
        }
    }
}

/**
 * Enum representing the expiry status of a medicine.
 */
enum class ExpiryStatus {
    VALID,          // More than 7 days until expiry
    EXPIRING_SOON,  // 7 days or less until expiry
    EXPIRED         // Already expired
}
