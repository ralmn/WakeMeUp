package fr.ralmn.wakemeup.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        List<AndroidCalendar> calendar = CalendarHelper.getCalendars(this);
        Log.d("RALMN", "" + calendar.size());
        PreferenceCategory targetCategory = (PreferenceCategory) findPreference("calendar_category");

        SharedPreferences sharedPreferences = getSharedPreferences("fr.ralmn.wakemeup", MODE_PRIVATE);

        Set<String> selectedCalendars = sharedPreferences.getStringSet("calendars", new HashSet<String>());

        targetCategory.getSharedPreferences().edit().clear().apply();

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
                    sharedPreferences.edit().putStringSet("calendarsb",selectedCalendars).apply();
                    Log.d("RALMN", "Save edit : " + sharedPreferences.getAll().toString());

                    return true;
                }
            });

            targetCategory.addPreference(checkbox);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static boolean isSimplePreferences(Context context) {
        //noinspection PointlessBooleanExpression,ConstantConditions
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class CalendarPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_calendar);




        }
    }




}
