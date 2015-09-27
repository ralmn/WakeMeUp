package fr.ralmn.wakemeup.object;


import android.content.Context;

import java.util.Calendar;

import fr.ralmn.wakemeup.Utils;

public class CalendarEvent {

    private AndroidCalendar calendar;

    private int eventId;

    private String title;

    private Calendar startEvent;

    public CalendarEvent(AndroidCalendar calendar, int eventId, String title, Calendar startEvent) {
        this.calendar = calendar;
        this.eventId = eventId;
        this.startEvent = startEvent;
        this.title = title;
    }

    public AndroidCalendar getCalendar() {
        return calendar;
    }

    public int getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public Calendar getStartEvent() {
        return startEvent;
    }

    public String toString(Context context) {
        return "CalendarEvent{" +
                "calendar=" + calendar +
                ", eventId=" + eventId +
                ", title='" + title + '\'' +
                ", startEvent=" + Utils.calendarToString(context, startEvent) +
                '}';
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "calendar=" + calendar +
                ", eventId=" + eventId +
                ", title='" + title + '\'' +
                ", startEvent=" + startEvent.getTimeInMillis() +
                '}';
    }
}
