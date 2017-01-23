package com.example.juliocesar.alarmclock;

/**
 * Created by Julio Cesar on 12/4/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;


import com.example.juliocesar.alarmclock.SQLiteTools.Alarm;
import com.example.juliocesar.alarmclock.SQLiteTools.Messenger;
import com.example.juliocesar.alarmclock.SQLiteTools.AlarmTime;
import com.example.juliocesar.alarmclock.SQLiteTools.AlarmDataBase;


public class NewAlarm extends Activity {


    private EditText msgEdit;
    private CheckBox soundCb;
    private DatePicker datePicker;
    private TimePicker timePicker;


    private SQLiteDatabase db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Add Alarm");
        setContentView(R.layout.addalarm);
        findViews();
        db = setSettings.db;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void findViews() {
        msgEdit = (EditText) findViewById(R.id.alarmName);
        soundCb = (CheckBox) findViewById(R.id.ringOn);
        datePicker = (DatePicker) findViewById(R.id.selectDay);
        timePicker = (TimePicker) findViewById(R.id.selectTime);


    }

    private boolean validate() {
        if (TextUtils.isEmpty(msgEdit.getText())) {
            msgEdit.requestFocus();
            Toast.makeText(this, "Enter Alarm Name", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /* Save */
    public void createNewAlarm(View v) {
        if (!validate()) return;

        Alarm alarm = new Alarm();
        alarm.setName(msgEdit.getText().toString());
        alarm.setSound(soundCb.isChecked());
        AlarmTime alarmTime = new AlarmTime();
        long alarmId = 0;
        final MediaPlayer sound3 = MediaPlayer.create(this, R.raw.alarmset);

        alarm.setFromDate(AlarmDataBase.getDateStr(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth()));
        alarmId = alarm.persist(db);

        alarmTime.setAt(AlarmDataBase.getTimeStr(timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
        alarmTime.setAlarmId(alarmId);
        alarmTime.persist(db);

        Intent service = new Intent(this, AlarmService.class);
        service.putExtra(Messenger.COL_ALARMID, String.valueOf(alarmId));
        service.setAction(AlarmService.POPULATE);
        startService(service);
        if (setSettings.isSound()) {
            sound3.start();
        }
        finish();

    }

}


