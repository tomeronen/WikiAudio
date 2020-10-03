package com.example.wikiaudio.activates.loading;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.activates.MainActivity;

/**
 * A welcome screen for opening the app
 */
public class SplashActivity extends AppCompatActivity {
    private static final int DELAY_IN_MILISEC = 2500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //todo-sm: tried and didn't work
//        //Init && holds all of the app's facades/singletons. Can't be init at WikiAudioApp because
//        //it needs an activity
//        Holder.getInstance(this);
//        List<String> chosenCategories = ((WikiAudioApp) getApplication()).getAppData().getChosenCategories();

//        // start loading categories playlists in splash screen
//        new Thread(()
//                -> Holder.playlistsManager.createCategoryBasedPlaylists(chosenCategories)).start();

        new Handler().postDelayed(() -> {
            Intent main = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(main);
            finish();
        }, DELAY_IN_MILISEC);
    }
}