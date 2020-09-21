package com.example.wikiaudio.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wikiaudio.playlist.Playlist;
import com.example.wikiaudio.playlist.PlaylistsHandler;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationHandler {
    //For logs
    private static final String TAG = "LocationHandler";
    private static final int RADIUS = 10000; // let user choose?
    private static final int MAP_CAMERA_ZOOM_RADIUS = 15;

    private static LocationHandler instance = null;

    private AppCompatActivity activity;
    private PlaylistsHandler playlistsHandler;
    private Wikipedia wikipedia;

    //Location related
    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private LocationHandler(AppCompatActivity activity) {
        this.activity = activity;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        locationUpdates();
    }

    public static LocationHandler getInstance(AppCompatActivity activity) {
        if (instance == null) {
            instance = new LocationHandler(activity);
        }
        return instance;
    }

    public void setGoogleMap(GoogleMap map) {
        mMap = map;
    }
//
//    public LocationHandler(AppCompatActivity activityCompat, PlaylistsHandler playlistsHandler,
//                           Wikipedia wikipedia, final GoogleMap mMap) {
//        this.activity = activityCompat;
//        this.playlistsHandler = playlistsHandler;
//        this.wikipedia = wikipedia;
//        this.mMap = mMap;
//        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
//        locationUpdates();
//    }

    /**
     * What to do for when the location updates or the GPS provider goes on/off
     */
    @SuppressLint("MissingPermission")
    private void locationUpdates(){
        if (isLocationPermissionGranted()){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, MAP_CAMERA_ZOOM_RADIUS, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d(TAG, "locationUpdates: onLocationChanged");
                            //Whenever the user's location changes,
                            //we recenter the map at the user's location
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_CAMERA_ZOOM_RADIUS));
                            //todo: maybe update the Nearby playlist here
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {}

                        @Override
                        public void onProviderEnabled(String provider) {
                            //gps went on (this may change) so
                            //we recenter the map at the user's location
                            Log.d(TAG, "locationUpdates: onProviderEnabled");
                            LatLng latLng = getCurrentLocation();
                            if (latLng != null && mMap != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        latLng, MAP_CAMERA_ZOOM_RADIUS));
                                playlistsHandler.createLocationBasedPlaylist
                                        (latLng.latitude, latLng.longitude, true);
                            }
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            //gps went off
                            Log.d(TAG, "locationUpdates: onProviderDisabled");
                            Toast.makeText(activity, "Please enable your GPS for location services",
                                    Toast.LENGTH_LONG).show();
                            //todo: maybe center the map at the current played wikipage?
                        }
                    });
        }
    }

    /**
     * Pretty self-explanatory, really.
     * @return a latitude-longitude tuple of the user's current location
     */
    public LatLng getCurrentLocation() {
        if (isLocationPermissionGranted()) {
            if (locationManager == null) {
                return null;
            }
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
            if (location != null) {
                Log.d(TAG, "getCurrentLocation: got current location");
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                return new LatLng(lat, lng);
            } else {
                return null;
            }
        } else {
            requestLocationPermission();
            return null;
        }
    }

    /**
     * Pretty self-explanatory, really.
     * @return true if are granted, false ow.
     */
    private boolean isLocationPermissionGranted(){
        if (activity == null) {
            Log.d(TAG, "isLocationPermissionGranted: null activity");
            return false;
        }
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Pretty self-explanatory, really.
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Marks the given Wikipage object on the map using its title and coordinates
     * @param wikipage the article we would like to mark
     */
    public void markLocation(Wikipage wikipage) {
        if (wikipage == null || wikipage.getLat() == null || wikipage.getLon() == null
                || wikipage.getTitle() == null || wikipage.getThumbnailSrc() == null) {
            Log.d(TAG, "markLocation: got some null ref regarding the Wikipedia object");
            return;
        }
        LatLng latLng = new LatLng(wikipage.getLat(), wikipage.getLon());
        mMap.addMarker(new MarkerOptions()
                .position(latLng).title(wikipage.getTitle())).setTag(wikipage);
        //TODO: add thumbnail one day
    }

    public void markPlaylist(Playlist playlist) {
        clearMap();
        if (playlist == null || playlist.getWikipages() == null) {
            Log.d(TAG,"markPlaylist: got null playlist or playlist's wikipages array is null");
            return;
        }
        for (Wikipage wikipage: playlist.getWikipages()) {
            markLocation(wikipage);
        }
    }

    // TODO
    // do we want to create and display a path of the playlist's locations?
    // we will also have to play the articles in that same order
    public void createPath(Location[] locations) {
    }

    /**
     * Removes all markers, overlays, and polylines from the map.
     */
    public void clearMap() {
        mMap.clear();
    }
}
