package fr.ralmn.wakemeup;

import android.content.Context;
import android.database.Cursor;
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

        Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, new String[]{
                CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.CALENDAR_COLOR}, null, null, null);

        while (cursor.moveToNext()){
            AndroidCalendar calendar = new AndroidCalendar(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
            );

            calendars.add(calendar);
        }
        return calendars;
    }


    public static AndroidCalendar getCalendar(Context context, int id){

        Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, new String[]{
                CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CalendarContract.Calendars.CALENDAR_COLOR},
                "(" + CalendarContract.Calendars._ID + " = ? )", new String[]{id +""}, null);

        if(cursor.getCount() == 0) return null;

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
        Set<String> calendarIds = context.getSharedPreferences("fr.ralmn.wakemeup", Context.MODE_PRIVATE).getStringSet("calendars", new HashSet<String>());

        String calendarIdsStr = "(" + Utils.joinSet(calendarIds, ", ") + ")";

        Calendar tomorow = Calendar.getInstance();
        tomorow.set(Calendar.HOUR_OF_DAY, 0);
        tomorow.set(Calendar.MINUTE, 0);
        tomorow.add(Calendar.DAY_OF_YEAR, offset);



        Calendar nextWeek = Calendar.getInstance();
        nextWeek.set(Calendar.HOUR_OF_DAY, 23);
        nextWeek.set(Calendar.MINUTE, 59);
        nextWeek.add(Calendar.DAY_OF_YEAR, offset);

        String selection =
                "(" + CalendarContract.Events.CALENDAR_ID + " in " + calendarIdsStr
                        + " and " + CalendarContract.Events.DTSTART + " > " + Calendar.getInstance().getTimeInMillis()
                        + " and " + CalendarContract.Events.DTSTART + " > " + tomorow.getTimeInMillis()
                        + " and " + CalendarContract.Events.DTSTART + " < "  + nextWeek.getTimeInMillis() + " and " + CalendarContract.Events.ALL_DAY + "=0)";

        Cursor cursor = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, new String[]{
                CalendarContract.Events.CALENDAR_ID, CalendarContract.Events._ID, CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART
        }, selection, new String[]{}, null);


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


    public static ArrayList<Alarm> calculateWeekAlarms(Context context){

        context.getContentResolver().delete(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, "", null);

        ArrayList<Alarm> alarms = new ArrayList<>();

        Set<String> alarmsBefore = context.getSharedPreferences("fr.ralmn.wakemeup", Context.MODE_PRIVATE).getStringSet("alarmsBefore", new HashSet<String>());
        Calendar now = Calendar.getInstance();
        for(int i = 1; i <= 7;i++){
            CalendarEvent lowerEvent = null;
            for (CalendarEvent calendarEvent : getCalendarDayOffSetEvent(context, i)) {
                if(lowerEvent == null){
                    lowerEvent = calendarEvent;
                }else if(lowerEvent.getStartEvent().getTimeInMillis() > calendarEvent.getStartEvent().getTimeInMillis()){
                    lowerEvent = calendarEvent;
                }
            }
            if(lowerEvent != null){

                for (String alarmBefore : alarmsBefore) {
                    String[] split = alarmBefore.split(":");
                    int hour = Integer.parseInt(split[0]);
                    int minutes = Integer.parseInt(split[1]);

                    Calendar startAlarm = (Calendar) lowerEvent.getStartEvent().clone();
                    startAlarm.add(Calendar.HOUR_OF_DAY, -hour);
                    startAlarm.add(Calendar.MINUTE, -minutes);
                    if(startAlarm.before(now)) continue;
                    Alarm alarm = new Alarm(
                            startAlarm,
                            lowerEvent.getTitle()
                    );
                    alarms.add(alarm);
                    context.getContentResolver().insert(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, alarm.toContentValues(context));
                }
            }
        }



        return alarms;
    }


    public static void calculateNextAlarm(Context context){
        List<Alarm> alarms = Alarm.getAlarms(context);

        if(alarms.size() == 0){
            calculateWeekAlarms(context);
            return;
        }

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        Alarm nextAlarm = null;
        for (Alarm alarm : alarms) {
            if((nextAlarm == null || alarm.getNextAlarm().before(nextAlarm.getNextAlarm()) )&& alarm.getNextAlarm().after(now)){
                nextAlarm = alarm;
            }
        }
        if(nextAlarm != null){
            Log.d("RALMN", nextAlarm.toString(context));
            nextAlarm.defineAlarm(context);
        }

    }

}
