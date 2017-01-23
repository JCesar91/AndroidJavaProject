package com.example.juliocesar.alarmclock.SQLiteTools;

/**
 * Created by Julio Cesar on 12/4/2015.
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.juliocesar.alarmclock.setSettings;
import com.example.juliocesar.alarmclock.getTime;

public class AlarmDataBase extends SQLiteOpenHelper {


    public static final String DB_NAME = "alarmclock8.db";
    public static final int DB_VERSION = 1;

    public static final SimpleDateFormat sdf = new SimpleDateFormat(setSettings.DEFAULT_DATE_FORMAT);

    public AlarmDataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Alarm.getSql());
        db.execSQL(AlarmTime.getSql());
        db.execSQL(Messenger.getSql());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Alarm.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AlarmTime.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Messenger.TABLE_NAME);

        onCreate(db);
    }

    public void shred(SQLiteDatabase db) {
        db.delete(Messenger.TABLE_NAME, Messenger.COL_STATUS+" = ?", new String[]{Messenger.CANCELLED});
    }

    private final String populateSQL = getTime.concat("SELECT ",
            "a." + Alarm.COL_FROMDATE + ", ",
            "a." + Alarm.COL_TODATE + ", ",
            "at." + AlarmTime.COL_AT,
            " FROM " + Alarm.TABLE_NAME + " AS a",
            " JOIN " + AlarmTime.TABLE_NAME + " AS at",
            " ON a." + Alarm.COL_ID + " = at." + AlarmTime.COL_ALARMID,
            " WHERE a." + Alarm.COL_ID + " = ?");

    public void populate(long alarmId, SQLiteDatabase db) {

        String[] selectionArgs = {String.valueOf(alarmId)};
        Cursor c = db.rawQuery(populateSQL, selectionArgs);

        if (c.moveToFirst()) {
            Calendar cal = Calendar.getInstance();
            Messenger messenger = new Messenger();
            long now = System.currentTimeMillis();
            db.beginTransaction();
            try {
                do {
                    Date fromDate = sdf.parse(c.getString(0)); //yyyy-M-d
                    cal.setTime(fromDate);

                    //at
                    String[] tokens = c.getString(2).split(":"); //hh:mm
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(tokens[0]));
                    cal.set(Calendar.MINUTE, Integer.parseInt(tokens[1]));
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);


                        messenger.reset();
                        messenger.setAlarmId(alarmId);
                        messenger.setDateTime(cal.getTimeInMillis());
                        if (messenger.getDateTime() < now- getTime.MIN)
                            messenger.setStatus(Messenger.EXPIRED);
                        messenger.save(db);


                } while(c.moveToNext());

                db.setTransactionSuccessful();
            } catch (Exception e) {

            } finally {
                db.endTransaction();
            }
        }
        c.close();
    }

    private final String listSQL = getTime.concat("SELECT ",
            "a." + Alarm.COL_NAME + ", ",
            "am." + Messenger.COL_ID + ", ",
            "am." + Messenger.COL_DATETIME + ", ",
            "am." + Messenger.COL_STATUS,
            " FROM " + Alarm.TABLE_NAME + " AS a",
            " JOIN " + Messenger.TABLE_NAME + " AS am",
            " ON a." + Alarm.COL_ID + " = am." + Messenger.COL_ALARMID);

    /**
     * @param db
     * @param args {startTime, endTime}
     * @return cursor
     */
    public Cursor listNotifications(SQLiteDatabase db, String... args) {
        String selection = "am."+ Messenger.COL_STATUS+" != '"+ Messenger.CANCELLED+"'";
        selection += (args!=null && args.length>0 && args[0]!=null) ? " AND am."+ Messenger.COL_DATETIME+" >= "+args[0] : "";
        selection += (args!=null && args.length>1 && args[1]!=null) ? " AND am."+ Messenger.COL_DATETIME+" <= "+args[1] : "";

        String sql = getTime.concat(listSQL,
                " WHERE " + selection,
                " ORDER BY am." + Messenger.COL_DATETIME + " ASC");

        return db.rawQuery(sql, null);
    }
    //Called when alarm is deleted
    public int cancelNotification(SQLiteDatabase db, long id, boolean isAlarmId) {
        ContentValues cv = new ContentValues();
        cv.put(Messenger.COL_STATUS, Messenger.CANCELLED);
        return db.update(Messenger.TABLE_NAME,
                cv,
                (isAlarmId ? Messenger.COL_ALARMID : Messenger.COL_ID)+" = ?",
                new String[]{String.valueOf(id)});
    }

    public int cancelNotification(SQLiteDatabase db, String startTime, String endTime) {
        ContentValues cv = new ContentValues();
        cv.put(Messenger.COL_STATUS, Messenger.CANCELLED);
        return db.update(Messenger.TABLE_NAME,
                cv,
                Messenger.COL_DATETIME+" >= ? AND "+ Messenger.COL_DATETIME+" <= ?",
                new String[]{startTime, endTime});
    }

    public static final String getDateStr(int year, int month, int date) {
        return getTime.concat(year, "-", month, "-", date);
    }

    public static final String getTimeStr(int hour, int minute) {
        return getTime.concat(hour, ":", minute > 9 ? "" : "0", minute);
    }

}

