package com.example.juliocesar.alarmclock;

/**
 * Created by Julio Cesar on 12/4/2015.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class setAlarm extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, AlarmService.class);
        service.setAction(AlarmService.CREATE);
        context.startService(service);
    }

}

