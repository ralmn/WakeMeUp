package fr.ralmn.wakemeup;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ralmn on 19/09/15.
 */
public class AlarmsDatabaseHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    static final String DATABASE_NAME = "alarms.db";
    static final String ALARMS_TABLE_NAME = "alarms";

    private final Context mContext;

    public AlarmsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createAlarmTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createAlarmTable(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + ALARMS_TABLE_NAME + " (" +
                AlarmsColumns._ID + " INTEGER PRIMARY KEY," +
                AlarmsColumns.HOUR + " INTEGER NOT NULL, " +
                AlarmsColumns.MINUTES + " INTEGER NOT NULL, " +
                AlarmsColumns.ENABLED + " INTEGER NOT NULL, " +
                AlarmsColumns.LABEL + " TEXT NOT NULL);");
    }

    public interface AlarmsColumns extends BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AlarmsProvider.AUTHORITY + "/alarms");

        public static final String HOUR = "hour";
        public static final String MINUTES = "minute";
        public static final String ENABLED = "enabled";
        public static final String LABEL = "label";
    }

}
