package com.wikiaudioapp.wikiaudio.location;

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

import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;
import com.wikiaudioapp.wikiaudio.data.Holder;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * This is where we handle location related actions - from marking wikipages on the map to checking
 * if the user moved "enough" so we'll present it a new Nearby playlist.
 */
public class LocationHandler {
    //For logs
    private static final String TAG = "LocationHandler";

    //Constants
    private static final int MAP_CAMERA_ZOOM_RADIUS = 15;
    private static final int MINIMUM_DISTANCE_TO_CREATE_NEW_NEARBY_PLAYLIST = 5000;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    //Vars
    private static LocationHandler instance = null;
    private AppCompatActivity activity;

    //Location
    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean shouldUpdateZoomAndCreateNearby = false;


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

    /**
     * What to do for when the location updates or the GPS provider goes on/off
     */
    @SuppressLint("MissingPermission")
    private void locationUpdates() {
        if (isLocationPermissionGranted()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, MAP_CAMERA_ZOOM_RADIUS, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d(TAG, "locationUpdates: onLocationChanged");
                            LatLng currentLocation = new LatLng(location.getLatitude(),
                                    location.getLongitude());
                            double currentLat = currentLocation.latitude;
                            double currentLng = currentLocation.longitude;

                            if (shouldUpdateZoomAndCreateNearby) {
                                shouldUpdateZoomAndCreateNearby = false;
                                recenterMapAndCreateNearbyPlaylist(currentLocation, currentLat, currentLng);
                            } else {
                                checkForNearbyUpdate(currentLocation, currentLat, currentLng);
                            }
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                            //gps went on (this may change) so
                            //we recenter the map at the user's location
                            Log.d(TAG, "locationUpdates: onProviderEnabled");
                            shouldUpdateZoomAndCreateNearby = true;
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            //gps went off
                            Log.d(TAG, "locationUpdates: onProviderDisabled");
                            Toast.makeText(activity, "Please enable your GPS for location services",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    /**
     * Recenter the map at user's location and creates a nearby playlist.
     */
    private void recenterMapAndCreateNearbyPlaylist(LatLng currentLocation, double currentLat,
                                                    double currentLng) {
        Log.d(TAG, "locationUpdates: onLocationChanged shouldUpdateZoomAndCreateNearby");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, MAP_CAMERA_ZOOM_RADIUS));
        Holder.playlistsManager.createLocationBasedPlaylist(currentLat, currentLng, true);
    }

    /**
     * Checks if the new location is far enough for creating a new nearby playlist.
     */
    private void checkForNearbyUpdate(LatLng currentLocation, double currentLat,
                                      double currentLng) {
        if (Holder.playlistsManager.getNearby() != null) {
            Playlist nearby = Holder.playlistsManager.getNearby();
            float[] results = {0};
            Location.distanceBetween(nearby.getLat(), nearby.getLon(), currentLat, currentLng, results);
            if (results[0] > MINIMUM_DISTANCE_TO_CREATE_NEW_NEARBY_PLAYLIST) {
                recenterMapAndCreateNearbyPlaylist(currentLocation, currentLat, currentLng);
            }
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
                Log.d(TAG, "enableMyLocation: null provider");
                return null;
            }
            // Getting Current Location
            @SuppressLint("MissingPermission") Location location = locationManager
                    .getLastKnownLocation(provider);

            if (location != null) {
                Log.d(TAG, "getCurrentLocation: got current location");
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                return new LatLng(lat, lng);
            } else {
                location = this.backUpLastKnownLocation(); // try getting with helper function
                if (location != null) {
                    Log.d(TAG, "getCurrentLocation: got current location");
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    return new LatLng(lat, lng);
                }
                else
                {
                    return null;
                }
            }
        } else {
            requestLocationPermission();
            return null;
        }
    }

    /**
     * Pretty self-explanatory, really.
     */
    private boolean isLocationPermissionGranted() {
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
     */
    public void markLocation(Wikipage wikipage) {
        if (wikipage == null || wikipage.getLat() == null || wikipage.getLon() == null
                || wikipage.getTitle() == null) {
            Log.d(TAG, "markLocation: got some null ref regarding the Wikipedia object");
            return;
        }
        LatLng latLng = new LatLng(wikipage.getLat(), wikipage.getLon());
        mMap.addMarker(new MarkerOptions()
                .position(latLng).title(wikipage.getTitle())).setTag(wikipage);
    }

    /**
     * Marks the given Playlist object on the map by marking all of its wikipages
     */
    public void markPlaylist(Playlist playlist) {
        clearMap();
        if (playlist == null || playlist.getWikipages() == null) {
            Log.d(TAG, "markPlaylist: got null playlist or playlist's wikipages array is null");
            return;
        }
        for (Wikipage wikipage : playlist.getWikipages()) {
            markLocation(wikipage);
        }
    }

    /**
     * Removes all markers, overlays, and polylines from the map.
     */
    public void clearMap() {
        mMap.clear();
    }

    /**
     * Marks the given Wikipage object on the map & zooms the camera on it
     */
    public void markAndZoom(Wikipage wikipage) {
        if (wikipage == null || wikipage.getLat() == null || wikipage.getLon() == null
                || wikipage.getTitle() == null) {
            Log.d(TAG, "markAndZoom: got some null ref regarding the Wikipedia object");
            return;
        }
        clearMap();
        LatLng latLng = new LatLng(wikipage.getLat(), wikipage.getLon());
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(wikipage.getTitle()));
        marker.setTag(wikipage);
        marker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_CAMERA_ZOOM_RADIUS));
    }


    private Location backUpLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (!isLocationPermissionGranted()) {
                return null;
            }
            else
            {
                @SuppressLint("MissingPermission") Location l
                        = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
        }
        return bestLocation;
    }
}
