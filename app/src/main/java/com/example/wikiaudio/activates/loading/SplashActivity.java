package com.example.wikiaudio.activates.loading;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.MainActivity;
import com.example.wikiaudio.data.AppData;
import com.example.wikiaudio.data.Holder;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * A welcome screen for opening the app
 */
public class SplashActivity extends AppCompatActivity {
    private static final int DELAY_IN_MILISEC = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppData appData = ((WikiAudioApp) getApplication()).getAppData();

        Holder.getInstance(this, appData);
        List<String> chosenCategories = ((WikiAudioApp) getApplication())
                .getAppData().getChosenCategories();

        // try to initialize location based playlist.
        ((WikiAudioApp) getApplication()).getExecutorService().execute(() -> {
            LatLng currentLatLng = Holder.locationHandler.getCurrentLocation();
            if (currentLatLng != null) {
                Holder.playlistsManager.createLocationBasedPlaylist(
                        currentLatLng.latitude, currentLatLng.longitude, true);
            }
        });

        // start loading categories playlists in splash screen
        new Thread(()
                -> Holder.playlistsManager.createCategoryBasedPlaylists(chosenCategories)).start();

        new Handler().postDelayed(() -> {
            Intent main = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(main);
            finish();
        }, DELAY_IN_MILISEC);
    }
}