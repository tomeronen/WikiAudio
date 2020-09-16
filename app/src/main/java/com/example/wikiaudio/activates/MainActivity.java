package com.example.wikiaudio.activates;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.WorkManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.choose_categories.ChooseCategoriesActivity;
import com.example.wikiaudio.activates.record_page.WikiRecordActivity;
import com.example.wikiaudio.location.LocationHandler;
import com.example.wikiaudio.wikipedia.PageAttributes;
import com.example.wikiaudio.wikipedia.WikiPage;
import com.example.wikiaudio.wikipedia.Wikipedia;
import com.example.wikiaudio.wikipedia.WorkerListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import com.example.wikiaudio.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.wikiaudio.activates.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    //For logs
    private static final String TAG = "MainActivity";

    AppCompatActivity activity;

    //Wikipedia facade object
    Wikipedia wikipedia;

    //Google services related (error for handling when the google service version is incorrect)
    private static final int ERROR_DIALOG_REQUEST = 9002;

    //Location related
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionGranted = false;
    private Boolean isGPSEnabled = false;
    private GoogleMap mMap;
    LocationHandler locationHandler;
    ArrayList<PlayListFragment> playLists = new ArrayList<>();
    private List<String> chosenCategories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadPlayLists();
        initVars();
        initMap();
    }

    private void loadPlayLists() {
        PlayListsFragmentAdapter playListsFragmentAdapter =
                new PlayListsFragmentAdapter(getSupportFragmentManager());
        chosenCategories  = ((WikiAudioApp) getApplication())
                .getAppData().getChosenCategories();
        chosenCategories.add("pages Nearby");
        for(String category: chosenCategories)
        {
            List<WikiPage> testingContent = new ArrayList<>();
            WikiPage wikiPage = new WikiPage();
            wikiPage.setTitle("test");
            testingContent.add(wikiPage);
            PlayListFragment playListFragment = new PlayListFragment(testingContent);
            playListsFragmentAdapter.addFrag(playListFragment);
        }
        ViewPager viewPager =
                findViewById(R.id.view_pager);
        viewPager.setAdapter(playListsFragmentAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        int counter = 0;
        for(String category: chosenCategories) // todo - not best. we do this twice.
        {
            tabs.getTabAt(counter).setText(category);
            counter++;
        }
    }


    private void testChooseCategoriesActivity() {
        Intent intent = new Intent(activity, ChooseCategoriesActivity.class);
        startActivity(intent);
    }

    private void testWikiRecordActivity() {
        final WikiPage testPage = new WikiPage();
        String pageName = "Hurricane_Irene_(2005)";
        List<PageAttributes> pageAttributes = new ArrayList<>();
        pageAttributes.add(PageAttributes.title);
        pageAttributes.add(PageAttributes.content);
        wikipedia.getWikiPage(pageName,
                pageAttributes,
                testPage,
                new WorkerListener() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(activity, WikiRecordActivity.class);
                        Gson gson = new Gson();
                        String wiki = gson.toJson(testPage);
                        intent.putExtra(WikiRecordActivity.WIKI_PAGE_TAG, wiki);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure() {

                    }
                }
        );
    }

    /**
     * Pretty self-explanatory, really.
     */
    private void initVars() {
        //Check for location perms
        mLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED;
        activity = this;
        wikipedia = new Wikipedia(this);
    }

    /**
     * For initializing the GoogleMaps fragment
     */
    private void initMap() {
        if (isGoogleServicesOK()) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragmentMap);
            if (mapFragment == null) {
                Log.d(TAG, "initMap: mapFragment is null");
            } else {
                mapFragment.getMapAsync(this);
            }
        } else {
            Log.d(TAG, "initMap: google services is not ok :(");
        }
    }

    /**
     * Pretty self-explanatory, really.
     * @return true if the version will enable our used Google API, false ow.
     */
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
            Toast.makeText(activity, "Oops, can't make any map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Simple permission request callback handler
     * @param requestCode int code of the permission request we made
     * @param permissions a list of strings representing the permissions
     * @param grantResults the results of each permission request from the user
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //TODO this will change when we'll add audio and storing permissions
        mLocationPermissionGranted = false;
//        Log.d(TAG, "onRequestPermissionsResult: ");
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = false;
                        Toast.makeText(activity,
                                "Can't create a location based playlist without your location :)",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                mLocationPermissionGranted = true;
                onMapReady(mMap);
            } else {
                // explain why we need these permissions
                Toast.makeText(activity,
                        "Can't create a location based playlist without your location :)",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Callback for when the GoogleMaps map is ready
     * This is where we set all related listeners, init location and map related objects
     * @param googleMap our map object
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            Log.d(TAG, "onMapReady: google map is null; map related actions will not work");
        } else {
            if (mLocationPermissionGranted) {
//                Log.d(TAG, "onMapReady: google map is NOT null & we have perm");
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMyLocationClickListener(this);
                mMap.setOnInfoWindowClickListener(this);

                locationHandler = new LocationHandler(activity, mMap);
                GPSEnabler(); //TODO
                initUserLocationAndMap();
            } else {
                //request permissions
                Log.d(TAG, "onMapReady: google map is NOT null & we DON'T have perm");
                requestLocationPermission();
            }
        }
    }

    /**
     * A simple location permission request for FINE, COARSE and INTERNET
     */
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Enables GoogleMaps location tracking, focuses the camera on the user's location and
     * presents nearby wikipages as markers
     */
    @SuppressLint("MissingPermission")
    private void initUserLocationAndMap() {
        if (mMap != null) {
            //Add here any action that you would like to appear as soon as the map opens if
            //we have the user's location
            Log.d(TAG, "enableMyLocation: google map is NOT null & we have perm");
            mMap.setMyLocationEnabled(true);

            //Zoom to user's location + show nearby wikipages
            LatLng currentLatLng = locationHandler.getCurrentLocation();
            if (currentLatLng != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                locationHandler.markWikipagesNearby(wikipedia);
            }
        }
    }

    //TODO
    private void GPSEnabler() {
        LocationServices
                .getSettingsClient(activity)
                .checkLocationSettings(new LocationSettingsRequest.Builder().build())
                .addOnSuccessListener(activity,
                        new OnSuccessListener<LocationSettingsResponse>()
                        {
                            @Override
                            public void onSuccess(LocationSettingsResponse
                                                          locationSettingsResponse) {
                                isGPSEnabled = true;
                                Log.d(TAG, "GPSEnabler: GPS IS ON");
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isGPSEnabled = false;
                        Log.d(TAG, "GPSEnabler: GPS IS OFF");
                        Toast.makeText(activity, "Please enable your GPS for location services", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * When the user clicks on a location on the map
     * We must implement this as part of the GoogleMaps API
     * We don't want that kind of interaction on the map
     */
    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    /**
     * The "center me" button in the right up edge of the map
     * Centers and zooms the user to its location
     */
    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        if (!isGPSEnabled) {
            //TODO
            Toast.makeText(activity, "Please enable your GPS for location services",
                    Toast.LENGTH_LONG).show();
        }
        return false;
    }
    /**
     * When the user clicks on a marker on the map, will show the marker's title in the info box
     * Other than this default action we don't want more interactions
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    /**
     * When the user clicks on the info box of the marker
     * @param marker GoogleMaps marker that contains the Wikipage tag
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
//        WikiPage tag = (WikiPage) marker.getTag();
//        TODO transfer to wikipage activity
        Toast.makeText(activity, "You clicked on wikipage: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
    }

}