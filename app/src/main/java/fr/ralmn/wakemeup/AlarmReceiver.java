package fr.ralmn.wakemeup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fr.ralmn.wakemeup.object.Alarm;

/**
 * Created by ralmn on 20/09/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public final static String INDICATOR_ACTION = "indicator";
    public final static String STATE_CHANGE_ACTION = "state_change";
    public static final String STATE_CHANGE_NEW_STATE = "new_state";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d("RALMN ACT",intent.getAction()+ " ok?");
        if (INDICATOR_ACTION.equals(intent.getAction())) {
        }else if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            CalendarHelper.calculateWeekAlarms(context);
            CalendarHelper.calculateNextAlarm(context);
        }else if(STATE_CHANGE_ACTION.equals(intent.getAction())){
            int newState = intent.getIntExtra(STATE_CHANGE_NEW_STATE, -1);
            Log.d("RALMN NS", newState + "");
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
        }
    }

}
