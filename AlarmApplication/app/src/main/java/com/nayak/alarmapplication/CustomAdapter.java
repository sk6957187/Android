package com.nayak.alarmapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

public class CustomAdapter extends BaseAdapter {

    private Context context;
    private List<Alarm> alarmList;
    private LayoutInflater layoutInflater;

    public CustomAdapter(Context c, List<Alarm> alarmList){
        this.context = c;
        this.alarmList = alarmList;
        layoutInflater = (LayoutInflater.from(context));
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
    public View getView(int position, View convertView, ViewGroup parent){
        convertView = layoutInflater.inflate(R.layout.row_item, null);
        final Alarm selectAlarm = alarmList.get(position);
        final TextView nameTV = convertView.findViewById(R.id.nameTextView);
        final TextView alarmTV = convertView.findViewById(R.id.timeTextView);
        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        nameTV.setText(selectAlarm.getName());
        alarmTV.setText(selectAlarm.toString());

    final Intent serviceIntent = new Intent(context, AlarmReceiver.class);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, selectAlarm.getHours());
        calendar.set(Calendar.MINUTE, selectAlarm.getMinutes());
        calendar.set(Calendar.SECOND, 0);

        if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DATE, 1);
        }

        ToggleButton toggleButton = convertView.findViewById(R.id.toggle);
        toggleButton.setChecked(selectAlarm.getStatus());
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                selectAlarm.setStatus(isChecked);
                DatabaseHelper db = new DatabaseHelper(context);
                db.updateAlarm(selectAlarm);

                MainActivity.alarmList.clear();
                List<Alarm> list = db.getAllAlarms();
                MainActivity.alarmList.addAll(list);
                notifyDataSetChanged();

                if(!isChecked && selectAlarm.toString().equals(MainActivity.activeAlarm)) {
                    serviceIntent.putExtra("extra", "off");
                    int requestCode = selectAlarm.getId();
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    alarmManager.cancel(pendingIntent);
                    context.sendBroadcast(serviceIntent);
                }
            }
        });

        if(selectAlarm.getStatus()){
            serviceIntent.putExtra("extra", "on");
            serviceIntent.putExtra("active", selectAlarm.toString());
            int requestCode = selectAlarm.getId();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }

        return convertView;
    }

}
