package com.lifetrail.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQ = 10;

    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the LifeTrail timeline dashboard.
        setContentView(R.layout.layout_main);

        status = findViewById(R.id.trackingStatus);

        Button start = findViewById(R.id.startTracking);
        Button stop = findViewById(R.id.stopTracking);

        start.setOnClickListener(v -> startTracking());
        stop.setOnClickListener(v -> stopTracking());
    }

    private void startTracking() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQ
            );

            return;
        }

        if (android.os.Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    REQ
            );

            return;
        }

        ContextCompat.startForegroundService(
                this,
                new Intent(this, LocationService.class)
        );

        status.setText("● RECORDING\nLifeTrail is recording your movement locally.");
    }

    private void stopTracking() {

        stopService(
                new Intent(this, LocationService.class)
        );

        status.setText("○ PAUSED\nTracking is currently paused.");
    }
}
