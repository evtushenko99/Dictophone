package com.example.permission.presenter;

import com.example.permission.data.FIleProvider;
import com.example.permission.view.fragments.IVoiceRecorderView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class VoiceRecorderListPresenter {

    private FIleProvider mFileProvider;

    private WeakReference<IVoiceRecorderView> mVoiceRecorderViewWeakReference;


    public VoiceRecorderListPresenter(IVoiceRecorderView voiceRecorderView) {
        mVoiceRecorderViewWeakReference = new WeakReference<>(voiceRecorderView);
        mFileProvider = new FIleProvider();
    }


    public void startRecyclerInitialization() {
        IVoiceRecorderView view = mVoiceRecorderViewWeakReference.get();
        List<String> fileNames = new ArrayList<>();
        boolean v = view.checkExternalStoragePermission();
        if (v) {
            boolean newFolderExist = mFileProvider.createFolderForRecorders();
            if (newFolderExist) {
                fileNames = new ArrayList<>(mFileProvider.loadRecords());
                view.initRecyclerView(fileNames);

            }
        } else {
            view.initRecyclerView(fileNames);
        }
    }


    public void startRecording() {
        IVoiceRecorderView view = mVoiceRecorderViewWeakReference.get();
        boolean isPermissionActivated = view.checkRecordAudioPermission();
        if (isPermissionActivated) {
            view.startRecorderService();
            view.setToast("Record start");
        }

    }

    public void updateRecyclerAfterSwipe() {
        IVoiceRecorderView view = mVoiceRecorderViewWeakReference.get();
        List<String> fileNames = new ArrayList<>(mFileProvider.loadRecords());
        view.updateRecycler(fileNames);
    }


    public void playStart() {
        final IVoiceRecorderView view = mVoiceRecorderViewWeakReference.get();
        view.startPlayerService();
        view.setToast("Playing Audio started");
    }

}

