package fr.ralmn.wakemeup;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import fr.ralmn.wakemeup.activities.AlarmActivity;
import fr.ralmn.wakemeup.object.Alarm;

/**
 * Created by ralmn on 24/09/15.
 */
public class AlarmNotification {

    public static void showAlarmNotification(Context context, Alarm alarm) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Close dialogs and window shade, so this will display
        context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        Resources resources = context.getResources();
        Notification.Builder notification = new Notification.Builder(context)
                .setContentTitle(alarm.getLabel())
                .setContentText(alarm.getTimeString(context))
                .setSmallIcon(R.drawable.ic_alarm_black)
                .setOngoing(true)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setWhen(0)
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setLocalOnly(true);

        // Setup Snooze Action
        Intent snoozeIntent = new Intent(context, AlarmReceiver.class);
        snoozeIntent.setData(Alarm.getUri(alarm.get_id()));
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, alarm.get_id(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        notification.addAction(R.drawable.ic_alarm_add_black,
                resources.getString(R.string.alarm_alert_snooze_text), snoozePendingIntent);

        // Setup Dismiss Action
        Intent dismissIntent = new Intent(context, AlarmReceiver.class);
        dismissIntent.setData(Alarm.getUri(alarm.get_id()));
        dismissIntent.setAction(AlarmReceiver.STATE_CHANGE_ACTION);
        dismissIntent.putExtra(AlarmReceiver.STATE_CHANGE_NEW_STATE, Alarm.DISMISS_STATE);

        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context,
                alarm.get_id(), dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.addAction(R.drawable.ic_alarm_off_black,
                resources.getString(R.string.alarm_alert_dismiss_text),
                dismissPendingIntent);

        // Setup Content Action
        Intent contentIntent = new Intent(context, AlarmActivity.class);
        contentIntent.setData(Alarm.getUri(alarm.get_id()));
        notification.setContentIntent(PendingIntent.getActivity(context,
                alarm.get_id(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        // Setup fullscreen intent
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        fullScreenIntent.setData(Alarm.getUri(alarm.get_id()));
        // set action, so we can be different then content pending intent
        fullScreenIntent.setAction("fullscreen_activity");
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        notification.setFullScreenIntent(PendingIntent.getActivity(context,
                alarm.get_id(), fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT), true);
        notification.setPriority(Notification.PRIORITY_MAX);

        nm.cancel(alarm.get_id());
        nm.notify(alarm.get_id(), notification.build());
    }

    public static void clearNotification(Context context, Alarm alarm) {
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(alarm.get_id());
    }
}
