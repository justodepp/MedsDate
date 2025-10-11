package com.medsdate.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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

        // Create notification channel (Android O+)
        createNotificationChannel(notificationManager)

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
            .setContentTitle("Medicine Expiry Reminder")
            .setContentText(getNotificationText(medicineName, daysUntilExpiry))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getNotificationText(medicineName, daysUntilExpiry))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Send notification
        notificationManager.notify(medicineId, notification)
    }

    /**
     * Creates notification channel for Android O+.
     */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Gets notification text based on days until expiry.
     */
    private fun getNotificationText(medicineName: String, daysUntilExpiry: Int): String {
        return when {
            daysUntilExpiry <= 0 -> "$medicineName has expired!"
            daysUntilExpiry == 1 -> "$medicineName expires tomorrow!"
            else -> "$medicineName expires in $daysUntilExpiry days"
        }
    }

    companion object {
        const val KEY_MEDICINE_ID = "medicine_id"
        const val KEY_MEDICINE_NAME = "medicine_name"
        const val KEY_DAYS_UNTIL_EXPIRY = "days_until_expiry"

        private const val CHANNEL_ID = "medicine_expiry_channel"
        private const val CHANNEL_NAME = "Medicine Expiry"
        private const val CHANNEL_DESCRIPTION = "Notifications for medicine expiry reminders"
    }
}
