package com.wikiaudioapp.wikiaudio.activates;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.wikiaudioapp.wikiaudio.R;
import com.wikiaudioapp.wikiaudio.WikiAudioApp;
import com.wikiaudioapp.wikiaudio.activates.choose_categories.ChooseCategoriesActivity;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;
import com.wikiaudioapp.wikiaudio.activates.playlist.PlaylistsManager;
import com.wikiaudioapp.wikiaudio.activates.playlist.playlist_ui.PlaylistFragment;
import com.wikiaudioapp.wikiaudio.activates.playlist.playlist_ui.PlaylistsFragmentAdapter;
import com.wikiaudioapp.wikiaudio.activates.record_page.SimpleAlertDialogFragment;
import com.wikiaudioapp.wikiaudio.data.AppData;
import com.wikiaudioapp.wikiaudio.data.Holder;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.tabs.TabLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.wikiaudioapp.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment.CHOOSE_CATEGORY_TAG;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    //For logs
    private static final String TAG = "MainActivity";

    //Vars
    private AppCompatActivity activity;
    private AppData appData;

    //Location
    private static final int ERROR_DIALOG_REQUEST = 9002;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionGranted = false;
    private Boolean isGPSEnabled = false;
    private Boolean mapWasInit= false;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    //Playlist
    private PlaylistsFragmentAdapter playListsFragmentAdapter;

    //MediaPlayer
    private MediaPlayerFragment mediaPlayerFragment;
    private MediaPlayer mediaPlayer;

    //Categories
    private List<String> chosenCategories;

    //Views
    private TabLayout tabLayout;
    private ProgressBar loadingIcon;
    private ImageButton chooseCategoriesButton;
    private ViewPager viewPager;
    private TextView noPlaylistsText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showPrivacyPolicy();
        initVars();
        initMap();
        initMediaPlayer();
//        loadPlaylists();  todo  problem having this in onCreate and restoreCategories onResume
        setOnClickButtons();
    }

    private void showPrivacyPolicy() {
        SimpleAlertDialogFragment privacyPolicy = new SimpleAlertDialogFragment(
                "WikiAudio may use personal Data to make your experince in the app better.\n" +
                        "Location Data - used to provide location based wikipedia articles playlists.\n" +
                        "Micrphone Recording - used to let you record Wikipedia articles and upload them to wikipedia servers if you desire. \n" +
                        "By using WikiAudio App you agree to our  Privacy Policy as shown in" +
                        "https://sites.google.com/view/wikiaudio/home", "I agree to WikiAudio privacy " +
                "policy.", "I dissagree to WikiAudio privacy policy",
                (DialogInterface.OnClickListener) (dialog, which) -> {
                    // what to do if agrees

                },
                (DialogInterface.OnClickListener) (dialog, which) -> {
                    // what to do if disagrees


                },  "Privacy Policy", getDrawable(R.drawable.privacy_policy_icon));
        FragmentManager fragmentManager = getSupportFragmentManager();
        privacyPolicy.show(fragmentManager, "privacy policy");

    }

    @Override
    public void onResume() {
        super.onResume();
        restoreCategories();
        initMediaPlayer();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CHOOSE_CATEGORY_TAG) {
                if (resultCode == RESULT_OK) {
                    //Categories changed
                    Log.d(TAG, "onActivityResult: for CHOOSE_CATEGORY_TAG, got RESULT_OK");
                    chosenCategories = ((WikiAudioApp) getApplication())
                            .getAppData().getChosenCategories();
                    //Update category based playlists & update their display (=tabLayout)
                    Holder.playlistsManager.updateCategoryBasedPlaylists(chosenCategories);
                    new Thread(this::displayPlaylists).start();
                } else {
                    //Categories unchanged
                    Log.d(TAG, "onActivityResult: for CHOOSE_CATEGORY_TAG, got RESULT_CANCELED");
                }
            }
        } catch (Exception ex) {
            Log.d(TAG, "onActivityResult: got an exception - " + ex.toString());
        }
    }

    /**
     * Pretty self-explanatory, really.
     */
    private void initVars() {
        activity = this;
        appData =((WikiAudioApp) getApplication()).getAppData();
        //Init && holds all of the app's facades/singletons. Can't be init at WikiAudioApp because
        //it needs an activity
        Holder.getInstance(activity, appData);
        chosenCategories = ((WikiAudioApp) getApplication()).getAppData().getChosenCategories();

        //Check for location perms
        mLocationPermissionGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                        == PackageManager.PERMISSION_GRANTED;
        //Views
        chooseCategoriesButton = findViewById(R.id.addCategoryButton);
        loadingIcon = findViewById(R.id.progressBar4);
        loadingIcon.setVisibility(View.GONE);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);
        noPlaylistsText = findViewById(R.id.noPlaylistsText);
        noPlaylistsText.setVisibility(View.GONE);
        playListsFragmentAdapter = new PlaylistsFragmentAdapter(getSupportFragmentManager());
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

    /**
     * Sets the chooseCategoriesButton listener
     */
    private void setOnClickButtons() {
        chooseCategoriesButton.setOnClickListener(v -> {
            Intent chooseCategories = new Intent(this, ChooseCategoriesActivity.class);
            startActivityForResult(chooseCategories, CHOOSE_CATEGORY_TAG);
        });
    }

    /**
     * Creates category based playlists and displays all playlists
     */
    private void loadPlaylists() {
        if(PlaylistsManager.getPlaylists().size() != chosenCategories.size())
        {
            // we still have things to load;
            // loadingIcon.setVisibility(View.VISIBLE);
            //todo need to fix. right now brings more problems then benefit
        }
        else
        {
            // All data was loaded in splash;
            loadingIcon.setVisibility(View.GONE);
        }
        new Thread(() -> {
            //todo-sm comment below when checking splash upload
            Holder.playlistsManager.createCategoryBasedPlaylists(chosenCategories);
            displayPlaylists();
        }).start();
    }

    /**
     * Loads the TabLayout using all playlist fragments.
     */
    private void displayPlaylists() {
        //Add all relevant playlists as fragments to the adapter
        playListsFragmentAdapter = new PlaylistsFragmentAdapter(getSupportFragmentManager());
        playListsFragmentAdapter.updatePlaylistFragmentList();
        playListsFragmentAdapter.notifyDataSetChanged();

        //Get currently played playlist's index to select it
        int selectedIndex = -1;
        if (mediaPlayer != null && mediaPlayer.getCurrentPlaylist() != null) {
            Playlist currentPlaylist = mediaPlayer.getCurrentPlaylist();
            selectedIndex = Holder.playlistsManager.getIndexByPlaylist(currentPlaylist);
        }

        //Display the tabLayout
        int finalSelectedIndex = selectedIndex;
        activity.runOnUiThread(() -> {
            try {
                // todo when we enable gps, when we have other playlist, makes the app to crash.
                //  dont know why
                viewPager.setAdapter(playListsFragmentAdapter);
            }
            catch (IndexOutOfBoundsException e)
            {
                e.printStackTrace();

                // bad solution but better then crashing. restart app.
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }

            tabLayout.setupWithViewPager(viewPager);
            playListsFragmentAdapter.setTabLayout(tabLayout);
            int counter = 0;
            for (Playlist playlist: PlaylistsManager.getPlaylists()) {
                if(counter >= tabLayout.getTabCount()){
                    break;
                }
                Objects.requireNonNull(tabLayout.getTabAt(counter)).setText(playlist.getTitle());
                if (counter == finalSelectedIndex) {
                    Objects.requireNonNull(tabLayout.getTabAt(counter)).select();
                }
                counter++;
            }

            //When first playlist fragment has data stop loading icon.
            if(playListsFragmentAdapter.getCount() > 0) {
                playListsFragmentAdapter.getItem(0).
                        wikipagePlayListRecyclerViewAdapter.
                        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                            @Override
                            public void onChanged() {
                                super.onChanged();
                                loadingIcon.setVisibility(View.GONE);
                            }
                        });
            }


            if(tabLayout.getTabCount() == 0) // no playlists
            {
                noPlaylistsText.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Adds (=displays) a matching tab for the given PlaylistFragment
     */
    private void addTab(PlaylistFragment playlistFragment) {
        Log.d(TAG, "addTab: got here");
        if (tabLayout != null && playlistFragment != null &&
                playlistFragment.getPlaylist() != null) {
            tabLayout.addTab(tabLayout.newTab().setText(playlistFragment.getPlaylist().getTitle()));
            playListsFragmentAdapter.addPlaylistFragment(playlistFragment);
            playListsFragmentAdapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "addTab: got null tabLayout/playlistFragment/playlist");
        }
    }

    /**
     * Creates the media player + navigation bar at the bottom.
     */
    private void initMediaPlayer() {
        mediaPlayerFragment = (MediaPlayerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mediaPlayerFragment);
        if (mediaPlayerFragment == null) {
            mediaPlayerFragment = new MediaPlayerFragment();
            mediaPlayerFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().replace(R.id.mediaPlayerFragment,
                    mediaPlayerFragment, "mediaPlayerFragment").commit();
        }
        mediaPlayer = new MediaPlayer(activity, appData, mediaPlayerFragment);
        mediaPlayerFragment.setAudioPlayer(mediaPlayer);
        Holder.playlistsManager.setMediaPlayer(mediaPlayer);
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
        mLocationPermissionGranted = false;
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
            //If there's something being played:
            // mark the playlist and zoom in on the wikipage being played
            if (mediaPlayer != null && mediaPlayer.getIsPlaying()) {
                Playlist currentPlaylist = mediaPlayer.getCurrentPlaylist();
                if (currentPlaylist != null) {
                    Holder.locationHandler.markPlaylist(currentPlaylist);
                    Holder.locationHandler.markAndZoom(mediaPlayer.getCurrentWikipage());
                } else {
                    Log.d(TAG, "initUserLocationOnTheMap: got null currentPlaylist");
                }
            } else {
                zoomInOnUserAndCreateNearbyPlaylist();
            }
        } else {
            Log.d(TAG, "initUserLocationOnTheMap: either google map is null or we have no perm");
        }
    }

    /**
     * If we have the permissions & user's location, we zoom in on the user and create + display
     * the Nearby playlist
     */
    private void zoomInOnUserAndCreateNearbyPlaylist() {
        if (isGPSEnabled && Holder.playlistsManager.getNearby() == null) {
            // we don't have nearby playlist. we create one here.
            LatLng currentLatLng = Holder.locationHandler.getCurrentLocation();
            if (currentLatLng != null) {
                addLocationPlaylist(currentLatLng);
            }
            else{
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    LatLng currentLocation = Holder.locationHandler.getCurrentLocation();
                    if (currentLocation != null) {
                        addLocationPlaylist(currentLatLng);
                    }
                }, 5000); // try again after some time
            }
        }
        else if(isGPSEnabled && Holder.playlistsManager.getNearby() != null )
        {
            // we have a nearby playlist just need to zoom map on it.
            LatLng currentLatLng = new LatLng(Holder.playlistsManager.getNearby().getLat(),
                    Holder.playlistsManager.getNearby().getLon());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        }
        else {
            // no GPS.
            Log.d(TAG, "initUserLocationOnTheMap: no GPS");
        }
    }

    private void addLocationPlaylist(LatLng currentLatLng) {
        //Zoom in to user's location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));

        //Create (and display on the map) the nearby playlist
        Holder.playlistsManager.createLocationBasedPlaylist(
                currentLatLng.latitude, currentLatLng.longitude, true);

        //Update the tabs after creating it
        if(!tabsContainsNearBy())
        {
            try {
                addTab(Holder.playlistsManager.getNearby().getPlaylistFragment());
            }
            catch (Exception e)
            {
                Log.d("error:", "could not load tab");
            }
        }

    }

    private boolean tabsContainsNearBy() {
        boolean containsNearBy = false;
        for(int i = 0; i < tabLayout.getTabCount(); ++i)
        {
            if(tabLayout.getTabAt(i).getText().equals(Holder.playlistsManager.getNearby().getTitle()))
            {
                containsNearBy = true;
                break;
            }
        }
        return containsNearBy;
    }

    /**
     * If required, creates new chosen categories and removes old ones + updates the display
     */
    private void restoreCategories() {
        if (chosenCategoriesChanged()) {
            // categories changed
            Log.d(TAG, "onResume: categories changed");
            chosenCategories = ((WikiAudioApp) getApplication())
                    .getAppData().getChosenCategories();
            //Update category based playlists & update their display (=tabLayout)
            Holder.playlistsManager.updateCategoryBasedPlaylists(chosenCategories);
            new Thread(this::displayPlaylists).start();
        }
    }

    private boolean chosenCategoriesChanged() {
        Set<String> oldChosenCategories = new HashSet<>();
        int tabsAmount = tabLayout.getTabCount();
        if(chosenCategories.size() != (tabLayout.getTabCount() - 1))
        {
            return true;
        }
        for(int i = 0; i < tabsAmount; ++i)
        {
            oldChosenCategories.add(String.valueOf(tabLayout.getTabAt(i).getText()));
        }
        return !oldChosenCategories
                .equals(new HashSet<>(chosenCategories)); // order does not matter
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
        PlaylistsManager.displayNearbyPlaylistOnTheMap();
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

}