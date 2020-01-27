package com.example.permission.view.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface IVoiceRecorderView {
    void initRecyclerView(@Nullable List<String> recordFileNames);

    boolean checkRecordAudioPermission();

    boolean checkExternalStoragePermission();

    void updateRecycler(@NonNull List<String> recordFileNames);

    void setToast(String error);

    void startRecorderService();

    void startPlayerService();
}
