package com.example.wikiaudio.location;

import android.Manifest;
import android.util.Log;
import android.annotation.SuppressLint;

import android.content.Context;
import android.content.pm.PackageManager;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class LocationHandler {
    //For logs
    private static final String TAG = "LocationHandler";

    private AppCompatActivity activity;

    //Location related
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;


    public LocationHandler(AppCompatActivity activityCompat, GoogleMap mMap) {
        this.activity = activityCompat;
        this.mMap = mMap;
    }

    /**
     * Pretty self-explanatory, really.
     * @return a latitude-longitude tuple of the user's current location
     */
    public LatLng getCurrentLocation() {
        if (isLocationPermissionGranted()) {
            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
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
     * Marks the given wikipage object on the map using its title and coordinates
     * @param wikiPage the article we would like to mark
     */
    public void markLocation(Wikipage wikiPage) {
        if (wikiPage != null) {
            LatLng latLng = new LatLng(wikiPage.getLat(), wikiPage.getLon());
            mMap.addMarker(new MarkerOptions()
                                    .position(latLng).title(wikiPage.getTitle())).setTag(wikiPage);
        } else {
            Log.d(TAG, "markLocation: got null wikipage object");
        }
    }

    /**
     * Create marks on the map of the nearby wikipedia articles based on current location
     * @param wikipedia: the wiki facade for getting the nearby articles
     */
    public void markWikipagesNearby(final Wikipedia wikipedia) {
        LatLng currentLocation = getCurrentLocation();
        if (currentLocation == null) {
            Log.d(TAG,"markWikipagesNearby: currentLocation == null");
            return;
        }

        double lat = currentLocation.latitude;
        double lng = currentLocation.longitude;

        final List<Wikipage> pagesNearby = new ArrayList<>();
        ArrayList<PageAttributes> pageAttributes = new ArrayList<>();
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.coordinates);
        //todo maybe add thumbnail:
        //https://developers.google.com/maps/documentation/android-sdk/infowindows#custom_info_windows

        wikipedia.getPagesNearby(lat, lng, 10000, pagesNearby, pageAttributes,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG,"markWikipagesNearby-WorkerListener-onSuccess: we found pages nearby!");
                        for(Wikipage page:pagesNearby) {
                            markLocation(page);
                        }

                    }
                    @Override
                    public void onFailure() {
                        Log.d(TAG,"markWikipagesNearby-WorkerListener-onFailure: couldn't find pages nearby");
                    }
                });
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
