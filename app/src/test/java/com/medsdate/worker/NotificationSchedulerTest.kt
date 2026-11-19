package com.medsdate.worker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date

class NotificationSchedulerTest {

    private fun at(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance().apply { clear(); set(year, month, day, hour, minute) }.timeInMillis

    @Test
    fun reminderIsAt9amNDaysBeforeExpiry() {
        val now = at(2026, Calendar.JANUARY, 1, 8, 0)
        val expiry = Date(at(2026, Calendar.JANUARY, 20, 15, 30))
        val delay = NotificationScheduler.delayUntilReminder(expiry, 7, now)
        // 13 Jan 09:00 is 12 days and 1 hour after 1 Jan 08:00
        assertEquals((12L * 24 + 1) * 3_600_000, delay)
    }

    @Test
    fun reminderAlreadyPassedIsNotPositive() {
        val now = at(2026, Calendar.JANUARY, 13, 9, 30)
        val expiry = Date(at(2026, Calendar.JANUARY, 20))
        assertTrue(NotificationScheduler.delayUntilReminder(expiry, 7, now) <= 0)
    }

    @Test
    fun reminderLaterTodayIsPositive() {
        val now = at(2026, Calendar.JANUARY, 13, 7, 0)
        val expiry = Date(at(2026, Calendar.JANUARY, 20))
        assertEquals(2 * 3_600_000L, NotificationScheduler.delayUntilReminder(expiry, 7, now))
    }
}
