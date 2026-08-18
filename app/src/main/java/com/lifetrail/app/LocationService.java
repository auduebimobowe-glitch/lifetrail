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

public class LocationService extends Service {

    private static final String CHANNEL = "lifetrail_tracking";
    private static final String PREFS = "lifetrail_data";
    private static final String KEY_DISTANCE = "distance_meters";
    private static final String KEY_MOVING = "moving_seconds";
    private static final String KEY_LAST_LAT = "last_lat";
    private static final String KEY_LAST_LON = "last_lon";
    private static final String KEY_LAST_TIME = "last_time";

    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();

        createChannel();

        client = LocationServices.getFusedLocationProviderClient(this);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                for (Location location : result.getLocations()) {
                    recordLocation(location);
                }
            }
        };
    }

    private void recordLocation(Location location) {

        if (!location.hasAccuracy() || location.getAccuracy() > 75) {
            return;
        }

        double lastLat = prefs.getFloat(KEY_LAST_LAT, Float.NaN);
        double lastLon = prefs.getFloat(KEY_LAST_LON, Float.NaN);
        long lastTime = prefs.getLong(KEY_LAST_TIME, 0);

        if (!Double.isNaN(lastLat) && !Double.isNaN(lastLon)) {

            float[] distance = new float[1];

            Location.distanceBetween(
                    lastLat,
                    lastLon,
                    location.getLatitude(),
                    location.getLongitude(),
                    distance
            );

            float meters = distance[0];

            // Ignore tiny GPS jitter and obviously large jumps.
            if (meters >= 5 && meters <= 200) {

                float oldDistance =
                        prefs.getFloat(KEY_DISTANCE, 0f);

                long movingSeconds =
                        prefs.getLong(KEY_MOVING, 0);

                long currentTime = location.getTime();

                if (lastTime > 0 && currentTime >= lastTime) {
                    long seconds =
                            (currentTime - lastTime) / 1000;

                    if (seconds <= 120) {
                        movingSeconds += seconds;
                    }
                }

                prefs.edit()
                        .putFloat(KEY_DISTANCE, oldDistance + meters)
                        .putLong(KEY_MOVING, movingSeconds)
                        .apply();
            }
        }

        prefs.edit()
                .putFloat(KEY_LAST_LAT, (float) location.getLatitude())
                .putFloat(KEY_LAST_LON, (float) location.getLongitude())
                .putLong(KEY_LAST_TIME, location.getTime())
                .apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL)
                        .setContentTitle("LifeTrail is tracking")
                        .setContentText("Recording your movement privately on this device.")
                        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                        .setOngoing(true)
                        .build();

        startForeground(7, notification);

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        10000
                )
                        .setMinUpdateIntervalMillis(5000)
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
            return START_NOT_STICKY;
        }

        client.requestLocationUpdates(
                request,
                locationCallback,
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

        if (client != null && locationCallback != null) {
            client.removeLocationUpdates(locationCallback);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
