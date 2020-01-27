package com.example.permission.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.permission.R;
import com.example.permission.data.DictaphoneMediaRecorder;
import com.example.permission.view.fragments.IOnRecorderChangedListener;
import com.example.permission.view.fragments.VoiceRecorderListFragment;

public class RecorderService extends Service {
    private static final String TAG = "RecorderService";
    public static final String CHANNEL_ID = "ID";
    public static final int NOTIFICATION_ID = 1;
    public static final String ACTION_STOP = "RECORDER_SERVICE_ACTION_CLOSE";
    public static final String ACTION_PLAY_PAUSE = "RECORDER_SERVICE_ACTION_PLAY_PAUSE";

    private DictaphoneMediaRecorder mMediaRecorder;
    private RemoteViews notificationLayout;

    private IOnRecorderChangedListener mOnRecorderChangedListener;

    private final IBinder mBinder = new LocalBinder();

    private Chronometer mChronometer;

    private boolean switcher = true;
    private long timeWhenStopped = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationLayout = new RemoteViews(getPackageName(), R.layout.dictophone_recorder_notification);
        mMediaRecorder = new DictaphoneMediaRecorder();
        mChronometer = new Chronometer(this);
        createNotificationChannel();
        Log.d(TAG, "onCreate() called");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /**
         * Tick listener не работает.
         */
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (mOnRecorderChangedListener != null) {

                    mOnRecorderChangedListener.onTimerChanged(chronometer.getText().toString());
                    mOnRecorderChangedListener.newRecordName(mMediaRecorder.getFileName());
                }
                startForeground(NOTIFICATION_ID, createNotification());
            }
        });
        Log.d(TAG, "onStartCommand() ");
        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_PLAY_PAUSE:
                        if (switcher) {
                            pauseRecording();
                            stopChronometer();
                            switcher = false;
                        } else {
                            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                            mChronometer.start();
                            resumeRecording();
                            switcher = true;
                        }
                        updateNotification();
                        break;

                    case ACTION_STOP:
                        stopChronometer();
                        stopRecording();
                        stopSelf();
                        break;
                }

            } else {
                mChronometer.start();
                startRecording();
                startForeground(NOTIFICATION_ID, createNotification());
            }

        }
        return START_NOT_STICKY;
    }

    private void stopChronometer() {
        timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
        mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
        mChronometer.stop();
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);

        Intent intent = new Intent(this, VoiceRecorderListFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        if (switcher) {
            notificationLayout.setChronometer(R.id.time_notification_text_view, SystemClock.elapsedRealtime() + timeWhenStopped, null, true);

        } else {
            notificationLayout.setChronometer(R.id.time_notification_text_view, SystemClock.elapsedRealtime() + timeWhenStopped, null, false);

        }


        if (mOnRecorderChangedListener != null) {
            mOnRecorderChangedListener.onTimerChanged(mChronometer.getText().toString());
            mOnRecorderChangedListener.newRecordName(mMediaRecorder.getFileName());
        }

        builder.setContentTitle(getString(R.string.recorder_service_content_title))
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_record_black_24dp)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentIntent(pendingIntent);
        Intent intentCloseService = new Intent(this, RecorderService.class);
        intentCloseService.setAction(ACTION_STOP);
        PendingIntent pendingIntentCloseService = PendingIntent.getService(this, 0, intentCloseService, 0);
        notificationLayout.setOnClickPendingIntent(R.id.stop_recorder_button, pendingIntentCloseService);


        Intent intentPauseRecording = new Intent(this, RecorderService.class);
        intentPauseRecording.setAction(ACTION_PLAY_PAUSE);
        PendingIntent pendingIntentPauseRecording = PendingIntent.getService(this, 0, intentPauseRecording, 0);
        notificationLayout.setOnClickPendingIntent(R.id.play_pause_button, pendingIntentPauseRecording);
        if (switcher) {
            notificationLayout.setImageViewResource(R.id.play_pause_button, R.drawable.ic_pause_24dp);
        } else {
            notificationLayout.setImageViewResource(R.id.play_pause_button, R.drawable.ic_play_24dp);
        }
        notificationLayout.setTextViewText(R.id.filename_notification_text_view, mMediaRecorder.getFileName());


        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void startRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.recordStart();

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pauseRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.recordPause();
            mOnRecorderChangedListener.onTimerChanged(String.valueOf(mChronometer.getBase()));

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.recordResume();
        }
    }

    private void stopRecording() {
        if (mMediaRecorder != null) {
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            mOnRecorderChangedListener.onTimerChanged(mChronometer.getText().toString());
            mMediaRecorder.recordStop();
            mOnRecorderChangedListener.unBindService();
        }
    }

    public void setOnRecorderChangedListener(IOnRecorderChangedListener onRecorderChangedListener) {
        mOnRecorderChangedListener = onRecorderChangedListener;
    }

    public class LocalBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }
    }

}
