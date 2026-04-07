package com.example.music_app_intent_service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    //Khai bao doi tuong ma service quan ly
    MediaPlayer mymedia;
    public static boolean isPlaying = false;

    private static final String CHANNEL_ID = "MusicServiceChannel";
    public static final String ACTION_STOP = "com.example.music_app_intent_service.ACTION_STOP";
    private static final int NOTIF_ID = 1;

    @Override
    public IBinder onBind (Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    //Goi ham oncreate de tao doi tuong ma service quan ly
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mymedia =
                MediaPlayer.create(MyService.this,R.raw.song);
        mymedia.setLooping(true); // cho phep lap lai lien tuc
    }
    //Goi ham onstartcommand de khoi chay doi tuong ma service quan ly
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            // stop requested from notification action
            if (mymedia != null && mymedia.isPlaying()) {
                mymedia.stop();
                isPlaying = false;
            }
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        // Start (or toggle) playback
        if (mymedia.isPlaying()) {
            mymedia.pause();
            isPlaying = false;
        }
        else {
            // set music stream volume to max
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
            }

            mymedia.start();
            isPlaying = true;
        }

        // Start foreground notification so music keeps playing when Activity is gone
        Notification notification = buildNotification();
        startForeground(NOTIF_ID, notification);

        return START_STICKY;
    }
    // goi ham onDestroy de huy doi tuong ma service quan ly
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mymedia != null) {
            if (mymedia.isPlaying()) mymedia.stop();
            mymedia.release();
            mymedia = null;
        }
        isPlaying = false;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Music Playback", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Channel for music playback controls");
            NotificationManager manager = (NotificationManager) getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Stop action intent
        Intent stopIntent = new Intent(this, MyService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPending;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stopPending = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            stopPending = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Music Player")
                .setContentText(isPlaying ? "Đang phát" : "Đã dừng")
                .setSmallIcon(R.drawable.nhac)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(isPlaying);

        // add a Stop action so user can stop from notification (optional)
        builder.addAction(R.drawable.stop, "Stop", stopPending);

        return builder.build();
    }

}