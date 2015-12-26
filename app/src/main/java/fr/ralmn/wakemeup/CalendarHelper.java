package fr.ralmn.wakemeup;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ralmn.wakemeup.object.Alarm;
import fr.ralmn.wakemeup.object.AndroidCalendar;
import fr.ralmn.wakemeup.object.CalendarEvent;

/**
 * Created by ralmn on 20/09/15.
 */
public class CalendarHelper {

    public static List<AndroidCalendar> getCalendars(Context context){

        List<AndroidCalendar> calendars = new ArrayList<>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int code = context.checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR);
            if(code != PackageManager.PERMISSION_GRANTED){
                return calendars;
            }
        }

        Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, new String[]{
                CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.CALENDAR_COLOR}, null, null, null);

        if(cursor == null) return calendars;
        if(cursor.getCount() == 0){
            cursor.close();
            return calendars;
        }

        while (cursor.moveToNext()){
            AndroidCalendar calendar = new AndroidCalendar(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            );

            calendars.add(calendar);
        }
        cursor.close();
        return calendars;
    }

    public static AndroidCalendar getCalendar(Context context, int id){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int code = context.checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR);
            if(code != PackageManager.PERMISSION_GRANTED){
                return null;
            }
        }

        Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, new String[]{
                CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.CALENDAR_COLOR},
                "(" + CalendarContract.Calendars._ID + " = ? )", new String[]{id +""}, null);

        if(cursor == null) return null;
        if(cursor.getCount() == 0){
            cursor.close();
            return null;
        }

        cursor.moveToNext();

        AndroidCalendar calendar = new AndroidCalendar(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2)
        );
        cursor.close();

        return calendar;
    }

    public static List<CalendarEvent> getCalendarsWeekEvent(Context context){
        List<CalendarEvent> calendarEvents = new ArrayList<>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int code = context.checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR);
            if(code != PackageManager.PERMISSION_GRANTED){
                return calendarEvents;
            }
        }

        Set<String> calendarIds = context.getSharedPreferences("fr.ralmn.wakemeup", Context.MODE_PRIVATE).getStringSet("calendars", new HashSet<String>());

        String calendarIdsStr = "(" + Utils.joinSet(calendarIds, ", ") + ")";

        Calendar tomorow = Calendar.getInstance();
        tomorow.set(Calendar.HOUR, 0);
        tomorow.set(Calendar.MINUTE, 0);
        tomorow.add(Calendar.DAY_OF_YEAR, 1);


        Calendar nextWeek = Calendar.getInstance();
        nextWeek.add(Calendar.WEEK_OF_YEAR, 1);
        nextWeek.set(Calendar.HOUR, 0);
        nextWeek.set(Calendar.MINUTE, 0);
        nextWeek.add(Calendar.DAY_OF_YEAR, 1);



        String selection =
                "(" + CalendarContract.Events.CALENDAR_ID + " in " + calendarIdsStr
                + " and " + CalendarContract.Events.DTSTART + " > " + tomorow.getTimeInMillis()
                + " and " + CalendarContract.Events.DTSTART + " < "  + nextWeek.getTimeInMillis() + ")";

        Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, new String[]{
                CalendarContract.Events.CALENDAR_ID, CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART
        }, selection, new String[]{}, null);

        if(cursor == null) return calendarEvents;
        if(cursor.getCount() == 0){
            cursor.close();
            return calendarEvents;
        }

        while(cursor.moveToNext()){
            CalendarEvent calendarEvent = new CalendarEvent(
                    getCalendar(context, cursor.getInt(0)),
                    cursor.getInt(1),
                    cursor.getString(2),
                    Utils.getCalendarFromMillis(cursor.getLong(3))
            );
            calendarEvents.add(calendarEvent);
        }
        cursor.close();

        return calendarEvents;
    }

    public static List<CalendarEvent> getCalendarDayOffSetEvent(Context context, int offset){
        List<CalendarEvent> calendarEvents = new ArrayList<>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int code = context.checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR);
            if(code != PackageManager.PERMISSION_GRANTED){
                return calendarEvents;
            }
        }

        Set<String> calendarIds = context.getSharedPreferences("fr.ralmn.wakemeup", Context.MODE_PRIVATE).getStringSet("calendars", new HashSet<String>());
        //Log.d("RALMN", context.getSharedPreferences("fr.ralmn.wakemeup", Context.MODE_PRIVATE).getAll().toString());
        String calendarIdsStr = "(" + Utils.joinSet(calendarIds, ", ") + ")";

        Calendar tomorow = Calendar.getInstance();
        tomorow.set(Calendar.HOUR_OF_DAY, 0);
        tomorow.set(Calendar.MINUTE, 0);
        tomorow.add(Calendar.DAY_OF_YEAR, offset);



        Calendar nextWeek = Calendar.getInstance();
        nextWeek.set(Calendar.HOUR_OF_DAY, 23);
        nextWeek.set(Calendar.MINUTE, 59);
        nextWeek.add(Calendar.DAY_OF_YEAR, offset);


        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, tomorow.getTimeInMillis());
        ContentUris.appendId(builder, nextWeek.getTimeInMillis());

        String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.CALENDAR_ID,      // 0
                CalendarContract.Instances.EVENT_ID,         // 1
                CalendarContract.Instances.TITLE,          // 2
                CalendarContract.Instances.BEGIN          // 3
        };


        Cursor cursor = context.getContentResolver().query(builder.build(), INSTANCE_PROJECTION, CalendarContract.Events.CALENDAR_ID + " in " + calendarIdsStr, null,null);

        if(cursor == null) return calendarEvents;
        if(cursor.getCount() == 0){
            cursor.close();
            return calendarEvents;
        }

        while(cursor.moveToNext()){
            CalendarEvent calendarEvent = new CalendarEvent(
                    getCalendar(context, cursor.getInt(0)),
                    cursor.getInt(1),
                    cursor.getString(2),
                    Utils.getCalendarFromMillis(cursor.getLong(3))
            );
            calendarEvents.add(calendarEvent);
        }


        cursor.close();
        return calendarEvents;
    }

    public static List<Alarm> calculateWeekAlarms(Context context){
        Log.e("WAKEMEUP", "Calculate Week alarm !");
        List<Alarm> alarms = new ArrayList<>();

        Set<String> alarmsBefore = context.getSharedPreferences("fr.ralmn.wakemeup", Context.MODE_PRIVATE).getStringSet("alarmsBefore", new HashSet<String>());
        Calendar now = Calendar.getInstance();
        for(int i = 0; i <= 7;i++){
            CalendarEvent lowerEvent = null;
            for (CalendarEvent calendarEvent : getCalendarDayOffSetEvent(context, i)) {
                Log.d("RALMN " + i, calendarEvent.toString(context));
                if(calendarEvent.getStartEvent().before(now)) {
                    continue;
                }
                if(lowerEvent == null){
                    lowerEvent = calendarEvent;
                }else if(lowerEvent.getStartEvent().after(calendarEvent.getStartEvent())){
                    lowerEvent = calendarEvent;
                }
            }
            if(lowerEvent != null){
               //Log.d("RALMN", i +" : " + lowerEvent.toString(context));

                for (String alarmBefore : alarmsBefore) {
                    String[] split = alarmBefore.split(":");
                    int hour = Integer.parseInt(split[0]);
                    int minutes = Integer.parseInt(split[1]);

                    Calendar startAlarm = (Calendar) lowerEvent.getStartEvent().clone();
                    startAlarm.add(Calendar.HOUR_OF_DAY, -hour);
                    startAlarm.add(Calendar.MINUTE, -minutes);
                    if(startAlarm.before(now)) continue;
                    Alarm alarm = findOrCreateAlarm(context, startAlarm, lowerEvent.getTitle());
                    alarms.add(alarm);

                }
            }/*else{
                Log.d("RALMN", i + " : lenf" );
            }*/
        }

        for(Alarm alarm : Alarm.getAlarms(context))
            alarm.unDefineAlarm(context);


        context.getContentResolver().delete(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, null, null);

//        alarms.clear();
//        Calendar c = Calendar.getInstance();
//        Calendar c1 = Calendar.getInstance();
//        c.add(Calendar.SECOND, 15);
//        c1.add(Calendar.SECOND, 60+15);
//        Alarm tmp = new Alarm(c, "test");
//        alarms.add(tmp);
//        Alarm tmp1 = new Alarm(c1, "test");
//        alarms.add(tmp1);

        for (Alarm alarm : alarms) {
            context.getContentResolver().insert(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, alarm.toContentValues(context));
        }
        alarms = Alarm.getAlarms(context);

        return alarms;
    }

    private static Alarm findOrCreateAlarm(Context context, Calendar calendar, String title){
        Alarm alarm;
        Cursor cursor = context.getContentResolver().query(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, null,
                "("+ AlarmsDatabaseHelper.AlarmsColumns.DATE+" == ?)",
                new String[]{calendar.getTimeInMillis() + ""},null);


        if(cursor != null && cursor.moveToNext()) {

            alarm = Alarm.createAlarmFromOpenedCursor(cursor);
        }else{
            alarm = new Alarm(calendar, title);
        }
        cursor.close();
        return alarm;
    }

    public static Alarm calculateNextAlarm(Context context){
        List<Alarm> alarms = Alarm.getAlarms(context);
        if(alarms.size() == 0){
            Log.e("WAKEMEUP", "Next alarm : size 0 ! Calculate Week alarm !");
            calculateWeekAlarms(context);
            alarms = Alarm.getAlarms(context);
        }

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        Alarm nextAlarm = null;
        for (Alarm alarm : alarms) {
            if((nextAlarm == null ||alarm.getNextAlarm().before(nextAlarm.getNextAlarm()))&& alarm.getNextAlarm().after(now)){
                if(alarm.isEnabled()) {
                    nextAlarm = alarm;
                }else{
                    alarm.unDefineAlarm(context);
                }
            }else{
                alarm.unDefineAlarm(context);
            }
        }
        if(nextAlarm != null){
            //Log.d("RALMN", nextAlarm.toString(context));
            nextAlarm.defineAlarm(context);
            ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
            return nextAlarm;
        }else{
            ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            return null;
        }

    }

}
