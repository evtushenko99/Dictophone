package com.example.permission.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.permission.R;
import com.example.permission.data.DictaphoneMediaPlayer;
import com.example.permission.view.fragments.VoiceRecorderListFragment;

public class PlayerService extends Service {
    private static final String TAG = "PlayerService";
    public static final String CHANNEL_ID = "ID";
    public static final int NOTIFICATION_ID = 2;
    public static final String ACTION_PLAYER_STOP = "PLAYER_SERVICE_ACTION_CLOSE";
    public static final String ACTION_PLAYER_PLAY_PAUSE = "PLAYER_SERVICE_ACTION_PLAY_PAUSE";

    public static final int MSG_START_PlAYER = 1;
    public static final int MSG_STOP_PLAYER = 2;
    public static final String BUNDLE_RECORD_NAME = "RECORDNAME";

    private String mFileName;
    private long mDuration = 0;

    private boolean switcher = true;
    private DictaphoneMediaPlayer mMediaPlayer;
    private RemoteViews notificationLayout;

    private Messenger mMessenger = new Messenger(new PlayerServiceHandler());

    @Override
    public void onCreate() {
        super.onCreate();
        notificationLayout = new RemoteViews(getPackageName(), R.layout.dictophone_recorder_notification);

        createNotificationChannel();
        Log.d(TAG, "onCreate() called");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAYER_PLAY_PAUSE:
                    if (switcher) {
                        pausePlayingRecord();
                        switcher = false;
                    } else {
                        resumePlayingRecord();
                        switcher = true;
                    }
                    updateNotification();
                    break;

                case ACTION_PLAYER_STOP:
                    stopPlayingRecord();
                    stopSelf();
                    break;
            }

        } else {
            startPlayingRecord();
            startForeground(NOTIFICATION_ID, createNotification());
        }

        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateNotification() {
        Notification notification = createNotification();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification createNotification() {
        if (mMediaPlayer == null && mFileName != null) {
            mMediaPlayer = new DictaphoneMediaPlayer(mFileName);
            startPlayingRecord();

            if (mDuration == 0) {
                mDuration = SystemClock.elapsedRealtime() - mMediaPlayer.getDuration();
                notificationLayout.setChronometer(R.id.time_notification_text_view, mDuration, null, false);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        Intent intent = new Intent(this, VoiceRecorderListFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setContentTitle(getString(R.string.recorder_service_content_title))
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_record_black_24dp)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentIntent(pendingIntent);
        Intent intentCloseService = new Intent(this, PlayerService.class);
        intentCloseService.setAction(ACTION_PLAYER_STOP);
        PendingIntent pendingIntentCloseService = PendingIntent.getService(this, 0, intentCloseService, 0);
        notificationLayout.setOnClickPendingIntent(R.id.stop_recorder_button, pendingIntentCloseService);

        Intent intentPauseRecording = new Intent(this, PlayerService.class);
        intentPauseRecording.setAction(ACTION_PLAYER_PLAY_PAUSE);
        PendingIntent pendingIntentPauseRecording = PendingIntent.getService(this, 0, intentPauseRecording, 0);
        notificationLayout.setOnClickPendingIntent(R.id.play_pause_button, pendingIntentPauseRecording);
        if (switcher) {
            notificationLayout.setImageViewResource(R.id.play_pause_button, R.drawable.ic_pause_24dp);
        } else {
            notificationLayout.setImageViewResource(R.id.play_pause_button, R.drawable.ic_play_24dp);
        }
        notificationLayout.setTextViewText(R.id.filename_notification_text_view, mFileName);

        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void startPlayingRecord() {
        if (mMediaPlayer != null) {
            mMediaPlayer.startPlayingRecord();

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pausePlayingRecord() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pausePlayingRecord();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumePlayingRecord() {
        if (mMediaPlayer != null) {
            mMediaPlayer.resumePlayingRecord();
        }
    }

    private void stopPlayingRecord() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stopPlayingRecord();
            Message message = Message.obtain(null, MSG_STOP_PLAYER);
            try {
                mMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public class PlayerServiceHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_START_PlAYER:
                    mFileName = msg.getData().getString(BUNDLE_RECORD_NAME);
                    updateNotification();
                    mMessenger = msg.replyTo;
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }

}
