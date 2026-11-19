package com.medsdate.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class MedicineTest {

    private val today = LocalDate.of(2026, 11, 19)

    private fun medicine(expiry: LocalDate, hour: Int = 0) = Medicine(
        name = "x",
        expiryDate = Date.from(expiry.atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant())
    )

    @Test
    fun expiringTodayIsNotExpiredWhatTheHour() {
        for (hour in listOf(0, 9, 23)) {
            val m = medicine(today, hour)
            assertFalse(m.isExpired(today))
            assertEquals(0L, m.daysUntilExpiry(today))
            assertEquals(ExpiryStatus.EXPIRING_SOON, m.getExpiryStatus(7, today))
        }
    }

    @Test
    fun yesterdayIsExpired() {
        val m = medicine(today.minusDays(1), 23)
        assertTrue(m.isExpired(today))
        assertEquals(-1L, m.daysUntilExpiry(today))
        assertEquals(ExpiryStatus.EXPIRED, m.getExpiryStatus(7, today))
    }

    @Test
    fun threeDaysAheadCountsCalendarDays() {
        val m = medicine(today.plusDays(3), 1)
        assertEquals(3L, m.daysUntilExpiry(today))
    }

    @Test
    fun soonThresholdIsExclusiveAndExpiredAndSoonNeverOverlap() {
        assertEquals(ExpiryStatus.EXPIRING_SOON, medicine(today.plusDays(6)).getExpiryStatus(7, today))
        assertEquals(ExpiryStatus.VALID, medicine(today.plusDays(7)).getExpiryStatus(7, today))
        val m = medicine(today)
        assertFalse(m.isExpired(today) && m.getExpiryStatus(7, today) == ExpiryStatus.EXPIRING_SOON)
    }
}
