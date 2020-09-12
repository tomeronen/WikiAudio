package com.example.wikiaudio.activates;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.wikiaudio.R;
import com.example.wikiaudio.location.LocationTracker;
import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.WikiPage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "MainActivity";

    Wikipedia wikipedia;
    AppCompatActivity activity;

    //Google services related (error for handling when the google service version is incorrect)
    private static final int ERROR_DIALOG_REQUEST = 9002;

    //Location related
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionGranted = false;
    LocationTracker locationTracker;

    //Map related
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();

        if (isGoogleServicesOK()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragmentMap);
            if (mapFragment == null) {
                Log.d(TAG, "onCreate: mapFragment is null");
            } else {
                mapFragment.getMapAsync(this);
//                initMap();
            }

        } else {
            Log.d(TAG, "google services not ok");
        }

        WorkManager.getInstance(this).cancelAllWork();  // todo debug

        wikipedia = Wikipedia.getInstance();
        activity = this;
        showCategories();
        final List<WikiPage> pagesNear = new ArrayList<>();
        final List<WikiPage> searchResults = new ArrayList<>();
        ArrayList<PageAttributes> pageAttributes = new ArrayList<>();
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.coordinates);
        pageAttributes.add(PageAttributes.content);

        wikipedia.getPagesNearby(32.0623506,
                34.7747997,
                10000,
                pagesNear,
                pageAttributes,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        if (pagesNear.size() == 10) {
                            Log.d("s", "s");
                        }

                    }

                    @Override
                    public void onFailure() {

                    }
                });


//        WikiPage wp = WikiPage.getPageForTesting();
//        Gson gson = new Gson();
//        String pageJsonString = gson.toJson(wp);
//        Intent rec = new Intent(this, WikiRecordActivity.class);
//        rec.putExtra(WikiRecordActivity.WIKI_PAGE_TAG, pageJsonString);
//        startActivity(rec);


//        WikiPage a = WikiTextParser.parseWikiHtml("https://en.wikipedia.org/wiki/Quark");
//        wikipedia.login("a","b");
        // for debug:

//        wikipedia = Wikipedia.getInstance();
//        wikipedia.getPagesNearby(this,32.443814,34.892546);
//        showCategories();
//        locationTracker = new LocationTracker(this);
    }

    private void initVars() {
        //Check for location perms
        mLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void showCategories() {
        UUID loadCategoriesId = wikipedia.loadSpokenPagesCategories(this);

    }

    private void initMap() {
        Log.d(TAG, "initMap: tried to init the map");
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragmentMap, mapFragment);
        fragmentTransaction.commit();
    }

    public boolean isGoogleServicesOK() {
        Log.d(TAG, "isGoogleServicesOK: verifying Google services' version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            // we're good
            Log.d(TAG, "isGoogleServicesOK: Google play version is ok");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            // if we can resolve the error
            Log.d(TAG, "isGoogleServicesOK: there's a resolvable error");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            // nothing we can do :(
            Toast.makeText(this, "Oops, can't make any map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        Log.d(TAG, "onRequestPermissionsResult: ");
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = false;
                        break;
                    }
                }
                mLocationPermissionGranted = true;
                enableMyLocation();
            } else {
                // explain why we need these permissions
                Toast.makeText(this, "Can't create a location bases playlist without the location :)", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            Log.d(TAG, "onMapReady: google map is null; map related actions will not work");
        } else {
            if (mLocationPermissionGranted) {
                Log.d(TAG, "onMapReady: google map is NOT null & we have perm");
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMyLocationClickListener(this);
                enableMyLocation();
            } else {
                //request permissions
                Log.d(TAG, "onMapReady: google map is NOT null & we DON'T have perm");
                requestLocationPermission();
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (mLocationPermissionGranted) {
            if (mMap != null) {
                Log.d(TAG, "enableMyLocation: google map is NOT null & we have perm");
                mMap.setMyLocationEnabled(true);
                Log.d(TAG, "enableMyLocation: setMyLocationEnabled is enabled");
                // Getting LocationManager object from System Service LOCATION_SERVICE
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                // Creating a criteria object to retrieve provider
                Criteria criteria = new Criteria();

                // Getting the name of the best provider
                String provider = locationManager.getBestProvider(criteria, true);
                if (provider == null) {
                    Log.d(TAG, "enableMyLocation: null provider");
                    return;
                }

                // Getting Current Location
                Location location = locationManager.getLastKnownLocation(provider);
                Log.d(TAG, "enableMyLocation: got current location");

                if (location != null) {
                    // Getting latitude of the current location
                    double latitude = location.getLatitude();

                    // Getting longitude of the current location
                    double longitude = location.getLongitude();

                    // Creating a LatLng object for the current location
                    LatLng latLng = new LatLng(latitude, longitude);

                    mMap.addMarker(new MarkerOptions().position(latLng).title("Start"));
                    Log.d(TAG, "enableMyLocation: marker should be on");
                }
            } else {
                requestLocationPermission();
            }
        }
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}
