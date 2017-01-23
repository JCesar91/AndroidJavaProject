package com.example.juliocesar.alarmclock;

/**
 * Created by Julio Cesar on 12/4/2015.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.example.juliocesar.alarmclock.SQLiteTools.Alarm;
import com.example.juliocesar.alarmclock.SQLiteTools.Messenger;

public class getAlarm extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        long alarmMsgId = intent.getLongExtra(Messenger.COL_ID, -1);
        long alarmId = intent.getLongExtra(Messenger.COL_ALARMID, -1);

        Messenger messenger = new Messenger(alarmMsgId);
        messenger.setStatus(Messenger.EXPIRED);
        messenger.persist(setSettings.db);

        Alarm alarm = new Alarm(alarmId);
        alarm.load(setSettings.db);

        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(), 0);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context);

        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.notification)
                .setTicker(alarm.getName())
                .setContentTitle(alarm.getName())
                .setContentText(alarm.getName())
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setContentIntent(pi)
                .setContentInfo("Info");

        if (setSettings.isVibrate())
        {
            b.setVibrate(new long[] { 500,500,500,500,500,500,500,500,500 });
        }

        if(alarm.getSound()) {
            b.setSound(Uri.parse(setSettings.getRingtone()));
        }

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) alarmMsgId, b.build());

    }

}

