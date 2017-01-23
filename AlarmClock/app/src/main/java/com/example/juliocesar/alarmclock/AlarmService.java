package com.example.juliocesar.alarmclock;

/**
 * Created by Julio Cesar on 12/4/2015.
 */

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import com.example.juliocesar.alarmclock.SQLiteTools.Alarm;
import com.example.juliocesar.alarmclock.SQLiteTools.Messenger;

public class AlarmService extends IntentService {

    private static final String TAG = "AlarmService";

    public static final String POPULATE = "POPULATE";
    public static final String CREATE = "CREATE";
    public static final String CANCEL = "CANCEL";

    private IntentFilter matcher;

    public AlarmService() {
        super(TAG);
        matcher = new IntentFilter();
        matcher.addAction(POPULATE);
        matcher.addAction(CREATE);
        matcher.addAction(CANCEL);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        String alarmId = intent.getStringExtra(Messenger.COL_ALARMID);
        String alarmMsgId = intent.getStringExtra(Messenger.COL_ID);
        String startTime = intent.getStringExtra(Alarm.COL_FROMDATE);
        String endTime = intent.getStringExtra(Alarm.COL_TODATE);

        if (matcher.matchAction(action)) {
            if (POPULATE.equals(action)) {
                setSettings.dbHelper.populate(Long.parseLong(alarmId), setSettings.db);
                execute(CREATE, alarmId);
            }

            if (CREATE.equals(action)) {
                execute(CREATE, alarmId, alarmMsgId, startTime, endTime);
            }

            if (CANCEL.equals(action)) {
                execute(CANCEL, alarmId, alarmMsgId, startTime, endTime);
                setSettings.dbHelper.shred(setSettings.db);
            }
        }
    }

    /**
     * @param action
     * @param args {alarmId, alarmMsgId, startTime, endTime}
     */
    private void execute(String action, String... args) {
        Intent i;
        PendingIntent pi;
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Cursor c;

        String alarmId = (args!=null && args.length>0) ? args[0] : null;
        String alarmMsgId = (args!=null && args.length>1) ? args[1] : null;
        String startTime = (args!=null && args.length>2) ? args[2] : null;
        String endTime = (args!=null && args.length>3) ? args[3] : null;

        String status = CANCEL.equals(action) ? Messenger.CANCELLED : Messenger.ACTIVE;

        if (alarmMsgId != null) {
            c = setSettings.db.query(Messenger.TABLE_NAME, null, Messenger.COL_ID+" = ?", new String[]{alarmMsgId}, null, null, null);

        } else {
            c = Messenger.list(setSettings.db, alarmId, startTime, endTime, status);
        }

        if (c.moveToFirst()) {
            long now = System.currentTimeMillis();
            long time, diff;
            do {
                i = new Intent(this, getAlarm.class);
                i.putExtra(Messenger.COL_ID, c.getLong(c.getColumnIndex(Messenger.COL_ID)));
                i.putExtra(Messenger.COL_ALARMID, c.getLong(c.getColumnIndex(Messenger.COL_ALARMID)));

                pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                time = c.getLong(c.getColumnIndex(Messenger.COL_DATETIME));
                diff = time-now + (long) getTime.MIN;
                if (CREATE.equals(action)) {
                    if (diff > 0 && diff < getTime.YEAR)
                        am.set(AlarmManager.RTC_WAKEUP, time, pi);

                } else if (CANCEL.equals(action)) {
                    am.cancel(pi);
                }
            } while(c.moveToNext());
        }

        c.close();
    }

}

