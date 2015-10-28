package fr.ralmn.wakemeup;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import fr.ralmn.wakemeup.activities.AlarmListActivity;
import fr.ralmn.wakemeup.object.Alarm;

/**
 * Created by ralmn on 20/09/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public final static String INDICATOR_ACTION = "indicator";
    public final static String STATE_CHANGE_ACTION = "state_change";
    public static final String STATE_CHANGE_NEW_STATE = "new_state";
    public static final String CALCULATE_ACTION = "fr.ralmn.wakemeup.calcAlarm";

    @Override
    public void onReceive(final Context context, final Intent intent) {
       Log.d("RALMN",intent.getAction()+ " ok?");
        if (INDICATOR_ACTION.equals(intent.getAction())) {
        }else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            CalendarHelper.calculateWeekAlarms(context);
            CalendarHelper.calculateNextAlarm(context);
            context.registerReceiver(new AlarmReceiver(), new IntentFilter(AlarmReceiver.CALCULATE_ACTION));
            AlarmListActivity.startAutoCheck(context);
        }else if(STATE_CHANGE_ACTION.equals(intent.getAction())){
            int newState = intent.getIntExtra(STATE_CHANGE_NEW_STATE, -1);
//            Log.d("RALMN NS", newState + "");
            Alarm alarm = Alarm.getAlarm(context.getContentResolver(), intent.getData());
                if(alarm != null){
                    switch (newState){
                        case Alarm.FIRED_STATE:
                            alarm.setFiredState(context);
                            break;
                        case Alarm.DISMISS_STATE:
                            alarm.setDismissState(context);
                            break;
                        case Alarm.SNOOZE_STATE:
                            alarm.setSnoozeState(context);
                    }
                }
        }else if(CALCULATE_ACTION.equals(intent.getAction())){
            Log.d("RALMN", "Auto calc");
            autoCalcAlarms(context);
        }
    }

    private void autoCalcAlarms(Context context){

        CalendarHelper.calculateWeekAlarms(context);
        Alarm nextAlarm = CalendarHelper.calculateNextAlarm(context);
        String notification_message;
        if(nextAlarm != null){
            notification_message = context.getResources().getString(R.string.alarm_next_alarm_set_on) + " " + nextAlarm.getNextFullDateString(context);
        }else{
            notification_message = context.getResources().getString(R.string.alarm_cannot_set);
        }
        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder notification = new Notification.Builder(context);
        notification.setContentText(notification_message)
                .setContentTitle(context.getResources().getString(R.string.app_name));

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            notification.setStyle(
                    new Notification.BigTextStyle()
                            .bigText(notification_message)
                            .setBigContentTitle(notification_message));
        }

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), context.getApplicationInfo().icon);

        notification.setLargeIcon(icon);
        notification.setSmallIcon(R.drawable.ic_notif);
        nm.notify(-505, notification.build());

        AlarmListActivity.startAutoCheck(context);
    }

}
