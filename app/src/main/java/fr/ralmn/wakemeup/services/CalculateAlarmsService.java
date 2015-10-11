package fr.ralmn.wakemeup.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.IBinder;

import fr.ralmn.wakemeup.CalendarHelper;
import fr.ralmn.wakemeup.R;
import fr.ralmn.wakemeup.object.Alarm;

public class CalculateAlarmsService extends Service {

    public static final String CALCULATE_ACTION = "fr.ralmn.wakemeup.calculateaction";

    public CalculateAlarmsService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CalendarHelper.calculateWeekAlarms(this);
        Alarm nextAlarm = CalendarHelper.calculateNextAlarm(this);
        String notification_message;
        if(nextAlarm != null){
            notification_message = getResources().getString(R.string.alarm_next_alarm_set_on) + " " + nextAlarm.getNextFullDateString(this);
        }else{
            notification_message = getResources().getString(R.string.alarm_cannot_set);
        }
        NotificationManager nm = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder notification = new Notification.Builder(this);
        notification.setContentText(notification_message)
                .setContentTitle(getResources().getString(R.string.app_name));

        Icon icon = Icon.createWithResource(this, getApplicationInfo().icon);
        Icon iconWhite = Icon.createWithResource(this, R.drawable.ic_notif);
        notification.setLargeIcon(icon);
        notification.setSmallIcon(iconWhite);
        nm.notify(-593 , notification.build());


        return Service.START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
