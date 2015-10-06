package fr.ralmn.wakemeup.activities;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.ralmn.wakemeup.AlarmBeforePref;
import fr.ralmn.wakemeup.CalendarHelper;
import fr.ralmn.wakemeup.R;
import fr.ralmn.wakemeup.object.AndroidCalendar;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = true;


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();

        ListView listView = getListView();
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ListAdapter listAdapter = listView.getAdapter();
                Object obj = listAdapter.getItem(position);
                return obj != null && obj instanceof AlarmBeforePref && ((AlarmBeforePref) obj).longClick(view);
            }
        });
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    @SuppressWarnings("deprecation")
    private void setupSimplePreferencesScreen() {

        addPreferencesFromResource(R.xml.pref_general);
        addPreferencesFromResource(R.xml.pref_calendar);
        addPreferencesFromResource(R.xml.pref_alarmsbefore);

        List<AndroidCalendar> calendar = CalendarHelper.getCalendars(this);
        PreferenceCategory calendarCategory = (PreferenceCategory) findPreference("calendar_category");

        final SharedPreferences sharedPreferences = getSharedPreferences("fr.ralmn.wakemeup", MODE_PRIVATE);

        Set<String> selectedCalendars = sharedPreferences.getStringSet("calendars", new HashSet<String>());

        calendarCategory.getSharedPreferences().edit().clear().apply();

        for (final AndroidCalendar androidCalendar : calendar) {


            CheckBoxPreference checkbox = new CheckBoxPreference(this);
            checkbox.setKey("calendar_" + androidCalendar.getId());

            checkbox.setTitle(androidCalendar.getName());
            checkbox.setChecked(selectedCalendars.contains(androidCalendar.getId() + ""));
            checkbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    SharedPreferences sharedPreferences = SettingsActivity.this.getSharedPreferences("fr.ralmn.wakemeup", MODE_PRIVATE);

                    boolean checked = (boolean) newValue;

                    Set<String> selectedCalendars = sharedPreferences.getStringSet("calendars", new HashSet<String>());
                    if(selectedCalendars == null) selectedCalendars = new HashSet<>();

                    if(checked){
                        selectedCalendars.add(androidCalendar.getId() +"");
                    }else{
                        selectedCalendars.remove(androidCalendar.getId() +"");
                    }
                    sharedPreferences.edit().putLong("time", System.currentTimeMillis()).putStringSet("calendars",selectedCalendars).apply();

                    return true;
                }
            });

            calendarCategory.addPreference(checkbox);
        }

        final PreferenceGroup alarmBeforeScreen = (PreferenceGroup) findPreference("alarmsbefore_category");
        final PreferenceCategory alarmBeforeItemsCategory = (PreferenceCategory) findPreference("alarmsbefore_items_category");
        final Set<String> alarmsBefore = sharedPreferences.getStringSet("alarmsBefore", new HashSet<String>());

        for (String time : alarmsBefore) {

            AlarmBeforePref pref = createAlarmBeforePref(time);
            alarmBeforeItemsCategory.addPreference(pref);
        }

        Preference addPref = new Preference(this);
        addPref.setTitle(R.string.pref_add);

        addPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String newTime = hourOfDay + ":" + (minute < 10 ? "0"+ minute : minute);
                        AlarmBeforePref pref = createAlarmBeforePref(newTime);
                        alarmBeforeItemsCategory.addPreference(pref);
                        alarmsBefore.add(newTime);
                        sharedPreferences.edit().putLong("time", System.currentTimeMillis()).putStringSet("alarmsBefore", alarmsBefore).apply();
                    }
                }, 1, 0, true);
                timePickerDialog.show();


                return false;
            }
        });
        addPref.setIcon(android.R.drawable.ic_input_add);
        alarmBeforeScreen.addPreference(addPref);
    }



    public AlarmBeforePref createAlarmBeforePref(String time){
        final AlarmBeforePref prf = new AlarmBeforePref(this);
        prf.setTitle(time);
        prf.setLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PreferenceCategory alarmBeforeCategory = (PreferenceCategory) findPreference("alarmsbefore_items_category");
                SharedPreferences sharedPreferences =  getSharedPreferences("fr.ralmn.wakemeup", MODE_PRIVATE);
                Set<String> alarmsBefore = sharedPreferences.getStringSet("alarmsBefore", new HashSet<String>());
                alarmsBefore.remove(prf.getTitle().toString());
                sharedPreferences.edit().putLong("time", System.currentTimeMillis()).putStringSet("alarmsBefore", alarmsBefore).apply();
                alarmBeforeCategory.removePreference(prf);
                return true;
            }
        });
        prf.setDoubleClickListener(new AlarmBeforePref.DoubleClickListener() {
            @Override
            public boolean onClick() {
                Toast.makeText(SettingsActivity.this, R.string.pref_alarmbefore_tap, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        prf.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String oldTime = prf.getTitle().toString();
                String[] split = oldTime.split(":");
                int hour = Integer.parseInt(split[0]);
                int minute = Integer.parseInt(split[1]);
                TimePickerDialog timePickerDialog = new TimePickerDialog(SettingsActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        String newTime = hourOfDay + ":" + (minute < 10 ? "0"+ minute : minute);
                        SharedPreferences sharedPreferences = getSharedPreferences("fr.ralmn.wakemeup", MODE_PRIVATE);
                        Set<String> alarmsBefore = sharedPreferences.getStringSet("alarmsBefore", new HashSet<String>());
                        alarmsBefore.remove(oldTime);
                        alarmsBefore.add(newTime);
                        sharedPreferences.edit().putLong("time", System.currentTimeMillis()).putStringSet("alarmsBefore", alarmsBefore).apply();
                        prf.setTitle(newTime);
                    }
                }, hour, minute, true);
                timePickerDialog.show();
                return true;
            }
        });
        return prf;
    }




}
