package fr.ralmn.wakemeup.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import fr.ralmn.wakemeup.CalendarHelper;
import fr.ralmn.wakemeup.R;
import fr.ralmn.wakemeup.adapter.AlarmArrayAdapter;
import fr.ralmn.wakemeup.object.Alarm;
import fr.ralmn.wakemeup.object.AndroidCalendar;


public class AlarmListActivity extends Activity {

    private static final int FETCH_PERMS = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d("RALMN", String.valueOf(requestCode) + " - " + Arrays.toString(permissions) + " " + Arrays.toString(grantResults));
        if (Arrays.asList(permissions).contains(Manifest.permission.READ_CALENDAR) && checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
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
        setContentView(R.layout.activity_alarm_list);
        initSharedPreference();

        ListView alarmList = (ListView) findViewById(R.id.alarmsListView);
        List<Alarm> alarms = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, FETCH_PERMS);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            alarms = CalendarHelper.calculateWeekAlarms(this); //Alarm.getAlarms(this);
            Collections.sort(alarms);
            CalendarHelper.calculateNextAlarm(this);
        }

        alarmList.setAdapter(new AlarmArrayAdapter(this, R.layout.alarm_list_item, alarms));

//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.SECOND, 10);
//        Alarm a = new Alarm(calendar, "Test");
//        a.defineAlarm(this);

    }

    private void initSharedPreference() {

        SharedPreferences sharedPreferences = getSharedPreferences("fr.ralmn.wakemeup", MODE_PRIVATE);
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Log.d("Ralmn", sharedPreferences.getAll().toString());
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED){
            if (!sharedPreferences.contains("calendars")) {
                HashSet<String> calendarsId = new HashSet<>();
                List<AndroidCalendar> androidCalendars = CalendarHelper.getCalendars(this);
                for (AndroidCalendar androidCalendar : androidCalendars) {
                    calendarsId.add(androidCalendar.getId() + "");
                }
                sharedPreferences.edit().putStringSet("calendars", calendarsId).apply();
            }
        }else{
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, FETCH_PERMS);
        }

        if(!sharedPreferences.contains("alarmsBefore")){
            HashSet<String> alarmsBefore = new HashSet<>();
            alarmsBefore.add("2:00");
            alarmsBefore.add("1:55");
            alarmsBefore.add("1:52");
            sharedPreferences.edit().putStringSet("alarmsBefore", alarmsBefore).apply();

        }

        if(!defaultSharedPref.contains("default_vibrate")){
            defaultSharedPref.edit().putBoolean("default_vibrate", true).apply();
        }
        if(!defaultSharedPref.contains("default_ringtone")){
            defaultSharedPref.edit().putString("default_ringtone", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()).apply();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        ListView alarmList = (ListView) findViewById(R.id.alarmsListView);

        List<Alarm> alarms = Alarm.getAlarms(this);
        Collections.sort(alarms);

        alarmList.setAdapter(new AlarmArrayAdapter(this, R.layout.alarm_list_item, alarms));
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||checkCallingOrSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
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


}
