package fr.ralmn.wakemeup.object;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import fr.ralmn.wakemeup.AlarmsDatabaseHelper;

public class Alarm {


    private int alarmId;

    private boolean enabled;

    private int hours;
    private int minutes;

    private String label;

    public Alarm(int alarmId, int hours, int minutes, String label, boolean enabled) {
        this.alarmId = alarmId;
        this.hours = hours % 24;
        this.minutes = minutes % 60;
        this.label = label;
        this.enabled = true;
    }

    public Alarm(int hours, int minutes, String label) {
        this(-1, hours, minutes, label, true);
    }

    public Alarm(int hours, int minutes) {
        this(-1, hours, minutes, "", true);
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTimeString(Context context){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, this.hours);
        calendar.set(Calendar.MINUTE, this.minutes);

        java.text.DateFormat dateFormat = DateFormat.getTimeFormat(context);
        return dateFormat.format(calendar.getTime());
    }

    public long getTimeMillis(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, this.hours);
        calendar.set(Calendar.MINUTE, this.minutes);
        calendar.setTimeZone(TimeZone.getDefault());
        if(calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()){
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTimeInMillis();
    }

    public static List<Alarm> getAlarms(Context context){
        List<Alarm> alarms = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, null, null, null, null);
        if(cursor == null) return alarms;

        while (cursor.moveToNext()) {
            Alarm alarm = new Alarm(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getInt(4) == 1);
            alarms.add(alarm);
        }
        return alarms;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if(alarmId > -1)
            values.put(AlarmsDatabaseHelper.AlarmsColumns._ID, alarmId);
        values.put(AlarmsDatabaseHelper.AlarmsColumns.HOUR, hours);
        values.put(AlarmsDatabaseHelper.AlarmsColumns.MINUTES, minutes);
        values.put(AlarmsDatabaseHelper.AlarmsColumns.LABEL, label);
        values.put(AlarmsDatabaseHelper.AlarmsColumns.ENABLED, enabled);
        return values;
    }
}
