package com.nayak.alarmapplication;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomAdapter extends BaseAdapter {

    private Context context;
    private List<Alarm> alarmList;
    private LayoutInflater layoutInflater;

    public CustomAdapter(Context c, List<Alarm> alarmList){
        this.context = c;
        this.alarmList = alarmList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return alarmList.size();
    }

    @Override
    public Object getItem(int position){
        return alarmList.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = layoutInflater.inflate(R.layout.row_item, null);

        Alarm selectAlarm = alarmList.get(position);

        TextView nameTV = convertView.findViewById(R.id.nameTextView);
        TextView alarmTV = convertView.findViewById(R.id.timeTextView);
        ToggleButton toggleButton = convertView.findViewById(R.id.toggle);
        // Click item to edit alarm
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddActivity.class);
                intent.putExtra("alarm_id", selectAlarm.getId());
                intent.putExtra("alarm_hour", selectAlarm.getHours());
                intent.putExtra("alarm_minute", selectAlarm.getMinutes());
                intent.putExtra("alarm_name", selectAlarm.getName());
                // start activity for result so MainActivity can refresh
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, MainActivity.REQUEST_CODE);
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });

        nameTV.setText(selectAlarm.getName());
        alarmTV.setText(selectAlarm.toString());

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Prevent listener firing during setChecked()
        toggleButton.setOnCheckedChangeListener(null);
        toggleButton.setChecked(selectAlarm.getStatus());

        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {

            selectAlarm.setStatus(isChecked);

            DatabaseHelper db = new DatabaseHelper(context);
            db.updateAlarm(selectAlarm);

            // Just update UI, no full DB reload
            notifyDataSetChanged();

            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("active", selectAlarm.toString());
            intent.putExtra("extra", isChecked ? "on" : "off");

            int requestCode = selectAlarm.getId();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (!isChecked) {
                alarmManager.cancel(pendingIntent);
                context.sendBroadcast(intent);
                // debug log cancellation
                try {
                    File out = new File(context.getExternalFilesDir(null), "schedule_log.txt");
                    FileWriter fw = new FileWriter(out, true);
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                    fw.write(time + " - Cancelled alarm id=" + requestCode + "\n");
                    fw.close();
                } catch (IOException ioe) {
                    // ignore
                }
                return;
            }

            if (!alarmManager.canScheduleExactAlarms()) {
                Intent permissionIntent =
                        new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                permissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(permissionIntent);
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, selectAlarm.getHours());
            calendar.set(Calendar.MINUTE, selectAlarm.getMinutes());
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
                // debug log scheduling
                try {
                    File out = new File(context.getExternalFilesDir(null), "schedule_log.txt");
                    FileWriter fw = new FileWriter(out, true);
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                    fw.write(time + " - Scheduled alarm id=" + requestCode + " for " + calendar.getTimeInMillis() + "\n");
                    fw.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        });

        return convertView;
    }


}
