package com.example.juliocesar.alarmclock.SQLiteTools;

/**
 * Created by Julio Cesar on 12/4/2015.
 */
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.juliocesar.alarmclock.getTime;


public class Alarm extends AbstractModel {

    public static final String TABLE_NAME = "alarm";
    public static final String COL_ID = AbstractModel.COL_ID;
    public static final String COL_CREATEDTIME = "created_time";
    public static final String COL_MODIFIEDTIME = "modified_time";
    public static final String COL_NAME = "name";
    public static final String COL_FROMDATE = "from_date";
    public static final String COL_TODATE = "to_date";
    public static final String COL_SOUND = "sound";

    static String getSql() {
        return getTime.concat("CREATE TABLE ", TABLE_NAME, " (",
                AbstractModel.getSql(),
                COL_CREATEDTIME, " INTEGER, ",
                COL_MODIFIEDTIME, " INTEGER, ",
                COL_NAME, " TEXT, ",
                COL_FROMDATE, " DATE, ",
                COL_TODATE, " DATE, ",
                COL_SOUND, " INTEGER",
                ");");
    }

    long save(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        long now = System.currentTimeMillis();
        cv.put(COL_CREATEDTIME, now);
        cv.put(COL_MODIFIEDTIME, now);
        cv.put(COL_NAME, name==null ? "" : name);
        cv.put(COL_FROMDATE, fromDate);
        cv.put(COL_TODATE, toDate);
        cv.put(COL_SOUND, sound ? 1 : 0);

        return db.insert(TABLE_NAME, null, cv);
    }

    boolean update(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        super.update(cv);
        cv.put(COL_MODIFIEDTIME, System.currentTimeMillis());
        if (name != null)
            cv.put(COL_NAME, name);
        if (fromDate != null)
            cv.put(COL_FROMDATE, fromDate);
        if (toDate != null)
            cv.put(COL_TODATE, toDate);
        if (sound != null)
            cv.put(COL_SOUND, sound ? 1 : 0);

        return db.update(TABLE_NAME, cv, COL_ID + " = ?", new String[]{String.valueOf(id)})
                == 1;
    }

    public boolean load(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, null, COL_ID+" = ?", new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                reset();
                super.load(cursor);
                createdTime = cursor.getLong(cursor.getColumnIndex(COL_CREATEDTIME));
                modifiedTime = cursor.getLong(cursor.getColumnIndex(COL_MODIFIEDTIME));
                name = cursor.getString(cursor.getColumnIndex(COL_NAME));
                fromDate = cursor.getString(cursor.getColumnIndex(COL_FROMDATE));
                toDate = cursor.getString(cursor.getColumnIndex(COL_TODATE));
                sound = cursor.getInt(cursor.getColumnIndex(COL_SOUND)) == 1;
                return true;
            }
            return false;
        } finally {
            cursor.close();
            //cursor = null;
        }
    }

    private long createdTime;
    private long modifiedTime;
    private String name;
    private String fromDate;
    private String toDate;
    private Boolean sound = Boolean.FALSE;
    private List<AlarmTime> occurrences;

    public void reset() {
        super.reset();
        createdTime = 0;
        modifiedTime = 0;
        name = null;
        fromDate = null;
        toDate = null;
        sound = Boolean.FALSE;
        occurrences = null;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFromDate() {
        return fromDate;
    }
    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public Boolean getSound() {
        return sound;
    }
    public void setSound(Boolean sound) {
        this.sound = sound;
    }

    public Alarm() {}

    public Alarm(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (obj.getClass() != this.getClass()))
            return false;

        return id == ((Alarm)obj).id;
    }

    @Override
    public int hashCode() {
        return 1;
    }

}
