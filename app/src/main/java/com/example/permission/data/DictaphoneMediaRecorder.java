package com.example.permission.data;

import android.media.MediaRecorder;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;

public class DictaphoneMediaRecorder {
    private MediaRecorder mediaRecorder;
    private FIleProvider mFileProvider;

    private String mFolderPath;
    private String mFilePath;
    private String mFileName;

    public int getDuration() {
        if (mediaRecorder != null) {
            return 5;
        }
        return 0;
    }


    public String getFileName() {
        return mFileName;
    }


    public DictaphoneMediaRecorder() {
        mFileProvider = new FIleProvider();
        mFileProvider.loadRecords();
        mFolderPath = FIleProvider.mFolderPath;
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);


    }

    public String recordStart() {
        try {
            mFileName = mFileProvider.getNewRecordName();
            mFilePath = mFolderPath + mFileName;
            File outFile = new File(mFilePath);
            if (outFile.exists()) {
                outFile.delete();
            }
            mediaRecorder.setOutputFile(mFilePath);
            try {
                mediaRecorder.prepare();
            } catch (IOException e) {
                return ("mediaRecorder.prepare() failed");
            }

            mediaRecorder.start();
            return ("Recording started");
        } catch (Exception e) {
            return (e.getMessage());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String recordPause() {
        if (mediaRecorder != null) {
            mediaRecorder.pause();
            return "mediaRecorder.pause()";
        }
        return "Cant pause mediaRecorder";
    }

    public String recordStop() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            releaseRecorder();
            return "mediaRecorder.stop()";
        }
        return "Cant stop mediaRecorder";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String recordResume() {
        if (mediaRecorder != null) {
            mediaRecorder.resume();

            return "mediaRecorder.resume()";
        }
        return "Cant resume mediaRecorder";
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }


}
