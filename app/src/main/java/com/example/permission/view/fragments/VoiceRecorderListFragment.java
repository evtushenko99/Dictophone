package com.example.permission.view.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.permission.R;
import com.example.permission.presenter.VoiceRecorderListPresenter;
import com.example.permission.services.PlayerService;
import com.example.permission.services.RecorderService;
import com.example.permission.view.Adapters.OnFileClickListener;
import com.example.permission.view.Adapters.RecorderAdapter;

import java.util.List;

public class VoiceRecorderListFragment extends Fragment implements IVoiceRecorderView, View.OnClickListener {
    private static final int REQUEST_RECORD_AUDIO = 0;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;


    private static final int MSG_START_PlAYER = 1;
    private static final int MSG_STOP_PLAYER = 2;
    private static final String BUNDLE_RECORD_NAME = "RECORDNAME";


    private SwipeRefreshLayout swipeContainer;//Свайп для обновление ресайклера
    private RecyclerView mRecyclerView;
    private TextView mTimerTextView;
    private TextView mRecordFileNameTextView;// Отображение в отдельной вьюшке выбранного файла для прослушивания
    private VoiceRecorderListPresenter mPresenter;
    private RecorderAdapter mAdapter;
    private AppCompatImageButton mStartPlayingRecord;// кнопка воспроизведения записи

    private RecorderService mRecorderService;


    private OnFileClickListener mOnFileClickListener = new OnFileClickListener() {
        @Override
        public void onItemClick(@NonNull String fileName) {
            mRecordFileNameTextView.setText(fileName);
            mTimerTextView.setText("");
        }
    };

    private ServiceConnection mRecorderConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.LocalBinder binder = (RecorderService.LocalBinder) service;
            mRecorderService = binder.getService();
            startRecorderListeningTimer();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private ServiceConnection mPlayerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Messenger playerServiceMessanger = new Messenger(service);

            Message message = Message.obtain(null, MSG_START_PlAYER);
            message.replyTo = mMessenger;
            Bundle bundle = new Bundle();
            bundle.putString(BUNDLE_RECORD_NAME, mRecordFileNameTextView.getText().toString());
            message.setData(bundle);
            try {
                playerServiceMessanger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private Messenger mMessenger = new Messenger(new VoiceRecorderHandler());

    public static Fragment newInstance() {
        return new VoiceRecorderListFragment();
    }

    {
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recorder_list_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler);
        mStartPlayingRecord = view.findViewById(R.id.play_recording);
        mStartPlayingRecord.setOnClickListener(this);

        view.findViewById(R.id.start_recording).setOnClickListener(this);
        mRecordFileNameTextView = view.findViewById(R.id.record_file_name);
        swipeContainer = view.findViewById(R.id.swipe_refresh_layout);
        mTimerTextView = view.findViewById(R.id.timer);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_recording:
                String fileName = mRecordFileNameTextView.getText().toString();
                if (!fileName.equals("")) {
                    mPresenter.playStart();
                    mTimerTextView.setText("");
                } else {
                    setToast("Выберите запись для прослушивания");
                }
                break;
            case R.id.start_recording:
                mPresenter.startRecording();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPresenter = new VoiceRecorderListPresenter(this);
        mPresenter.startRecyclerInitialization();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.updateRecyclerAfterSwipe();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainer.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Разрешение на чтение из внешнего хранилища получено", Toast.LENGTH_LONG).show();
            if (mPresenter != null) {
                mPresenter.startRecyclerInitialization();
            }
        }
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "Разрешение на запись аудио получено", Toast.LENGTH_LONG).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void initRecyclerView(@NonNull List<String> recordFileNames) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false);
        // GridLayoutManager gridLayoutManager = new GridLayoutManager(requireContext(), 2);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new RecorderAdapter(recordFileNames);
        mAdapter.setClickListener(mOnFileClickListener);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean checkRecordAudioPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
        return false;
    }

    @Override
    public boolean checkExternalStoragePermission() {

        if (!(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            return false;
        }
        return true;

    }

    @Override
    public void updateRecycler(@NonNull List<String> recordFileNames) {
        mAdapter.updateData(recordFileNames);
    }

    @Override
    public void setToast(String error) {
        Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void startRecorderService() {
        Intent intent = new Intent(getActivity(), RecorderService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, mRecorderConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void startPlayerService() {

        Intent intent = new Intent(getActivity(), PlayerService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, mPlayerConnection, Context.BIND_AUTO_CREATE);
        mStartPlayingRecord.setEnabled(false);

    }


    public void unBindRecorderService() {
        if (mRecorderService != null) {
            getActivity().unbindService(mRecorderConnection);
            mRecorderService = null;
        }
    }

    public void unBindPlayerService() {

        getActivity().unbindService(mPlayerConnection);
        mStartPlayingRecord.setEnabled(true);
    }


    @Override
    public void onStop() {
        mPlayerConnection = null;
        mRecorderService = null;
        super.onStop();
    }

    private void startRecorderListeningTimer() {
        mRecorderService.setOnRecorderChangedListener(new IOnRecorderChangedListener() {
            @Override
            public void onTimerChanged(String timer) {
                mTimerTextView.setText(timer);
            }

            @Override
            public void newRecordName(String name) {
                mRecordFileNameTextView.setText(name);
            }

            @Override
            public void unBindService() {
                unBindRecorderService();
            }
        });
    }

    public class VoiceRecorderHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_STOP_PLAYER:
                    unBindPlayerService();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}
