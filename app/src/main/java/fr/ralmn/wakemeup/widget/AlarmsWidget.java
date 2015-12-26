package fr.ralmn.wakemeup.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fr.ralmn.wakemeup.AlarmsDatabaseHelper;
import fr.ralmn.wakemeup.CalendarHelper;
import fr.ralmn.wakemeup.R;
import fr.ralmn.wakemeup.activities.AlarmListActivity;
import fr.ralmn.wakemeup.object.Alarm;

/**
 * Implementation of App Widget functionality.
 */
public class AlarmsWidget extends AppWidgetProvider {

    private static String ACTION_CLICK_WIDGET = "fr.ralmn.wakemeup.widget.click";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String firstAlarmStr = "";

        List<Alarm> alarms = Alarm.getAlarms(context, null, AlarmsDatabaseHelper.AlarmsColumns.ENABLED + "=1");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.alarms_widget);

        if (alarms.size() > 0) {

            // Construct the RemoteViews object
            Paint mPaint = new Paint();
            mPaint.setTextSize(28);
            mPaint.setFakeBoldText(true);
            mPaint.setTypeface(Typeface.DEFAULT);
            Rect bounds = new Rect();
            mPaint.getTextBounds("WED 00h00", 0, "WED 00h00".length(), bounds);
            int height = (appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)) / bounds.height();

            int width = (appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) + 0) / bounds.width();
            int nbAlarmsLine = Math.max(height, 1);
            int nbAlarmsColumns = Math.max(width, 1);

            int iMax = (int) Math.ceil(Math.min(nbAlarmsLine * nbAlarmsColumns, alarms.size()) / (double) nbAlarmsColumns);
            for(int i = 0; i < iMax; i++){
                int jMax = Math.min(nbAlarmsColumns , alarms.size() - ((i)*nbAlarmsColumns));
                for(int j = 0; j < jMax; j++){

                    Alarm alarm = alarms.get((i)*nbAlarmsColumns + j);
                    firstAlarmStr += (
                            new SimpleDateFormat("EEE", Locale.getDefault()).format(alarm.getNextAlarm().getTime())).toUpperCase() + " "
                            + alarm.getNextTimeString(context) + "   ";

                }

                firstAlarmStr += "\n";
            }
        }else{
            firstAlarmStr = "No next alarms";
        }

        views.setTextViewText(R.id.appwidget_text, firstAlarmStr);

        Intent clickIntent = new Intent(context, AlarmsWidget.class);
        clickIntent.setAction(ACTION_CLICK_WIDGET);
        views.setOnClickPendingIntent(R.id.appwidget_text, PendingIntent.getBroadcast(context, 0, clickIntent, 0));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Log.d("RALMN", "uw");
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);


            //PendingIntent.getBroadcast(context, 0, clickIntent, 0);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //Log.d("RALMN", intent.getAction());

        if (intent.getAction().equalsIgnoreCase(ACTION_CLICK_WIDGET)){
            Intent startIntent = new Intent(context, AlarmListActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startIntent);
        }else if(intent.getAction().toLowerCase().contains("update")){
            if(intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -903);
                updateAppWidget(context, AppWidgetManager.getInstance(context), id);
            }else if(intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)){
                int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                for(int id : ids){
                    updateAppWidget(context, AppWidgetManager.getInstance(context), id);
                }
            }
        }

    }
}

