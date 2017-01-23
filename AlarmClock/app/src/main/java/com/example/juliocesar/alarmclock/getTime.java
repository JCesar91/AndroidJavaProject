package com.example.juliocesar.alarmclock;

/**
 * Created by Julio Cesar on 12/4/2015.
 */


import com.example.juliocesar.alarmclock.SQLiteTools.AlarmDataBase;


public class getTime {

    public static final StringBuilder sb = new StringBuilder();

    public static final double MIN = 60 * 1000.0;
    public static final double HOUR = 60 * MIN;
    public static final double DAY = 24 * HOUR;
    public static final double MONTH = 30 * DAY;
    public static final double YEAR = 365 * DAY;

    public static String concat(Object... objects) {
        sb.setLength(0);
        for (Object obj : objects) {
            sb.append(obj);
        }
        return sb.toString();
    }

    public static String getActualTime(int hour, int minute) {
        if (hour < 12) return AlarmDataBase.getTimeStr(hour, minute) + " AM";
        else if (hour == 12) return AlarmDataBase.getTimeStr(12, minute) + " PM";
        else return AlarmDataBase.getTimeStr(hour - 12, minute) + " PM";
    }

}

