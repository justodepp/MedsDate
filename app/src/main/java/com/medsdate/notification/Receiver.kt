package com.medsdate.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.medsdate.MainActivity
import com.medsdate.R
import com.medsdate.data.db.model.MedicineEntry
import java.util.*

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        sendNotification(context, intent)
    }

    /**
     * Create and show a simple notification containing the received message.
     *
     * @param extra received.
     */
    private fun sendNotification(context: Context, extra: Intent) {
        val data = extra.extras
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0.toString() + data!![NOTIFICATION_MEDICINE_ID].toString()
        val realRequestCode = Integer.valueOf(requestCode)
        val pendingIntent = PendingIntent.getActivity(context,
                realRequestCode /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT)
        val channelId = context.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(
                        context.getString(
                                R.string.notification_message,
                                data.getString(NOTIFICATION_MEDICINE_NAME)
                        )
                )
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel = notificationManager.getNotificationChannel(channelId)
            val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            if (mChannel == null) {
                mChannel = NotificationChannel(channelId, channelId, importance)
                mChannel.description = channelId
                mChannel.enableVibration(true)
                mChannel.setSound(defaultSoundUri, audioAttributes)
                notificationManager.createNotificationChannel(mChannel)
            }
        }
        notificationManager.notify(realRequestCode /* ID of notification */, notificationBuilder.build())
    }

    companion object {
        var NOTIFICATION_MEDICINE_ID = "Receiver.NOTIFICATION_MEDICINE_ID"
        var NOTIFICATION_MEDICINE_NAME = "Receiver.NOTIFICATION_MEDICINE_NAME"

        @JvmStatic
        fun setAlarmNotification(context: Context, medicine: MedicineEntry) {
            val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, Receiver::class.java)
            intent.putExtra(NOTIFICATION_MEDICINE_ID, medicine.id)
            intent.putExtra(NOTIFICATION_MEDICINE_NAME, medicine.name)
            val requestCode = 0.toString() + medicine.id.toString()
            val realRequestCode = Integer.valueOf(requestCode)
            val operation = PendingIntent.getBroadcast(context, realRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            // I choose 3s after the launch of my application /* System.currentTimeMillis() + 3000 */
            val alarmTime = medicine.expireAt!!.time + 10 * 60 * 60 * 1000
            alarms[AlarmManager.RTC_WAKEUP, alarmTime] = operation
            // First alarm, 2 days before the expiration date
            val calendar = Calendar.getInstance()
            calendar.time = medicine.expireAt
            calendar.add(Calendar.DAY_OF_MONTH, -2)
            val prevAlarmTime = calendar.time.time + 10 * 60 * 60 * 1000
            alarms[AlarmManager.RTC_WAKEUP, prevAlarmTime] = operation
        }

        @JvmStatic
        fun cancelAlarmNotification(context: Context, medicine: MedicineEntry) {
            val alarms = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, Receiver::class.java)
            val requestCode = 0.toString() + medicine.id.toString()
            val realRequestCode = Integer.valueOf(requestCode)
            val operation = PendingIntent.getBroadcast(context, realRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            alarms.cancel(operation)
            operation.cancel()
        }
    }
}