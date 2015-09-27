package fr.ralmn.wakemeup.object;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.ralmn.wakemeup.AlarmNotification;
import fr.ralmn.wakemeup.AlarmReceiver;
import fr.ralmn.wakemeup.AlarmService;
import fr.ralmn.wakemeup.AlarmsDatabaseHelper;
import fr.ralmn.wakemeup.CalendarHelper;
import fr.ralmn.wakemeup.Utils;
import fr.ralmn.wakemeup.activities.AlarmListActivity;

public class Alarm implements Comparable<Alarm>{

    public static final int IDLE_STATE = 0;
    public static final int FIRED_STATE = 1;
    public static final int DISMISS_STATE = 2;
    public static final int SNOOZE_STATE = 3;

    private int _id = -1;
    private int alarmId = -1;

    private int state = IDLE_STATE;

    private boolean enabled;

    private Calendar date;
    private Calendar snooze;

    private String label;

    public Alarm(int _id, Calendar date, String label, boolean enabled, int state) {
        this._id = _id;
        this.state = state;
        this.enabled = enabled;
        this.date = date;
        this.label = label;
    }

    public Alarm(int _id, Calendar date, String label, boolean enabled) {
        this._id = _id;
        this.date = date;
        this.label = label;
        this.enabled = enabled;
    }

    public Alarm(Calendar date, String label) {
        this(-1, date, label, true);
    }

    public Alarm(Calendar date) {
        this(-1, date, "", true);
    }

    public int getHours() {
        return date.get(Calendar.HOUR_OF_DAY);
    }

    public String getLabel() {
        return label;
    }

    public int getMinutes() {
        return date.get(Calendar.MINUTE);
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public int get_id() {
        return _id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTimeString(Context context){
        java.text.DateFormat dateFormat = DateFormat.getTimeFormat(context);
        return dateFormat.format(date.getTime());
    }

    public String getSnoozeTimeString(Context context) {
        if(snooze == null) return "";
        java.text.DateFormat dateFormat = DateFormat.getTimeFormat(context);
        return dateFormat.format(snooze.getTime());
    }

    public void setSnooze(Calendar snooze) {
        this.snooze = snooze;
    }

    public Calendar getSnooze() {
        return snooze;
    }

    public long getTimeMillis(){
        return date.getTimeInMillis();
    }

    public static List<Alarm> getAlarms(Context context){
        List<Alarm> alarms = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, null, null, null, null);
        if(cursor == null) return alarms;
        Log.d("RALMN", cursor.getColumnCount() + "");
        while (cursor.moveToNext()) {
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(Long.parseLong(cursor.getString(1)));
            Alarm alarm = new Alarm(
                    cursor.getInt(0),
                    date,
                    cursor.getString(3),
                    cursor.getInt(4) == 1,
                    cursor.getInt(5));
            long snoozeMillis = Long.parseLong(cursor.getString(4));
            if(snoozeMillis > -1) {
                Calendar snooze = Calendar.getInstance();
                snooze.setTimeInMillis(snoozeMillis);
                alarm.snooze = snooze;
            }
            alarms.add(alarm);
        }
        return alarms;
    }

    public ContentValues toContentValues(Context context) {
        ContentValues values = new ContentValues();
        if(_id > -1)
            values.put(AlarmsDatabaseHelper.AlarmsColumns._ID, _id);
        values.put(AlarmsDatabaseHelper.AlarmsColumns.DATE, date.getTimeInMillis());
        values.put(AlarmsDatabaseHelper.AlarmsColumns.LABEL, label);
        values.put(AlarmsDatabaseHelper.AlarmsColumns.ENABLED, enabled);
        values.put(AlarmsDatabaseHelper.AlarmsColumns.STATE, state);
        values.put(AlarmsDatabaseHelper.AlarmsColumns.SNOOZE, snooze != null ? snooze.getTimeInMillis() : -1);
        return values;
    }

    public void defineAlarm(Context context){

        Log.d("RALMN", "define " + toString());

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        if(_id == -1) {
            Uri uri = context.getContentResolver().insert(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, toContentValues(context));
            this._id = (int) ContentUris.parseId(uri);
        }

        int flags = _id == -1 ? PendingIntent.FLAG_NO_CREATE :  0; // (if need to dont create : PendingIntent.FLAG_NO_CREATE)
        PendingIntent operation = PendingIntent.getBroadcast(context, _id /* requestCode */,
                new Intent(context, AlarmReceiver.class).setAction(AlarmReceiver.INDICATOR_ACTION), flags);

        PendingIntent viewIntent = PendingIntent.getActivity(context, _id,
                createViewAlarmIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);

        long alarmTime = getNextAlarm().getTimeInMillis();

        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(alarmTime, viewIntent);

        alarmManager.setAlarmClock(info, operation);

        scheduleInstanceStateChange(context, getNextAlarm(), FIRED_STATE);


    }

    private void scheduleInstanceStateChange(Context context, Calendar time, int newState) {
        long timeInMillis = time.getTimeInMillis();
        Intent stateChangeIntent = new Intent(context, AlarmReceiver.class);
        stateChangeIntent.setAction(AlarmReceiver.STATE_CHANGE_ACTION);
        stateChangeIntent.putExtra(AlarmReceiver.STATE_CHANGE_NEW_STATE, newState);
        stateChangeIntent.setData(getUri(_id));


        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, _id,
                stateChangeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Utils.isKitKatOrLater()) {
            am.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    public void unDefineAlarm(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int flags = _id == -1 ? PendingIntent.FLAG_NO_CREATE :  0; // (if need to dont create : PendingIntent.FLAG_NO_CREATE)
        PendingIntent operation = PendingIntent.getBroadcast(context, _id /* requestCode */,
                new Intent(context, AlarmReceiver.class).setAction(AlarmReceiver.INDICATOR_ACTION), flags);

        if(operation != null){
            alarmManager.cancel(operation);
        }

    }

    public void setFiredState(Context context){
        AlarmNotification.clearAlarmSnoozeNotification(context, this);
        this.state = FIRED_STATE;
        update(context);
        AlarmService.startAlarm(context, this);
    }

    private void update(Context context) {
        context.getContentResolver().update(getUri(_id), toContentValues(context), null, null);
    }

    public void setDismissState(Context context){
        this.state = DISMISS_STATE;
        update(context);
        AlarmService.stopAlarm(context, this);
        AlarmNotification.clearNotification(context, this);
        AlarmNotification.clearAlarmSnoozeNotification(context, this);
        unDefineAlarm(context);
        CalendarHelper.calculateNextAlarm(context);
    }

    public void setSnoozeState(Context context){
        AlarmService.stopAlarm(context, this);

        this.state = SNOOZE_STATE;
        this.snooze = Calendar.getInstance();
        int minutes = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("default_snooze_time", "10"));
        this.snooze.add(Calendar.MINUTE, minutes); //MINUTES
        update(context);
        //defineAlarm(context);
        //scheduleInstanceStateChange(context, snooze, FIRED_STATE);
        AlarmNotification.showAlarmSnoozeNotification(context, this);
        CalendarHelper.calculateNextAlarm(context);

    }

    public Calendar getNextAlarm(){
        if(snooze != null && snooze.after(date)){
            Log.d("RALMN", "use snooze date");
            return snooze;
        }
        return date;
    }

    public static Intent createIntent(Context context, Class<?> cls, long alarmId) {
        return new Intent(context, cls).setData(getUri(alarmId));
    }

    public static Uri getUri(long alarmId) {
        return ContentUris.withAppendedId(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, alarmId);
    }

    private Intent createViewAlarmIntent(Context context) {
        Intent viewAlarmIntent = createIntent(context, AlarmListActivity.class, alarmId) ;
        //viewAlarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return viewAlarmIntent;
    }

    public static Alarm getAlarm(ContentResolver contentResolver, Uri data) {
        if(data == null){
            Log.e("RALMN", "data uri null");

        }
        Cursor cursor = contentResolver.query(data, null, null, null, null);
        Alarm result = null;
        if (cursor == null) {
            return result;
        }
        try {
            if (cursor.moveToFirst()) {
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(Long.parseLong(cursor.getString(1)));
                long snoozeMillis = Long.parseLong(cursor.getString(4));
                result = new Alarm(
                        cursor.getInt(0),
                        date,
                        cursor.getString(3),
                        cursor.getInt(4) == 1,
                        cursor.getInt(5));
                if(snoozeMillis > -1) {
                    Calendar snooze = Calendar.getInstance();
                    snooze.setTimeInMillis(snoozeMillis);
                    result.snooze = snooze;
                }
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    public int getState() {
        return state;
    }

    public boolean isFireState() {
        return state == FIRED_STATE;
    }

    public String toString(Context context) {
        return "Alarm{" +
                "_id=" + _id +
                ", alarmId=" + alarmId +
                ", state=" + state +
                ", enabled=" + enabled +
                ", date=" + getTimeString(context) +
                ", snooze=" + getSnoozeTimeString(context) +
                ", label='" + label + '\'' +
                '}';
    }


    public int compareTo(Alarm a) {
        return getDate().compareTo(a.getDate());
    }

}
