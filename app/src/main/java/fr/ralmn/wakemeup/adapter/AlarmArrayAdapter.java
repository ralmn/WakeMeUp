package fr.ralmn.wakemeup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

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

        Alarm alarm = this.alarms.get(position);

        TextView textTime = (TextView) rowView.findViewById(R.id.alarm_item_textTime);
        textTime.setText(alarm.getTimeString(context));

        Switch enabledSwitch = (Switch) rowView.findViewById(R.id.alarm_item_switch);
        enabledSwitch.setChecked(alarm.isEnabled());


        return rowView;
    }
}
