package com.nayak.alarmapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Alarm_Manager";
    private static final String TABLE_NAME = "Alarm";

    private static final String COLUMN_ALARM_ID = "Alarm_Id";
    private static final String COLUMN_ALARM_HOUR = "Alarm_Hour";
    private static final String COLUMN_ALARM_MINUTE = "Alarm_Minute";
    private static final String COLUMN_ALARM_STATUS = "Alarm_Status";
    private static final String COLUMN_ALARM_NAME = "Alarm_Name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // ✅ Added proper spacing and commas
        String script = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ALARM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ALARM_HOUR + " INTEGER, "
                + COLUMN_ALARM_MINUTE + " INTEGER, "
                + COLUMN_ALARM_STATUS + " INTEGER, "
                + COLUMN_ALARM_NAME + " TEXT"
                + ")";
        db.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ✅ Added space after IF EXISTS
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert alarm and return inserted id
    public long addAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALARM_HOUR, alarm.getHours());
        values.put(COLUMN_ALARM_MINUTE, alarm.getMinutes());
        values.put(COLUMN_ALARM_STATUS, alarm.getStatus() ? 1 : 0);
        values.put(COLUMN_ALARM_NAME, alarm.getName());

        long id = db.insert(TABLE_NAME, null, values);
        if (id != -1) {
            alarm.setId((int) id);
        }
        db.close();
        return id;
    }

    // ✅ Get all alarms
    public List<Alarm> getAllAlarms() {
        List<Alarm> alarmList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Alarm alarm = new Alarm();
                alarm.setId(cursor.getInt(0));
                alarm.setHours(cursor.getInt(1));
                alarm.setMinutes(cursor.getInt(2));
                alarm.setStatus(cursor.getInt(3) != 0);
                alarm.setName(cursor.getString(4));
                alarmList.add(alarm);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return alarmList;
    }

    // ✅ Update alarm
    public int updateAlarm(Alarm alarm) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ALARM_HOUR, alarm.getHours());
        values.put(COLUMN_ALARM_MINUTE, alarm.getMinutes());
        values.put(COLUMN_ALARM_STATUS, alarm.getStatus());
        values.put(COLUMN_ALARM_NAME, alarm.getName());

        return db.update(TABLE_NAME, values, COLUMN_ALARM_ID + " = ?",
                new String[]{String.valueOf(alarm.getId())});
    }

    // ✅ Optional: delete an alarm
    public void deleteAlarm(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ALARM_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}
