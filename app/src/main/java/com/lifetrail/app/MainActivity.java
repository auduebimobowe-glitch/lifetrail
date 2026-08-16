package com.lifetrail.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int REQ = 10;
    private TextView status;
    @Override public void onCreate(Bundle b) { super.onCreate(b);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(36,48,36,36);
        TextView title = new TextView(this); title.setText("LifeTrail"); title.setTextSize(34); title.setTypeface(null,1); root.addView(title);
        TextView sub = new TextView(this); sub.setText("Your private automatic timeline.\n\nCarry your phone. LifeTrail remembers your day."); sub.setTextSize(18); root.addView(sub);
        status = new TextView(this); status.setText("\nTracking is off."); status.setTextSize(17); root.addView(status);
        Button start = new Button(this); start.setText("START MY DAY"); root.addView(start);
        Button stop = new Button(this); stop.setText("PAUSE TRACKING"); root.addView(stop);
        start.setOnClickListener(v -> startTracking()); stop.setOnClickListener(v -> stopTracking()); setContentView(root);
    }
    private void startTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ); return;
        }
        if (android.os.Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ); return;
        }
        ContextCompat.startForegroundService(this, new Intent(this, LocationService.class)); status.setText("\nTracking is ON.\nLifeTrail is recording your movement locally.");
    }
    private void stopTracking() { stopService(new Intent(this, LocationService.class)); status.setText("\nTracking is paused."); }
}
