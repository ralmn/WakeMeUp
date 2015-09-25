package fr.ralmn.wakemeup;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import fr.ralmn.wakemeup.object.Alarm;

public class AlarmService extends Service {

    public static final String ALARM_ALERT_ACTION = "fr.ralmn.wakemeup.ALARM_ALERT";

    // A public action sent by AlarmService when the alarm has stopped for any reason.
    public static final String ALARM_DONE_ACTION = "fr.ralmn.wakemeup.ALARM_DONE";

    // Private action used to start an alarm with this service.
    public static final String START_ALARM_ACTION = "fr.ralmn.wakemeup.START_ALARM";

    // Private action used to stop an alarm with this service.
    public static final String STOP_ALARM_ACTION = "fr.ralmn.wakemeup.STOP_ALARM";
    private Alarm mCurrentAlarm;


    public static void startAlarm(Context context, Alarm alarm){
        Intent intent = new Intent(context, AlarmService.class);
        intent.setData(Alarm.getUri(alarm.get_id()));

        intent.setAction(START_ALARM_ACTION);

        AlarmAlertWakeLock.acquireCpuWakeLock(context);
        AlarmAlertWakeLock.acquireScreenCpuWakeLock(context);

        context.startService(intent);
    }

    public static void stopAlarm(Context context, Alarm alarm){
        Intent intent = new Intent(context, AlarmService.class);
        intent.setData(Alarm.getUri(alarm.get_id()));

        intent.setAction(STOP_ALARM_ACTION);

        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        context.startService(intent);
    }



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ContentResolver cr = getContentResolver();
        Alarm alarm = Alarm.getAlarm(cr, intent.getData());
        if(intent.getAction().equals(START_ALARM_ACTION)){

            if(alarm == null){
                Log.e("WAKEMEUP", "No alarm found");
                return Service.START_NOT_STICKY;
            } else if (mCurrentAlarm != null && mCurrentAlarm.get_id() == alarm.get_id()) {
                Log.e("WAKEMEUP", "Alarm already started for instance: " + alarm.get_id());
                return Service.START_NOT_STICKY;
            }
            startAlarm(alarm);
        }else if(intent.getAction().equals(STOP_ALARM_ACTION)){
            if(mCurrentAlarm != null && mCurrentAlarm.get_id() != alarm.get_id()){
                Log.e("WAKEMEUP", "Alarm is not current alarm");
            }else
                stopSelf();

        }


        return Service.START_NOT_STICKY;
    }

    private void startAlarm(Alarm alarm) {
        if (mCurrentAlarm != null) {
            //TODO : AlarmStateManager.setMissedState(this, mCurrentAlarm);
            stopCurrentAlarm();
        }
        AlarmNotification.showAlarmNotification(this, alarm);
        AlarmKlaxon.start(this, alarm, false);
        AlarmAlertWakeLock.acquireCpuWakeLock(this);
        mCurrentAlarm = alarm;


    }

    private void stopCurrentAlarm() {
        if (mCurrentAlarm == null) {
            return;
        }
        AlarmNotification.clearNotification(this, mCurrentAlarm);
        AlarmKlaxon.stop(this);
        AlarmAlertWakeLock.releaseCpuLock();
        sendBroadcast(new Intent(ALARM_DONE_ACTION));
        mCurrentAlarm = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCurrentAlarm();
    }

    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }
}
