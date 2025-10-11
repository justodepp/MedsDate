package com.medsdate.worker

import android.content.Context
import androidx.work.*
import com.medsdate.domain.model.Medicine
import com.medsdate.domain.model.NotificationSettings
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Scheduler for medicine expiry notifications using WorkManager.
 *
 * Schedules notifications based on user preferences and medicine expiry dates.
 */
object NotificationScheduler {

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
        if (!settings.enableNotifications) {
            Timber.d("Notifications disabled, skipping schedule")
            return
        }

        // Cancel existing notifications for this medicine
        cancelMedicineNotifications(context, medicine.id)

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
        val daysUntilExpiry = medicine.daysUntilExpiry()
        val daysUntilNotification = daysUntilExpiry - daysBeforeExpiry

        // Don't schedule if notification time has passed
        if (daysUntilNotification < 0) {
            Timber.d("Notification time passed for ${medicine.name}, skipping")
            return
        }

        val delayMillis = TimeUnit.DAYS.toMillis(daysUntilNotification.toLong())

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

        Timber.d("Scheduled notification for ${medicine.name} in $daysUntilNotification days")
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
