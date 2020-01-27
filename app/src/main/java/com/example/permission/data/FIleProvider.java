package com.example.permission.data;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FIleProvider {
    private static final String FOLDER_NAME = "/Recorders/";
    public static final String mFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + FOLDER_NAME;
    private int i = 0;

    public List<String> loadRecords() {
        List<String> records = new ArrayList<>();
        String[] list;
        final File externalStorageDirectory = new File(mFolderPath);
        list = externalStorageDirectory.list();
        if (list != null) {
            records = new ArrayList<>(Arrays.asList(list));
            i = list.length + 1;
        }
        return records;
    }

    public String getNewRecordName() {
        Date date = new Date();
        @SuppressLint("SimpleDateFormat")
        DateFormat format = new SimpleDateFormat("HH:mm:ss");
        String fileRecordName = "record_" + i + ".mp3";
        return fileRecordName;
    }

    public boolean createFolderForRecorders() {
        String externalStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(externalStorageState) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState)) {
            File dir = new File(mFolderPath);
            if (!dir.exists()) {
                return dir.mkdir();
            }
            return true;
        }
        return false;
    }
}
