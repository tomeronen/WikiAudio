package com.example.wikiaudio.activates.loading;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.Holder;
import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.MainActivity;

import java.util.List;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Init && holds all of the app's facades/singletons. Can't be init at WikiAudioApp because
        //it needs an activity
        Holder.getInstance(this);
        List<String> chosenCategories = ((WikiAudioApp) getApplication()).getAppData().getChosenCategories();

        // start loading categories playlists in splash screen // todo (S.M)
        new Thread(()
                -> Holder.playlistsManager.createCategoryBasedPlaylists(chosenCategories)).start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent main = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(main);
                finish();
            }
        }, 2500);
    }
}