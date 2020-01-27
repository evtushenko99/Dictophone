package com.example.permission.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.permission.R;
import com.example.permission.view.fragments.VoiceRecorderListFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, VoiceRecorderListFragment.newInstance())
                .commit();

    }

}
