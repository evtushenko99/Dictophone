package com.example.permission.view.fragments;

public interface IOnRecorderChangedListener {
    void onTimerChanged(String timer);

    void newRecordName(String name);

    void unBindService();
}
