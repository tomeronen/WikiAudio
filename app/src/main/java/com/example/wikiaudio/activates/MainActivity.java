package com.example.wikiaudio.activates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.work.WorkManager;

import com.example.wikiaudio.AppData;
import com.example.wikiaudio.Holder;
import com.example.wikiaudio.R;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.choose_categories.ChooseCategoriesActivity;
import com.example.wikiaudio.activates.playlist_ui.PlaylistFragment;
import com.example.wikiaudio.activates.playlist_ui.PlaylistsFragmentAdapter;
import com.example.wikiaudio.playlist.Playlist;
import com.example.wikiaudio.playlist.PlaylistsHandler;
import com.example.wikiaudio.wikipedia.Wikipage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    //For logs
    private static final String TAG = "MainActivity";
    private static final int CHOOSE_CATEGORY_TAG = 10172 ;

    private AppCompatActivity activity;

    //Google services related (error for handling when the google service version is incorrect)
    private static final int ERROR_DIALOG_REQUEST = 9002;

    //Location related
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionGranted = false;
    private Boolean isGPSEnabled = false;
    private Boolean mapWasInit= false;
    private GoogleMap mMap;


    //Playlist related
    private PlaylistsFragmentAdapter playlistsFragmentAdapter;

    // idk
    private ArrayList<PlaylistFragment> playLists = new ArrayList<>();
    private List<String> chosenCategories;

    //Views
    private ImageButton chooseCategories;
    private SearchView searchBar;

    private TabLayout tabs;
    private ProgressBar loadingIcon;
    private MediaPlayerFragment mediaPlayerFragment;
    private PlaylistFragment searchResultFragment;
    private ViewPager viewPager;
    private AppData appData;
    private SupportMapFragment mapFragment;


    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        cleanData(); //todo if wanted for debugging.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();
        setOnClickButtons();
        initMap();
        loadPlaylists();
        initMediaPlayer();
//        testMediaPlayer();
    }


    private void testMediaPlayer() {
        Playlist playList = playlistsFragmentAdapter.getItem(0).getPlaylist();
        new Thread(() -> appData.setPlaylist(new Playlist("Biology", false, 0, 0))).start();
    }

    private void cleanData() {
        WorkManager.getInstance(this).cancelAllWork();
        ((WikiAudioApp) getApplication()).getAppData().saveChosenCategories(new ArrayList<>());
        ((WikiAudioApp) getApplication()).getAppData().setCategories(new ArrayList<>());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        try {
            Date date = sdf.parse("01/01/2000"); // just a very far date.
            ((WikiAudioApp) getApplication()).getAppData().setLastLoadedCategories(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


//    private void testUploadFile() {
//        FileManager fileManager = new FileManager(this);
//        String fp = fileManager.getFilePath("BenDeLaCreme",
//                1,
//                3)
//                + "." + "3gp";
//        String fileName = "BenDeLaCreme.3gp";
//        wikipedia.uploadFile(fileName,fp);
//    }


    private void initMediaPlayer() {
        mediaPlayerFragment = (MediaPlayerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.audioPlayerFragment);
        if (mediaPlayerFragment != null) {
            mediaPlayerFragment.showTitle(true);
        } else {
            Log.d(TAG, "initMediaPlayer: we got null mediaPlayerFragment");
        }
    }

//    //  todo option B check if crasches app.
//    @Override
//    public void onResume(){
//        super.onResume();
//        if(tabs != null)
//        {
//                setUpTabs();
//                loadPlaylists();
//                return;
//        }

//        for (int i =0; i < tabCount; i++)
//        {
//
//        }
//        if(chosenCategories != ((WikiAudioApp)getApplication()).getAppData().getChosenCategories())
//        {
//             reload playlists
//            loadPlayLists();
//        }

//        }

    /**
     * Pretty self-explanatory, really.
     */
    private void initVars() {
        activity = this;
        Holder.getInstance(activity); // Holds all of the app's facades/singletons

        //Check for location perms
        mLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED;
        activity = this;
        chooseCategories = findViewById(R.id.chooseCategories);
        searchBar = findViewById(R.id.search_bar);
        searchBar.setIconified(true);
        searchBar.onActionViewCollapsed();
        loadingIcon = findViewById(R.id.progressBar4);
        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);
        chosenCategories = ((WikiAudioApp) getApplication())
                .getAppData().getChosenCategories();
        appData =((WikiAudioApp) getApplication()).getAppData();


    }


    /**
     * For initializing the GoogleMaps fragment
     */
    private void initMap() {
        if (isGoogleServicesOK()) {
            mapFragment = (SupportMapFragment) getSupportFragmentManager()
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

    private boolean needToReloadTabs() {
        // todo change to set? what happens if we take down and add again. order changes.
        if(PlaylistsHandler.getPlaylists().size() != chosenCategories.size())
        {
            return true;
        }
        for(String category: chosenCategories)
        {
            if(!categoryInPlaylists(category))
            {
                return true;
            }
        }
        return false;
    }


    private void setOnClickButtons() {
        chooseCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseCategoriesIntent =  new Intent(activity,
                        ChooseCategoriesActivity.class);
                startActivityForResult(chooseCategoriesIntent, CHOOSE_CATEGORY_TAG);
            }
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Playlist playlist = Holder.playlistsHandler.createSearchBasedPlaylist(query);
                searchResultFragment = playlist.getPlaylistFragment();
                searchResultVisibility(true, searchResultFragment);
                searchBar.onActionViewCollapsed();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchResultVisibility(boolean bool, PlaylistFragment searchResultFragment) {
        if(searchResultFragment == null)
        {
            return;
        }
        int othersAreVisible;
        if (bool) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .addToBackStack(null)
                    .replace(R.id.search_result_placeholder, searchResultFragment)
                    .commit();
            othersAreVisible = GONE;
        } else {
            searchResultFragment.getView().setVisibility(GONE);
            othersAreVisible = VISIBLE;
        }
        viewPager.setVisibility(othersAreVisible);
        tabs.setVisibility(othersAreVisible);
        chooseCategories.setVisibility(othersAreVisible);
        mapFragment.getView().setVisibility(othersAreVisible);
        findViewById(R.id.mapBackground).setVisibility(othersAreVisible);
    }

    /**
     * Loads the playlists fragment based on favorite categories and, if enabled, also
     * based on location
     */
    private void loadPlaylists() {
        if(loadingIcon == null) {
            loadingIcon = findViewById(R.id.progressBar4);
        }
        loadingIcon.setVisibility(VISIBLE);
//        final PlaylistsFragmentAdapter playListsFragmentAdapter =
//                new PlaylistsFragmentAdapter(getSupportFragmentManager());
        new Thread(() -> {
            Holder.playlistsHandler.createCategoryBasedPlaylists(chosenCategories);
            playlistsFragmentAdapter = new PlaylistsFragmentAdapter(getSupportFragmentManager());
            //Add all playlists as fragments to the adapter
            for (Playlist playlist: PlaylistsHandler.getPlaylists())
                playlistsFragmentAdapter.addPlaylistFragment(playlist.getPlaylistFragment());
            activity.runOnUiThread(() -> {
                ViewPager viewPager = findViewById(R.id.view_pager);
                viewPager.setAdapter(playlistsFragmentAdapter);
                tabs = findViewById(R.id.tabs);
                tabs.setupWithViewPager(viewPager);
                int counter = 0;
                for (Playlist playlist: PlaylistsHandler.getPlaylists()) {
                    Objects.requireNonNull(tabs.getTabAt(counter)).setText(playlist.getTitle());
                    counter++;
                }
                loadingIcon.setVisibility(GONE);
            });
        }).start();
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
                if (!mapWasInit) {
                    Log.d(TAG, "onRequestPermissionsResult: map was not init, initing it now");
                    onMapReady(mMap);
                }
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
            if (mLocationPermissionGranted && !mapWasInit) {
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMyLocationClickListener(this);
                mMap.setOnInfoWindowClickListener(this);
                Holder.locationHandler.setGoogleMap(mMap);
                initUserLocationOnTheMap();
            } else {
                if (!mLocationPermissionGranted){
                    Log.d(TAG, "onMapReady: google map is NOT null & we DON'T have perm");
                    requestLocationPermission();
                }
                mapWasInit = false;
            }
        }
    }

    /**
     * Enables GoogleMaps location tracking & focuses the camera on the user's location
     * (Add here any action that you would like to appear as soon as the map opens)
     */
    @SuppressLint("MissingPermission")
    private void initUserLocationOnTheMap() {
        if (mMap != null && mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            isLocationEnabled();
            if (isGPSEnabled) {
                LatLng currentLatLng = Holder.locationHandler.getCurrentLocation();
                if (currentLatLng != null) {
                    //Zoom in to user's location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    //Create (and display) the nearby playlist
                    Holder.playlistsHandler.createLocationBasedPlaylist(
                            currentLatLng.latitude, currentLatLng.longitude, true);
                }
            } else {
                Log.d(TAG, "initUserLocationOnTheMap: no GPS");
            }
        } else {
            Log.d(TAG, "initUserLocationOnTheMap: either google map is null or we have no perm");
        }
    }

    /**
     * Checks if location services are on.
     * If not, displays a request to turn them on.
     */
    public void isLocationEnabled() {
        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            isGPSEnabled = false;
            Log.d(TAG, "GPSEnabler: null locationManager, something went wrong");
        } else {
            isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGPSEnabled) {
                Toast.makeText(activity, "Please enable your GPS for location services",
                        Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "GPSEnabler: GPS is Enabled");
            }
        }
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
        isLocationEnabled();
        PlaylistsHandler.displayNearbyPlaylistOnTheMap();
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
     * When the user clicks on the info box of the marker we redirect to its wikipage activity
     * @param marker GoogleMaps marker that contains the Wikipage tag
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        Wikipage wikipage = (Wikipage) marker.getTag();
        if (wikipage != null) {
            Playlist playlist = wikipage.getPlaylist();
            if (playlist != null) {
                int index = playlist.getIndexByWikipage(wikipage);
                if (index > -1) {
                    Intent WikipageIntent = new Intent(this, WikipageActivity.class);
                    WikipageIntent.putExtra("playlistTitle", playlist.getTitle());
                    WikipageIntent.putExtra("index", index);
                    startActivity(WikipageIntent);
                    return;
                } else {
                    Log.d(TAG, "onInfoWindowClick: index is bad");
                }
            } else {
                Log.d(TAG, "onInfoWindowClick: playlist is null :(");
            }
        }
        Log.d(TAG, "onInfoWindowClick: marker's tag is null :(");
    }


//    private void testChooseCategoriesActivity() {
//        Intent intent = new Intent(activity, ChooseCategoriesActivity.class);
//        startActivity(intent);
//    }

//    private void testWikiRecordActivity() {
//        final Wikipage testPage = new Wikipage();
//        String pageName = "Hurricane_Irene_(2005)";
//        List<PageAttributes> pageAttributes = new ArrayList<>();
//        pageAttributes.add(PageAttributes.title);
//        pageAttributes.add(PageAttributes.content);
//        wikipedia.getWikipage(pageName,
//                pageAttributes,
//                testPage,
//                new WorkerListener() {
//                    @Override
//                    public void onSuccess() {
//                        Intent intent = new Intent(activity, WikiRecordActivity.class);
//                        Gson gson = new Gson();
//                        String wiki = gson.toJson(testPage);
//                        intent.putExtra(WikiRecordActivity.WIKI_PAGE_TAG, wiki);
//                        startActivity(intent);
//                    }
//                    @Override
//                    public void onFailure() {
//                    }
//                }
//        );
//    }

//    private void testMediaPlayerFragment() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                String category = "Biology";
//                final List<String> results = new ArrayList<>();
//                wikipedia.loadSpokenPagesNamesByCategories(category,
//                        results,
//                        new WorkerListener() {
//                            @Override
//                            public void onSuccess() {
//                                final List<Wikipage> playableList = new ArrayList<>();
//                                int numberOfPagesTryLoading = 0;
//                                for(final String pageName: results) {
//                                    numberOfPagesTryLoading++;
//                                    final Wikipage wikipage = new Wikipage();
//                                    ArrayList<PageAttributes> pageAttributes = new ArrayList<>();
//                                    pageAttributes.add(PageAttributes.audioUrl);
//                                    pageAttributes.add(PageAttributes.content);
//                                    final int finalNumberOfPagesTryLoading = numberOfPagesTryLoading;
//                                    wikipedia.getWikipage(pageName,
//                                            pageAttributes,
//                                            wikipage,
//                                            new WorkerListener() {
//                                                @Override
//                                                public void onSuccess() {
//                                                    Toast
//                                                            .makeText(activity,
//                                                                    "loaded" + playableList.size(),
//                                                                    Toast.LENGTH_SHORT);
//                                                    wikipage.setTitle(pageName);
//                                                    playableList.add(wikipage);
//                                                    if(finalNumberOfPagesTryLoading == results.size()) {
//                                                        Toast
//                                                                .makeText(activity,
//                                                                        "all results began loading" + pageName,
//                                                                        Toast.LENGTH_SHORT);
//                                                        // all result were fully loaded.
//                                                        mediaPlayerFragment.updatePlayList(playableList, false);
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void onFailure() {
//                                                    Toast
//                                                            .makeText(activity,
//                                                                    "something went wrong with loading" + pageName,
//                                                                    Toast.LENGTH_SHORT)
//                                                            .show();
//
//                                                }
//                                            });
//
//                                }
//                            }
//
//                            @Override
//                            public void onFailure() {
//
//                            }
//                        }
//                );
//            }
//        }).start();
//
//
//    }
    @Override
    public void onBackPressed() {

        if(searchResultFragment != null &&
                searchResultFragment.getView() != null &&
                    searchResultFragment.getView().getVisibility() == VISIBLE)
        {
            searchResultVisibility(false, searchResultFragment);
        }
        else
        {
            super.onBackPressed();
        }
    }

    private boolean categoryInPlaylists(String category) {
        for(int i = 0; i < PlaylistsHandler.getPlaylists().size(); ++i)
        {
            if(PlaylistsHandler.getPlaylists().get(i).getTitle().equals(category))
            {
                return true;
            }
        }
        return false; // category was not found.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_CATEGORY_TAG) {
            if(resultCode == Activity.RESULT_OK){
                boolean dataSaved = data.getBooleanExtra("dataSaved", false);
                if(dataSaved)
                {
                    chosenCategories = ((WikiAudioApp) getApplication())
                            .getAppData().getChosenCategories();
                    this.loadPlaylists();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult
}
