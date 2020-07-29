package com.example.wikiaudio.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationTracker {
    private Location curLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private ComponentActivity activity;
    private FusedLocationProviderClient fusedLocationClient;


    public LocationTracker(ComponentActivity ownerActivity, LocationCallback locationCallback) {
        this.activity = ownerActivity;
        createLocationRequest();
        this.locationCallback = locationCallback;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    //TODO Make a wrapper that calls the param function only if distance changed enough (100m?)
//    private LocationCallback wrapLocationCallBack(LocationCallback locationCallback) {
//        return new LocationCallback(){
//            @Override
//            public void onLocationResult(LocationResult locationResult) {
//                if (locationResult == null) {
//                    return;
//                }
//                for (Location location : locationResult.getLocations()) {
//                    // Update UI with location data
//                    // ...
//                }
//            }
//        };
//    }


    public void startTracking() {
        boolean hasGpsPermissions = ActivityCompat.
                checkSelfPermission(activity.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
        if (hasGpsPermissions) {
            startLocationUpdates();
        }
    }

    public void stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    static void sendLocationChangeBroadcast(Context context) {
        Intent intent = new Intent().setAction("locationChange");
        context.sendBroadcast(intent);
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    public Location getCurLocation() {
        return curLocation;
    }
}
