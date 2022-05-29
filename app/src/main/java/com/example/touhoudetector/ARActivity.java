package com.example.touhoudetector;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.unity3d.player.UnityPlayerActivity;

public class ARActivity extends UnityPlayerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aractivity);
        LinearLayout ll_unity_container = findViewById(R.id.ll_unity_container);
        View unity_view = mUnityPlayer.getView();
        ll_unity_container.addView(unity_view);
    }
}