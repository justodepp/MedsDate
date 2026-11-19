package com.medsdate.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.medsdate.MainActivity
import com.medsdate.R
import timber.log.Timber

/**
 * WorkManager worker for sending medicine expiry notifications.
 *
 * This worker is scheduled by NotificationScheduler and sends notifications
 * at the appropriate time before medicine expiry.
 */
class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val medicineId = inputData.getInt(KEY_MEDICINE_ID, -1)
            val medicineName = inputData.getString(KEY_MEDICINE_NAME) ?: "Medicine"
            val daysUntilExpiry = inputData.getInt(KEY_DAYS_UNTIL_EXPIRY, 0)

            if (medicineId == -1) {
                return Result.failure()
            }

            sendNotification(medicineId, medicineName, daysUntilExpiry)
            Timber.d("Notification sent for medicine: $medicineName")

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error sending notification")
            Result.retry()
        }
    }

    /**
     * Sends a notification for medicine expiry.
     */
    private fun sendNotification(medicineId: Int, medicineName: String, daysUntilExpiry: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val localized = localizedContext()

        // Create notification channel (Android O+)
        createNotificationChannel(notificationManager, localized)

        // Create intent to open app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            medicineId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(localized.getString(R.string.expiry_notification_title))
            .setContentText(getNotificationText(localized, medicineName, daysUntilExpiry))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getNotificationText(localized, medicineName, daysUntilExpiry))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Send notification
        notificationManager.notify(medicineId, notification)
    }

    /**
     * Context in the language chosen in the app. Below API 33 the application context only follows
     * the system language, so apply the app locale by hand.
     */
    private fun localizedContext(): Context {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return applicationContext
        val config = Configuration(applicationContext.resources.configuration)
        config.setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
        return applicationContext.createConfigurationContext(config)
    }

    /**
     * Creates notification channel for Android O+.
     */
    private fun createNotificationChannel(notificationManager: NotificationManager, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.expiry_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = context.getString(R.string.expiry_channel_description)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Gets notification text based on days until expiry.
     */
    private fun getNotificationText(context: Context, medicineName: String, daysUntilExpiry: Int): String {
        return if (daysUntilExpiry <= 1) {
            context.getString(R.string.expiry_notification_tomorrow, medicineName)
        } else {
            context.getString(R.string.expiry_notification_days, medicineName, daysUntilExpiry)
        }
    }

    companion object {
        const val KEY_MEDICINE_ID = "medicine_id"
        const val KEY_MEDICINE_NAME = "medicine_name"
        const val KEY_DAYS_UNTIL_EXPIRY = "days_until_expiry"

        private const val CHANNEL_ID = "medicine_expiry_channel"
    }
}
