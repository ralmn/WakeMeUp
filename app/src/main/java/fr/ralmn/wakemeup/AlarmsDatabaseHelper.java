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

    private static final int VERSION = 3;

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
        if(oldVersion < 3){
            dropAlarmTable(db);
            createAlarmTable(db);
        }
    }

    private void dropAlarmTable(SQLiteDatabase db){
        db.execSQL("DROP TABLE " + ALARMS_TABLE_NAME +";");
    }

    private void createAlarmTable(SQLiteDatabase db){
        db.execSQL("CREATE TABLE " + ALARMS_TABLE_NAME + " (" +
                AlarmsColumns._ID + " INTEGER PRIMARY KEY," + //0
                AlarmsColumns.DATE + " TEXT NOT NULL, " + // 1
                AlarmsColumns.ENABLED + " INTEGER NOT NULL, " + //2
                AlarmsColumns.LABEL + " TEXT NOT NULL, " + //3
                AlarmsColumns.SNOOZE + " TEXT NOT NULL, " + // 4
                AlarmsColumns.STATE + " INTEGER NOT NULL DEFAULT -1);"); //5
    }

    public interface AlarmsColumns extends BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AlarmsProvider.AUTHORITY + "/alarms");

        public static final String DATE = "date";
        public static final String ENABLED = "enabled";
        public static final String LABEL = "label";
        public static final String STATE = "state";
        public static final String SNOOZE = "snooze";
    }

}
