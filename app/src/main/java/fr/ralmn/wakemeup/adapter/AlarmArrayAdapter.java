package fr.ralmn.wakemeup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import fr.ralmn.wakemeup.CalendarHelper;
import fr.ralmn.wakemeup.R;
import fr.ralmn.wakemeup.object.Alarm;

public class AlarmArrayAdapter extends ArrayAdapter<Alarm> {

    private Context context;
    private List<Alarm> alarms;
    private int ressourceId;

    public AlarmArrayAdapter(Context context, int resource, List<Alarm> objects) {
        super(context, resource, objects);
        this.context = context;
        this.ressourceId = resource;
        this.alarms = objects;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(this.ressourceId, parent, false);

        final Alarm alarm = this.alarms.get(position);

        TextView textTime = (TextView) rowView.findViewById(R.id.alarm_item_textTime);
        TextView textDate = (TextView) rowView.findViewById(R.id.alarm_item_dateText);

        textTime.setText(alarm.getTimeString(context));
        String date = new SimpleDateFormat("EEEE d", Locale.getDefault()).format(alarm.getDate().getTime());
        textDate.setText(date.substring(0, 1).toUpperCase() + date.substring(1));

        Switch enabledSwitch = (Switch) rowView.findViewById(R.id.alarm_item_switch);
        enabledSwitch.setChecked(alarm.isEnabled());
        enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarm.setEnabled(isChecked);
                alarm.update(context);
                if(!isChecked)
                    alarm.unDefineAlarm(context);
                CalendarHelper.calculateNextAlarm(context);
            }
        });


        return rowView;
    }
}
