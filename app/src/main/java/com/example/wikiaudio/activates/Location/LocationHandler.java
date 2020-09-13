package com.example.wikiaudio.activates.Location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationHandler {

    private static final String TAG = "LocationHandler";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private AppCompatActivity activity;
    private GoogleMap mMap;
    private Boolean mLocationPermissionGranted = false;

    public LocationHandler(AppCompatActivity activityCompat, GoogleMap mMap) {
        this.activity = activityCompat;
        this.mMap = mMap;

        mLocationPermissionGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED;
    }

    public Location getCurrentLocation() {
        if (mLocationPermissionGranted) {
            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);
            if (provider == null) {
                //                    Log.d(TAG, "enableMyLocation: null provider");
                return null;
            }
            // Getting Current Location
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, "enableMyLocation: got current location");
            return location; //which may be null
        } else {
            requestLocationPermission();
            return null;
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    //todo: add a tag object, we need to agree on a tag format
    public void markLocation(LatLng latLng, String title) {
        if (latLng != null) {
            mMap.addMarker(new MarkerOptions().position(latLng).title(title));
            //.setTag(tag);
//            Log.d(TAG, "markLocation: marker should be on " + title);
        }
    }

    //todo: do we want to create and display a path of the playlist's locations?
    // we will also have to play the articles in that same order
    public void createPath(Location[] locations) {
    }
}
