package fr.ralmn.wakemeup;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AlarmsProvider extends ContentProvider {

    public static final String AUTHORITY = "fr.ralmn.wakemeup";

    private static final int ALARMS = 1;
    private static final int ALARMS_ID = 2;



    private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURLMatcher.addURI(AUTHORITY, "alarms", ALARMS);
        sURLMatcher.addURI(AUTHORITY, "alarms/#", ALARMS_ID);
    }

    private AlarmsDatabaseHelper mOpenHelper;


    @Override
    public boolean onCreate() {
        mOpenHelper = new AlarmsDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        int match = sURLMatcher.match(uri);

        switch (match){
            case ALARMS:
                qb.setTables(AlarmsDatabaseHelper.ALARMS_TABLE_NAME);
                break;
            case ALARMS_ID:
                qb.setTables(AlarmsDatabaseHelper.ALARMS_TABLE_NAME);
                qb.appendWhere(AlarmsDatabaseHelper.AlarmsColumns._ID + "=");
                qb.appendWhere(uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        if(ret == null){
            Log.e("WakeMeUp",".Alamrs query failed");
        }

        return ret;
    }

    @Override
    public String getType(Uri uri) {
        int match = sURLMatcher.match(uri);
        switch (match) {
            case ALARMS:
                return "vnd.android.cursor.dir/alarms";
            case ALARMS_ID:
                return "vnd.android.cursor.item/alarms";
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long rowId;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int match = sURLMatcher.match(uri);
        switch (match){
            case ALARMS:
                rowId = db.insert(AlarmsDatabaseHelper.ALARMS_TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Cannot insert from URL: " + uri);
        }

        Uri uriResult = ContentUris.withAppendedId(AlarmsDatabaseHelper.AlarmsColumns.CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(uriResult, null);
        return uriResult;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        String primaryKey;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURLMatcher.match(uri)) {
            case ALARMS:
                count = db.delete(AlarmsDatabaseHelper.ALARMS_TABLE_NAME, selection, selectionArgs);
                break;
            case ALARMS_ID:
                primaryKey = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    selection = AlarmsDatabaseHelper.AlarmsColumns._ID + "=" + primaryKey;
                } else {
                    selection = AlarmsDatabaseHelper.AlarmsColumns._ID + "=" + primaryKey +
                            " AND (" + selection + ")";
                }
                count = db.delete(AlarmsDatabaseHelper.ALARMS_TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete from URL: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count;
        String alarmId;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (sURLMatcher.match(uri)) {
            case ALARMS_ID:
                alarmId = uri.getLastPathSegment();
                count = db.update(AlarmsDatabaseHelper.ALARMS_TABLE_NAME, values,
                        AlarmsDatabaseHelper.AlarmsColumns._ID + "=" + alarmId,
                        null);
                break;
            default: {
                throw new UnsupportedOperationException(
                        "Cannot update URL: " + uri);
            }
        }
        Log.v("WakeMeUp","*** notifyChange() id: " + alarmId + " url " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
