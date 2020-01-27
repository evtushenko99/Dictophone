package com.example.permission.data;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class DictaphoneMediaPlayer {
    private MediaPlayer mMediaPlayer;
    public static int pausedSaver;
    private String mFolderPath;
    private String mFilePath;

    public int getDuration() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public DictaphoneMediaPlayer(String fileName) {
        mMediaPlayer = new MediaPlayer();
        mFolderPath = FIleProvider.mFolderPath;
        mFilePath = mFolderPath + fileName;
        try {
            mMediaPlayer.setDataSource(mFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

    }

    public void startPlayingRecord() {

        try {
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }

    public void pausePlayingRecord() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            pausedSaver = mMediaPlayer.getCurrentPosition();
        }
    }

    public void resumePlayingRecord() {
        if ((mMediaPlayer != null) && (pausedSaver != 0)) {
            mMediaPlayer.seekTo(pausedSaver);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        }
    }

    public void stopPlayingRecord() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            releasePlayer();
        }

    }

    private void releasePlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
