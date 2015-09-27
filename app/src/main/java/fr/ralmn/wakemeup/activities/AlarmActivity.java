package fr.ralmn.wakemeup.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

import fr.ralmn.wakemeup.AlarmReceiver;
import fr.ralmn.wakemeup.AlarmService;
import fr.ralmn.wakemeup.R;
import fr.ralmn.wakemeup.object.Alarm;

public class AlarmActivity extends Activity {


    public static final String ALARM_SNOOZE_ACTION = "fr.ralmn.wakemeup.ALARM_SNOOZE";
    public static final String ALARM_DISMISS_ACTION= "fr.ralmn.wakemeup.ALARM_DISMISS";

    private boolean mAlarmHandled;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();


            if (!mAlarmHandled) {
                switch (action) {
                    case ALARM_SNOOZE_ACTION:
                        snooze();
                        break;
                    case ALARM_DISMISS_ACTION:
                        dismiss();
                        break;
                    case AlarmService.ALARM_DONE_ACTION:
                        finish();
                        break;
                    default:
                }
            }
        }
    };
    private ImageButton doneButton;
    private ImageButton snoozeButton;
    private Alarm alarm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        alarm = Alarm.getAlarm(getContentResolver(), getIntent().getData());
        Log.d("RALMN", "Alarm state : " + alarm.getState());
        if(alarm == null ||!alarm.isFireState()){
            finish();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

        final IntentFilter filter = new IntentFilter(AlarmService.ALARM_DONE_ACTION);
        filter.addAction(ALARM_SNOOZE_ACTION);
        filter.addAction(ALARM_DISMISS_ACTION);
        registerReceiver(mReceiver, filter);

        TextView labelTextView = (TextView) findViewById(R.id.alarm_label);
        labelTextView.setText(alarm.getLabel());

        doneButton = (ImageButton) findViewById(R.id.alarm_done);
        snoozeButton = (ImageButton) findViewById(R.id.alarm_snooze);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RALMN CLICK", "Dismiss");
                dismiss();
            }
        });

        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RALMN CLICK", "Snooze");
                snooze();
            }
        });

        if(Build.VERSION.SDK_INT < 17){
            TextView clock = (TextView) findViewById(R.id.textClock);
            clock.setText(DateFormat.getTimeFormat(this).format(Calendar.getInstance().getTime()));
        }

    }

    private void snooze() {
        mAlarmHandled = true;
        Intent snoozeIntent = new Intent(this, AlarmReceiver.class);
        snoozeIntent.setData(Alarm.getUri(alarm.get_id()));
        snoozeIntent.setAction(AlarmReceiver.STATE_CHANGE_ACTION);
        snoozeIntent.putExtra(AlarmReceiver.STATE_CHANGE_NEW_STATE, Alarm.SNOOZE_STATE);
        sendBroadcast(snoozeIntent);
        finish();
    }
    private void dismiss(){
        mAlarmHandled = true;

        Intent dismissIntent = new Intent(this, AlarmReceiver.class);
        dismissIntent.setData(Alarm.getUri(alarm.get_id()));
        dismissIntent.setAction(AlarmReceiver.STATE_CHANGE_ACTION);
        dismissIntent.putExtra(AlarmReceiver.STATE_CHANGE_NEW_STATE, Alarm.DISMISS_STATE);

        sendBroadcast(dismissIntent);

        finish();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
