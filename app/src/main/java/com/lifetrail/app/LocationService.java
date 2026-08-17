package com.lifetrail.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {

    private static final String CHANNEL = "lifetrail_tracking";

    private FusedLocationProviderClient client;
    private LocationListener locationListener;

    @Override
    public void onCreate() {
        super.onCreate();

        createChannel();

        client = LocationServices.getFusedLocationProviderClient(this);

        locationListener = location -> {
            // Location received.
            // Later we will save this point locally.
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL)
                        .setContentTitle("LifeTrail is tracking")
                        .setContentText(
                                "Your timeline is being recorded privately on this device."
                        )
                        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                        .setOngoing(true)
                        .build();

        startForeground(7, notification);

        LocationRequest request =
                new LocationRequest.Builder(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        30000
                )
                        .setMinUpdateDistanceMeters(50)
                        .build();

        try {
            client.requestLocationUpdates(
                    request,
                    locationListener,
                    getMainLooper()
            );
        } catch (SecurityException ignored) {
            // Permission was not available.
        }

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

        if (client != null && locationListener != null) {
            client.removeLocationUpdates(locationListener);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
