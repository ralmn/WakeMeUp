package fr.ralmn.wakemeup;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import fr.ralmn.wakemeup.widget.AlarmsWidget;

/**
 * Created by ralmn on 20/09/15.
 */
public class Utils {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    public static String PREF_NAME = "fr.ralmn.wakemeup";


    public static String joinSet(Set<String> set, String sep) {
        String result = null;
        if(set != null) {
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = set.iterator();
            if(it.hasNext()) {
                sb.append(it.next());
            }
            while(it.hasNext()) {
                sb.append(sep).append(it.next());
            }
            result = sb.toString();
        }
        return result;
    }

    public static Calendar getCalendarFromMillis(long millis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    public static String calendarToString(Context context,Calendar calendar){
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);

        return dateFormat.format(calendar.getTime()) + " " + timeFormat.format(calendar.getTime());
    }

    public static Calendar stringToCalendar(Context context, String str){
        SimpleDateFormat dateFormat = (SimpleDateFormat) android.text.format.DateFormat.getDateFormat(context);
        SimpleDateFormat timeFormat = (SimpleDateFormat) android.text.format.DateFormat.getTimeFormat(context);

        String fullPatern = dateFormat.toPattern() + " " + timeFormat.toPattern();

        SimpleDateFormat fullFormat = new SimpleDateFormat(fullPatern);

        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(fullFormat.parse(str));
        } catch (ParseException e) {
            return null;
        }

        return calendar;
    }

    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static void forceUpdateWidget(Context context){
        Log.d("RALMN", "Force update");
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(context,AlarmsWidget.class));
        for(int id : ids){
            Intent updateIntent = new Intent(context, AlarmsWidget.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            context.sendBroadcast(updateIntent);
        }
    }


}
