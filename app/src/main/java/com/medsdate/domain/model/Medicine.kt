package com.medsdate.domain.model

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

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
     * Checks if the medicine is expired: it stays valid through the end of its expiry day.
     */
    fun isExpired(today: LocalDate = LocalDate.now()): Boolean {
        return daysUntilExpiry(today) < 0
    }

    /**
     * Calendar days from [today] to the expiry day (0 = expires today, negative if expired).
     */
    fun daysUntilExpiry(today: LocalDate = LocalDate.now()): Long {
        val expiryDay = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return ChronoUnit.DAYS.between(today, expiryDay)
    }

    fun getExpiryStatus(expiringSoonDays: Int = 7, today: LocalDate = LocalDate.now()): ExpiryStatus {
        val days = daysUntilExpiry(today)
        return when {
            days < 0 -> ExpiryStatus.EXPIRED
            days < expiringSoonDays -> ExpiryStatus.EXPIRING_SOON
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
