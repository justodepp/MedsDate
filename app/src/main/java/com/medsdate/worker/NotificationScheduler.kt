package com.medsdate.worker

import android.content.Context
import androidx.work.*
import com.medsdate.domain.model.Medicine
import com.medsdate.domain.model.NotificationSettings
import timber.log.Timber
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Scheduler for medicine expiry notifications using WorkManager.
 *
 * Schedules notifications based on user preferences and medicine expiry dates.
 */
object NotificationScheduler {

    // shortcut: reminder hour is fixed, add a setting if users ask for it
    private const val NOTIFICATION_HOUR = 9

    /**
     * Milliseconds from [now] until 09:00 local time, [daysBefore] days before [expiry].
     * Zero or negative when that moment has already passed.
     */
    internal fun delayUntilReminder(expiry: Date, daysBefore: Int, now: Long): Long {
        val trigger = Calendar.getInstance().apply {
            time = expiry
            add(Calendar.DAY_OF_YEAR, -daysBefore)
            set(Calendar.HOUR_OF_DAY, NOTIFICATION_HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return trigger.timeInMillis - now
    }

    /**
     * Re-creates the notifications of every medicine (settings changed, app updated).
     * Safe to repeat: work names are unique and replaced.
     */
    fun rescheduleAll(context: Context, medicines: List<Medicine>, settings: NotificationSettings) {
        medicines.forEach { scheduleMedicineNotifications(context, it, settings) }
    }

    /**
     * Schedules notifications for a medicine based on user settings.
     *
     * @param context Application context
     * @param medicine Medicine to schedule notifications for
     * @param settings User notification settings
     */
    fun scheduleMedicineNotifications(
        context: Context,
        medicine: Medicine,
        settings: NotificationSettings
    ) {
        // Cancel existing notifications for this medicine
        cancelMedicineNotifications(context, medicine.id)

        if (!settings.enableNotifications) {
            Timber.d("Notifications disabled, skipping schedule")
            return
        }

        // Schedule first notification
        scheduleNotification(
            context = context,
            medicine = medicine,
            daysBeforeExpiry = settings.firstNotificationDays,
            isFirstNotification = true
        )

        // Schedule second notification if enabled
        if (settings.enableSecondNotification) {
            scheduleNotification(
                context = context,
                medicine = medicine,
                daysBeforeExpiry = settings.secondNotificationDays,
                isFirstNotification = false
            )
        }

        Timber.d("Scheduled notifications for medicine: ${medicine.name}")
    }

    /**
     * Schedules a single notification.
     */
    private fun scheduleNotification(
        context: Context,
        medicine: Medicine,
        daysBeforeExpiry: Int,
        isFirstNotification: Boolean
    ) {
        val delayMillis = delayUntilReminder(medicine.expiryDate, daysBeforeExpiry, System.currentTimeMillis())

        // Don't schedule if notification time has passed
        if (delayMillis <= 0) {
            Timber.d("Notification time passed for ${medicine.name}, skipping")
            return
        }

        // Create work data
        val data = workDataOf(
            NotificationWorker.KEY_MEDICINE_ID to medicine.id,
            NotificationWorker.KEY_MEDICINE_NAME to medicine.name,
            NotificationWorker.KEY_DAYS_UNTIL_EXPIRY to daysBeforeExpiry
        )

        // Create work request
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(getWorkTag(medicine.id, isFirstNotification))
            .build()

        // Enqueue work
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                getWorkName(medicine.id, isFirstNotification),
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

        Timber.d("Scheduled notification for ${medicine.name} in ${TimeUnit.MILLISECONDS.toHours(delayMillis)} h")
    }

    /**
     * Cancels all notifications for a medicine.
     *
     * @param context Application context
     * @param medicineId Medicine ID
     */
    fun cancelMedicineNotifications(context: Context, medicineId: Int) {
        val workManager = WorkManager.getInstance(context)

        // Cancel both notifications
        workManager.cancelUniqueWork(getWorkName(medicineId, true))
        workManager.cancelUniqueWork(getWorkName(medicineId, false))

        Timber.d("Cancelled notifications for medicine ID: $medicineId")
    }

    /**
     * Gets unique work name for a medicine notification.
     */
    private fun getWorkName(medicineId: Int, isFirstNotification: Boolean): String {
        val suffix = if (isFirstNotification) "first" else "second"
        return "medicine_notification_${medicineId}_$suffix"
    }

    /**
     * Gets work tag for a medicine notification.
     */
    private fun getWorkTag(medicineId: Int, isFirstNotification: Boolean): String {
        return "medicine_$medicineId"
    }
}
