package com.lifetrail.app;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service {

    private static final String CHANNEL = "lifetrail_tracking";
    private static final String PREFS = "lifetrail_data";

    private FusedLocationProviderClient client;
    private LocationCallback callback;
    private SharedPreferences prefs;

    private Location lastLocation;
    private long lastTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        createChannel();

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        client = LocationServices.getFusedLocationProviderClient(this);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {

                for (Location location : result.getLocations()) {
                    processLocation(location);
                }
            }
        };
    }

    private void processLocation(Location location) {

    long now = System.currentTimeMillis();

    float speed = location.hasSpeed()
            ? location.getSpeed()
            : 0f;

    double distanceMeters = 0;

    if (lastLocation != null) {

        distanceMeters = lastLocation.distanceTo(location);

        // Ignore very small GPS jumps/noise.
        if (distanceMeters < 3) {
            distanceMeters = 0;
        }

        double totalDistance =
                prefs.getFloat("distance_meters", 0f)
                + distanceMeters;

        long movingSeconds =
                prefs.getLong("moving_seconds", 0);

        if (lastTime > 0) {

            long seconds =
                    (now - lastTime) / 1000;

            // Consider the user moving
            // faster than approximately 0.8 m/s.
            if (speed >= 0.8f
                    && seconds > 0
                    && seconds < 120) {

                movingSeconds += seconds;
            }
        }

        prefs.edit()
                .putFloat(
                        "distance_meters",
                        (float) totalDistance
                )
                .putLong(
                        "moving_seconds",
                        movingSeconds
                )
                .apply();
    }

    prefs.edit()
            .putFloat(
                    "last_lat",
                    (float) location.getLatitude()
            )
            .putFloat(
                    "last_lon",
                    (float) location.getLongitude()
            )
            .putFloat(
                    "last_speed",
                    speed
            )
            .putLong(
                    "last_location_time",
                    now
            )
            .apply();

    lastLocation = location;
    lastTime = now;
}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL)
                        .setContentTitle("LifeTrail is tracking")
                        .setContentText("Recording your movement privately")
                        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                        .setOngoing(true)
                        .build();

        startForeground(7, notification);

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        5000
                )
                        .setMinUpdateIntervalMillis(3000)
                        .setMinUpdateDistanceMeters(5)
                        .build();

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {

            return START_STICKY;
        }

        client.requestLocationUpdates(
                request,
                callback,
                getMainLooper()
        );

        return START_STICKY;
    }

    private void createChannel() {

        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL,
                        "LifeTrail tracking",
                        NotificationManager.IMPORTANCE_LOW
                );

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {

        if (client != null && callback != null) {
            client.removeLocationUpdates(callback);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
