package com.nayak.alarmapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Music extends Service {

    private MediaPlayer mediaPlayer;
    private static final String CHANNEL_ID = "alarm_channel";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create notification channel and run as foreground service for reliable playback
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Alarm")
                .setContentText("Alarm is playing")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .build();

        startForeground(1, notification);

        // Log service start
        try {
            File out = new File(getExternalFilesDir(null), "music_started.txt");
            FileWriter fw = new FileWriter(out, true);
            String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
            fw.write(time + " - Music.onStartCommand invoked\n");
            fw.close();
        } catch (IOException ioe) {
            // ignore
        }

        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
                if (mediaPlayer == null) {
                    throw new Exception("MediaPlayer.create returned null");
                }
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                // Log successful start
                try {
                    File out = new File(getExternalFilesDir(null), "music_started.txt");
                    FileWriter fw = new FileWriter(out, true);
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                    fw.write(time + " - MediaPlayer started successfully\n");
                    fw.close();
                } catch (IOException ioe) {
                    // ignore
                }
            } catch (Throwable t) {
                try {
                    File out = new File(getExternalFilesDir(null), "music_error.txt");
                    FileWriter fw = new FileWriter(out, true);
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                    fw.write(time + " - MediaPlayer failed: " + t.toString() + "\n");
                    fw.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarm Channel", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Channel for alarm playback");
                manager.createNotificationChannel(channel);
            }
        }
    }
}
