package fr.ralmn.wakemeup.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import fr.ralmn.wakemeup.AlarmReceiver;
import fr.ralmn.wakemeup.CalendarHelper;
import fr.ralmn.wakemeup.R;
import fr.ralmn.wakemeup.Utils;
import fr.ralmn.wakemeup.adapter.AlarmArrayAdapter;
import fr.ralmn.wakemeup.object.Alarm;
import fr.ralmn.wakemeup.object.AndroidCalendar;


public class AlarmListActivity extends Activity {

    private static final int FETCH_PERMS = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("RALMN", String.valueOf(requestCode) + " - " + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));
        if (Arrays.asList(permissions).contains(Manifest.permission.READ_CALENDAR) && checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            Log.v("WAKEMEUP", "Perms Read caleander allowed");
            SharedPreferences sharedPreferences = getSharedPreferences("fr.ralmn.wakemeup", MODE_PRIVATE);
            if (!sharedPreferences.contains("calendars")) {
                HashSet<String> calendarsId = new HashSet<>();
                List<AndroidCalendar> androidCalendars = CalendarHelper.getCalendars(this);
                for (AndroidCalendar androidCalendar : androidCalendars) {
                    calendarsId.add(androidCalendar.getId() + "");
                }
                sharedPreferences.edit().putStringSet("calendars", calendarsId).apply();
            }
            ListView alarmList = (ListView) findViewById(R.id.alarmsListView);
            List<Alarm> alarms = CalendarHelper.calculateWeekAlarms(this); //Alarm.getAlarms(this);
            Collections.sort(alarms);
            CalendarHelper.calculateNextAlarm(this);
            alarmList.setAdapter(new AlarmArrayAdapter(this, R.layout.alarm_list_item, alarms));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerReceiver(new AlarmReceiver(), new IntentFilter(AlarmReceiver.CALCULATE_ACTION));
        setContentView(R.layout.activity_alarm_list);

        ListView alarmList = (ListView) findViewById(R.id.alarmsListView);
        List<Alarm> alarms = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED) {
            if(!shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
                requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, FETCH_PERMS);
            }
            else {
                Log.i("WAKEMEUP", "Rationale");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.perms_rational_calendar_title);
                builder.setMessage(R.string.perms_rational_calendar_message);
                builder.setPositiveButton(R.string.perms_rational_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, FETCH_PERMS);
                    }
                });
                builder.setNegativeButton(R.string.perms_rational_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        quit();
                    }
                });
                builder.show().show();
                return;
            }
        }

        initSharedPreference();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            alarms = CalendarHelper.calculateWeekAlarms(this); //Alarm.getAlarms(this);
            Collections.sort(alarms);
            CalendarHelper.calculateNextAlarm(this);
        }

        alarmList.setAdapter(new AlarmArrayAdapter(this, R.layout.alarm_list_item, alarms));

//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.SECOND, 10);
//        Alarm a = new Alarm(calendar, "Test");
//        a.defineAlarm(this);

        startAutoCheck(this);

    }

    private void initSharedPreference() {

        SharedPreferences sharedPreferences = getSharedPreferences(Utils.PREF_NAME, MODE_PRIVATE);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED){

        if (!sharedPreferences.contains("calendars")) {
                HashSet<String> calendarsId = new HashSet<>();
                List<AndroidCalendar> androidCalendars = CalendarHelper.getCalendars(this);
                for (AndroidCalendar androidCalendar : androidCalendars) {
                    calendarsId.add(androidCalendar.getId() + "");
                }
                sharedPreferences.edit().putStringSet("calendars", calendarsId).apply();
            }
        }


        if(!sharedPreferences.contains("alarmsBefore") || sharedPreferences.getStringSet("alarmsBefore", new HashSet<String>()).size() == 0){
            HashSet<String> alarmsBefore = new HashSet<>();
            alarmsBefore.add("2:00");
            alarmsBefore.add("1:55");
            alarmsBefore.add("1:52");
            sharedPreferences.edit().putStringSet("alarmsBefore", alarmsBefore).putLong("timer", System.currentTimeMillis()).apply();

        }

        if(!sharedPreferences.contains("default_vibrate")){
            sharedPreferences.edit().putBoolean("default_vibrate", true).apply();
        }
        if(!sharedPreferences.contains("default_ringtone")){
            sharedPreferences.edit().putString("default_ringtone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()).apply();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView alarmList = (ListView) findViewById(R.id.alarmsListView);

        List<Alarm> alarms = Alarm.getAlarms(this);
        Collections.sort(alarms);

        alarmList.setAdapter(new AlarmArrayAdapter(this, R.layout.alarm_list_item, alarms));
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
            CalendarHelper.calculateNextAlarm(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_alarm_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }else if(id == R.id.action_about){
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static void startAutoCheck(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AlarmReceiver.CALCULATE_ACTION);
        //intent.setAction();

        PendingIntent startBroadcast = PendingIntent.getBroadcast(context,
                -999, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar c = Calendar.getInstance();

        c.set(Calendar.SECOND, 0);
       c.set(Calendar.HOUR_OF_DAY, 20);
        c.set(Calendar.MINUTE, 0);
        if(c.before(Calendar.getInstance()))
            c.add(Calendar.DAY_OF_YEAR, 1);


        Log.d("RALMN", "next update at : " + DateFormat.getDateFormat(context).format(c.getTime()) + " - " + DateFormat.getTimeFormat(context).format(c.getTime()));
        alarmManager.cancel(startBroadcast);


        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), startBroadcast);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), startBroadcast);
        }
    }

    public void quit() {
        finish();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }

}
