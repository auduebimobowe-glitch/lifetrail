package com.lifetrail.app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQ = 10;

    private TextView status;
    private TextView distanceView;
    private TextView movingView;

    private SharedPreferences prefs;
    private Handler handler = new Handler();
    private Runnable updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        prefs = getSharedPreferences("lifetrail_data", MODE_PRIVATE);

        status = findViewById(R.id.trackingStatus);
        distanceView = findViewById(R.id.distanceValue);
        movingView = findViewById(R.id.movingValue);

        Button start = findViewById(R.id.startTracking);
        Button stop = findViewById(R.id.stopTracking);

        start.setOnClickListener(v -> startTracking());
        stop.setOnClickListener(v -> stopTracking());

        updateDashboard();

        updater = new Runnable() {
            @Override
            public void run() {
                updateDashboard();
                handler.postDelayed(this, 2000);
            }
        };

        handler.post(updater);
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

        updateDashboard();
    }

    private void stopTracking() {

        stopService(
                new Intent(this, LocationService.class)
        );

        status.setText("○ PAUSED\nTracking is currently paused.");

        updateDashboard();
    }

    private void updateDashboard() {

        float meters = prefs.getFloat("distance_meters", 0f);
        long seconds = prefs.getLong("moving_seconds", 0);

        float kilometers = meters / 1000f;

        if (distanceView != null) {
            distanceView.setText(
                    String.format(java.util.Locale.US, "%.2f km", kilometers)
            );
        }

        if (movingView != null) {
            long minutes = seconds / 60;
            movingView.setText(minutes + " min");
        }
    }

    @Override
    protected void onDestroy() {

        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }

        super.onDestroy();
    }
}
