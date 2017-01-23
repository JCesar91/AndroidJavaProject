package com.example.juliocesar.alarmclock;

import android.media.MediaPlayer;
import android.os.Bundle;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.example.juliocesar.alarmclock.SQLiteTools.Alarm;
import com.example.juliocesar.alarmclock.SQLiteTools.Messenger;

public class MainActivity extends ListActivity {


    private TextView rangeText;

    private SQLiteDatabase db;
    private AlertDialog disclaimer;

    public final Calendar cal = Calendar.getInstance();
    public final Date dt = new Date();
    private String[] monthArr;

    private Alarm alarm = new Alarm();
    private Messenger messenger = new Messenger();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        rangeText = (TextView) findViewById(R.id.currentDate);

        db = setSettings.db;

        monthArr = getResources().getStringArray(R.array.month);

        int r = setSettings.getDateRange();
        switch(r) {
            case 3: // Yearly
                cal.set(Calendar.MONTH, 0);

            case 2: // Monthly
                cal.set(Calendar.DATE, 1);

            case 1: // Weekly
                if (r==1) cal.set(Calendar.DATE, cal.getFirstDayOfWeek());

            case 0: // Daily
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
        }

        registerForContextMenu(getListView());

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("cal", cal.getTimeInMillis());
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        cal.setTimeInMillis(state.getLong("cal"));
    }

    private String getRangeStr() {
        int date = cal.get(Calendar.DATE);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        dt.setTime(System.currentTimeMillis());

        switch(setSettings.getDateRange()) {
            case 0: // Daily
                if (date==dt.getDate() && month==dt.getMonth() && year==dt.getYear()+1900) return "Today";
                else return date+" "+monthArr[month+1];

            case 1: // Weekly
                return date+" "+monthArr[month+1] + move(+1) + " - " + cal.get(Calendar.DATE)+" "+monthArr[cal.get(Calendar.MONTH)+1] + move(-1);

            case 2: // Monthly
                return monthArr[month+1]+" "+year;

            case 3: // Yearly
                return year+"";
        }
        return null;
    }

    private Cursor createCursor() {
        Cursor c = setSettings.dbHelper.listNotifications(db, cal.getTimeInMillis()+move(+1), cal.getTimeInMillis()+move(-1));
        //startManagingCursor(c); THIS IS SO BAD
        return c;

    }

    @Override
    protected void onResume() {
        super.onResume();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.listview,
                createCursor(),
                new String[]{Alarm.COL_NAME, Messenger.COL_DATETIME, Messenger.COL_DATETIME, Messenger.COL_DATETIME, Messenger.COL_DATETIME},
                new int[]{R.id.displayName, R.id.displayTime, R.id.displayTime});
        adapter.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.displayName) return false;

                TextView tv = (TextView) view;
                long time = cursor.getLong(columnIndex);

                dt.setTime(time);
                switch (view.getId()) {
                    case R.id.displayTime:
                        long now = System.currentTimeMillis();
                        String txt = getTime.getActualTime(dt.getHours(), dt.getMinutes());
                        if (TextUtils.isEmpty(txt))
                            txt = getTime.getActualTime(dt.getHours(), dt.getMinutes());
                        tv.setText(txt);
                        break;
                }
                return true;
            }
        });
        setListAdapter(adapter);

        rangeText.setText(getRangeStr());
    }

    private String move(int step) {
        switch(setSettings.getDateRange()) {
            case 0:
                cal.add(Calendar.DATE, 1*step);
                break;
            case 1:
                cal.add(Calendar.DATE, 7*step);
                break;
            case 2:
                cal.add(Calendar.MONTH, 1*step);
                break;
            case 3:
                cal.add(Calendar.YEAR, 1*step);
                break;
        }
        return "";
    }

    public void onClick(View v) {

        final MediaPlayer sound = MediaPlayer.create(this, R.raw.saveconfirm);
        final MediaPlayer sound2 = MediaPlayer.create(this, R.raw.select);
        switch (v.getId()) {
            case R.id.changeSettings:
                startActivity(new Intent(this, Settings.class));
                if (setSettings.isSound()) {
                    sound.start();
                }
                break;
            case R.id.newAlarm:
                startActivity(new Intent(this, NewAlarm.class));
                if (setSettings.isSound()) {
                    sound.start();
                }
                break;
            case R.id.previousButton:
                move(-1);
                rangeText.setText(getRangeStr());
                ((SimpleCursorAdapter)getListAdapter()).changeCursor(createCursor());
                 if (setSettings.isSound()) {
                    sound2.start();
                 }
                break;
            case R.id.nextButton:
                move(+1);
                rangeText.setText(getRangeStr());
                ((SimpleCursorAdapter)getListAdapter()).changeCursor(createCursor());
                if (setSettings.isSound()) {
                    sound2.start();
                }
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (v.getId() == android.R.id.list) {
            getMenuInflater().inflate(R.menu.context_menu, menu);
            menu.setHeaderTitle("Choose an Option");
            menu.setHeaderIcon(R.drawable.notification);

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            messenger.setId(info.id);
            messenger.load(db);
            if (messenger.getDateTime() < System.currentTimeMillis())
                menu.removeItem(R.id.menu_edit);

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        boolean refresh = false;

        switch (item.getItemId()) {
            case R.id.menu_edit:
                messenger.setId(info.id);
                messenger.load(db);
                alarm.reset();
                alarm.setId(messenger.getAlarmId());
                alarm.load(db);

                showDialog(R.id.menu_edit);
                break;

            case R.id.menu_delete:
                setSettings.dbHelper.cancelNotification(db, info.id, false);
                refresh = true;

                Intent cancelThis = new Intent(this, AlarmService.class);
                cancelThis.putExtra(Messenger.COL_ID, String.valueOf(info.id));
                cancelThis.setAction(AlarmService.CANCEL);
                startService(cancelThis);
                break;

        }

        if (refresh) {
            SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
            adapter.getCursor().requery();
            adapter.notifyDataSetChanged();
        }

        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        openContextMenu(v);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case R.id.menu_edit:
                return new AlertDialog.Builder(this)
                        .setTitle("Edit")
                        .setView(getLayoutInflater().inflate(R.layout.editalarm, null))
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Dialog d = (Dialog) dialog;
                                EditText msgEdit = (EditText) d.findViewById(R.id.alarmName);
                                CheckBox soundCb = (CheckBox) d.findViewById(R.id.ringOn);

                                alarm.setSound(soundCb.isChecked());
                                if (!TextUtils.isEmpty(msgEdit.getText())) {
                                    alarm.setName(msgEdit.getText().toString());
                                    alarm.persist(db);

                                    SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
                                    adapter.getCursor().requery();
                                    adapter.notifyDataSetChanged();

                                } else {
                                    Toast.makeText(MainActivity.this, "Enter a message", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case R.id.menu_edit:
                EditText msgEdit = (EditText) dialog.findViewById(R.id.alarmName);
                CheckBox soundCb = (CheckBox) dialog.findViewById(R.id.ringOn);

                msgEdit.setText(alarm.getName());
                soundCb.setChecked(alarm.getSound());
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getListAdapter().isEmpty()) {
            menu.findItem(R.id.menu_delete_all).setEnabled(false);
        } else {
            menu.findItem(R.id.menu_delete_all).setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_all:
                String startTime = cal.getTimeInMillis()+move(+1);
                String endTime = cal.getTimeInMillis()+move(-1);
                setSettings.dbHelper.cancelNotification(db, startTime, endTime);

                Intent cancelAll = new Intent(this, AlarmService.class);
                cancelAll.putExtra(Alarm.COL_FROMDATE, startTime);
                cancelAll.putExtra(Alarm.COL_TODATE, endTime);
                cancelAll.setAction(AlarmService.CANCEL);
                startService(cancelAll);

                SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
                adapter.getCursor().requery();
                adapter.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (disclaimer != null)
            disclaimer.dismiss();
        super.onDestroy();

    }
}