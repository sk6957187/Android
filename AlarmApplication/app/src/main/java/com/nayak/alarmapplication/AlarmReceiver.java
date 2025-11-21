package com.nayak.alarmapplication;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        boolean isRunning = false;
        String string = null;
        try {
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey("extra")) {
                string = intent.getExtras().getString("extra");
            }
        } catch (Exception e) {
            // ignore
        }

        // Debug: write a simple log file to external files so we can confirm receiver was invoked
        try {
            File out = new File(context.getExternalFilesDir(null), "alarm_received.txt");
            FileWriter fw = new FileWriter(out, true);
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            fw.write(time + " - AlarmReceiver invoked - extra=" + String.valueOf(string) + "\n");
            fw.close();
        } catch (IOException ioe) {
            // ignore
        }

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)){
            if (Music.class.getName().equals(serviceInfo.service.getClassName())){
                isRunning = true;
            }
        }

        Intent mIntent = new Intent(context, Music.class);
        if(string.equals("on") && !isRunning){
            // Use foreground service on Android O+ to ensure service starts reliably
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(mIntent);
            } else {
                context.startService(mIntent);
            }
            MainActivity.activeAlarm = intent.getExtras().getString("active");
        } else if(string.equals("off")){
            context.stopService(mIntent);
            MainActivity.activeAlarm = "";
        }

     }
}
