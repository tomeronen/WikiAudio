package com.example.wikiaudio.location;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

public class LocationTracker {
    private Location curLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private ComponentActivity activity;
    private FusedLocationProviderClient fusedLocationClient;


    public LocationTracker(ComponentActivity ownerActivity) {
        this.activity = ownerActivity;
        createLocationRequest();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public LocationTracker setLocationCallback(LocationCallback locationCallback)
    {
        this.locationCallback = locationCallback;
        return this;
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


    public Task<Void> startTracking() {
        boolean hasGpsPermissions = ActivityCompat.
                checkSelfPermission(activity.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED;
        if (hasGpsPermissions) {
            return startLocationUpdates();
        }
        else
        {
            // TODO what if there are no GpsPermissions.
            return null;
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

    private Task<Void> startLocationUpdates() {
        if(this.locationCallback != null)
        {
            return fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }
        else
        {
            return null;
        }
    }

    public Location getCurLocation() {
        return curLocation;
    }
}
