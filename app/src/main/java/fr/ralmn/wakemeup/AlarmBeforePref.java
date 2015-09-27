package fr.ralmn.wakemeup;

import android.content.Context;
import android.preference.Preference;
import android.view.View;

/**
 * Created by ralmn on 27/09/15.
 */
public class AlarmBeforePref extends Preference implements Preference.OnPreferenceClickListener {

    private View.OnLongClickListener longClickListener;
    private OnPreferenceClickListener otherClickListener;
    private DoubleClickListener doubleClickListener;

    private long lastDoubleClick = -1;

    public AlarmBeforePref(Context context) {
        super(context);
        super.setOnPreferenceClickListener(this);
    }

    @Override
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        otherClickListener = onPreferenceClickListener;
    }

    public boolean longClick(View v){
        return longClickListener.onLongClick(v);
    }

    public DoubleClickListener getDoubleClickListener() {
        return doubleClickListener;
    }

    public void setDoubleClickListener(DoubleClickListener doubleClickListener) {
        this.doubleClickListener = doubleClickListener;
    }

    public void setLongClickListener(View.OnLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        boolean res = false;
        boolean doubleClick = false;
        if(doubleClickListener != null){
            long now = System.currentTimeMillis();
            if(now - lastDoubleClick < 1000){
                res = doubleClickListener.onClick();
                lastDoubleClick = -1;
                doubleClick = true;
            }else{
                lastDoubleClick = now;
            }
        }
        if(otherClickListener != null && !doubleClick){
            res = otherClickListener.onPreferenceClick(preference);
        }
        return res;
    }

    public static interface DoubleClickListener{

        public boolean onClick();

    }

}
