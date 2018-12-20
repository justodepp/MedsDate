package com.medsdate.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;

import com.medsdate.MainActivity;
import com.medsdate.R;
import com.medsdate.data.db.model.MedicineEntry;

public class Receiver extends BroadcastReceiver {

    public static String NOTIFICATION_MEDICINE_ID = "Receiver.NOTIFICATION_MEDICINE_ID";
    public static String NOTIFICATION_MEDICINE_NAME = "Receiver.NOTIFICATION_MEDICINE_NAME";

    @Override
    public void onReceive(Context context, Intent intent) {
        sendNotification(context, intent);
    }

    /**
     * Create and show a simple notification containing the received message.
     *
     * @param extra received.
     */
    private void sendNotification(Context context, Intent extra) {
        Bundle data = extra.getExtras();
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String requestCode = 0 + String.valueOf(data.get(NOTIFICATION_MEDICINE_ID));
        int realRequestCode = Integer.valueOf(requestCode);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                realRequestCode /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = context.getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
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
                        .setContentIntent(pendingIntent);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            assert notificationManager != null;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(channelId);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            if (mChannel == null) {
                mChannel = new NotificationChannel(channelId, channelId, importance);
                mChannel.setDescription(channelId);
                mChannel.enableVibration(true);
                mChannel.setSound(defaultSoundUri, audioAttributes);
                notificationManager.createNotificationChannel(mChannel);
            }
        }

        assert notificationManager != null;
        notificationManager.notify(realRequestCode /* ID of notification */, notificationBuilder.build());
    }

    public static void setAlarmNotification(Context context, MedicineEntry medicine) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, Receiver.class);
        intent.putExtra(NOTIFICATION_MEDICINE_ID, medicine.getId());
        intent.putExtra(NOTIFICATION_MEDICINE_NAME, medicine.getName());

        String requestCode = 0 + String.valueOf(medicine.getId());
        int realRequestCode = Integer.valueOf(requestCode);
        PendingIntent operation = PendingIntent.getBroadcast(context, realRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // I choose 3s after the launch of my application /* System.currentTimeMillis() + 3000 */
        Long alarmTime = medicine.getExpireAt().getTime()+10*60*60*1000;
        assert alarms != null;
        alarms.set(AlarmManager.RTC_WAKEUP, alarmTime, operation);

        // Toast.makeText(context, context.getString(R.string.text_alarm_set), Toast.LENGTH_SHORT).show();
    }

    public static void cancelAlarmNotification(Context context, MedicineEntry medicine) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, Receiver.class);
        String requestCode = 0 + String.valueOf(medicine.getId());
        int realRequestCode = Integer.valueOf(requestCode);
        PendingIntent operation = PendingIntent.getBroadcast(context, realRequestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        assert alarms != null;
        alarms.cancel(operation);
        operation.cancel();

        // Toast.makeText(context, context.getString(R.string.text_alarm_unset), Toast.LENGTH_SHORT).show();
    }
}
