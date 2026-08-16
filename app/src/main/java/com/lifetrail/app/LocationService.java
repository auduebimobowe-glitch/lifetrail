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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {
    private static final String CHANNEL="lifetrail_tracking"; private FusedLocationProviderClient client;
    @Override public void onCreate(){ super.onCreate(); createChannel(); client=LocationServices.getFusedLocationProviderClient(this); }
    @Override public int onStartCommand(Intent intent,int flags,int id){
        Notification n=new NotificationCompat.Builder(this,CHANNEL).setContentTitle("LifeTrail is tracking").setContentText("Your timeline is being recorded privately on this device.").setSmallIcon(android.R.drawable.ic_menu_mylocation).setOngoing(true).build();
        startForeground(7,n);
        LocationRequest req=new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY,30000).setMinUpdateDistanceMeters(50).build();
        try { client.requestLocationUpdates(req, new android.location.LocationListener(){ public void onLocationChanged(android.location.Location l){} public void onProviderEnabled(String p){} public void onProviderDisabled(String p){} }, getMainLooper()); } catch(SecurityException ignored){}
        return START_STICKY;
    }
    private void createChannel(){ NotificationChannel c=new NotificationChannel(CHANNEL,"LifeTrail tracking",NotificationManager.IMPORTANCE_LOW); getSystemService(NotificationManager.class).createNotificationChannel(c); }
    @Override public void onDestroy(){ if(client!=null) client.removeLocationUpdates(new android.location.LocationListener(){ public void onLocationChanged(android.location.Location l){} public void onProviderEnabled(String p){} public void onProviderDisabled(String p){} }); super.onDestroy(); }
    @Nullable @Override public IBinder onBind(Intent i){ return null; }
}
