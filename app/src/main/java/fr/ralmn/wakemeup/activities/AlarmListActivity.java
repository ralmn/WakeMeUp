package fr.ralmn.wakemeup.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import fr.ralmn.wakemeup.CalendarHelper;
import fr.ralmn.wakemeup.R;
import fr.ralmn.wakemeup.adapter.AlarmArrayAdapter;
import fr.ralmn.wakemeup.object.Alarm;
import fr.ralmn.wakemeup.object.AndroidCalendar;


public class AlarmListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);
        initSharedPreference();

        ListView alarmList = (ListView) findViewById(R.id.alarmsListView);

        List<Alarm> alarms = CalendarHelper.calculateWeekAlarms(this); //Alarm.getAlarms(this);


        alarmList.setAdapter(new AlarmArrayAdapter(this, R.layout.alarm_list_item, alarms));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
        Alarm a = new Alarm(calendar, "Test");
        Log.d("RALMN", "ALA : " + a.getTimeString(this));
        a.defineAlarm(this);

    }

    private void initSharedPreference(){
        SharedPreferences sharedPreferences = getSharedPreferences("fr.ralmn.wakemeup", MODE_PRIVATE);
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("RALMN", sharedPreferences.contains("calendars") + "");
        Log.d("RALMN", sharedPreferences.getAll().toString());
        Log.d("RALMN", defaultSharedPref.getAll().toString());
        if(!sharedPreferences.contains("calendars")){
            HashSet<String> calendarsId = new HashSet<>();
            List<AndroidCalendar> androidCalendars = CalendarHelper.getCalendars(this);
            for (AndroidCalendar androidCalendar : androidCalendars) {
                calendarsId.add(androidCalendar.getId() + "");
            }
            sharedPreferences.edit().putStringSet("calendars", calendarsId).apply();
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
        Log.d("RALMN", defaultSharedPref.getAll().toString());

        Log.d("RALMN", sharedPreferences.getAll().toString());


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
        }

        return super.onOptionsItemSelected(item);
    }
}
