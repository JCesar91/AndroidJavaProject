package com.example.juliocesar.alarmclock;

/**
 * Created by Julio Cesar on 12/4/2015.
 */
import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.example.juliocesar.alarmclock.SQLiteTools.AlarmDataBase;


public class setSettings extends Application {


    public static AlarmDataBase dbHelper;
    public static SQLiteDatabase db;
    public static SharedPreferences sp;

    public static final String DATE_RANGE = "date_range";
    public static final String VIBRATE_PREF = "vibrate_pref";
    public static final String RINGTONE_PREF = "ringtone_pref";
    public static final String SOUND_PREF = "sound_pref";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-M-d";

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        dbHelper = new AlarmDataBase(this);
        db = dbHelper.getWritableDatabase();
    }

    public static int getDateRange() {
        return Integer.parseInt(sp.getString(DATE_RANGE, "0"));
    }

    public static boolean isVibrate() {
        return sp.getBoolean(VIBRATE_PREF, true);
    }

    public static String getRingtone() {
        return sp.getString(RINGTONE_PREF, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }
    public static boolean isSound(){ return sp.getBoolean(SOUND_PREF, true);}

}

