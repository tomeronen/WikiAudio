package com.wikiaudioapp.wikiaudio.activates.settings_activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.wikiaudioapp.wikiaudio.R;
import com.wikiaudioapp.wikiaudio.WikiAudioApp;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.MediaPlayer;
import com.wikiaudioapp.wikiaudio.activates.mediaplayer.ui.MediaPlayerFragment;
import com.wikiaudioapp.wikiaudio.data.AppData;
import com.wikiaudioapp.wikiaudio.data.Holder;

public class SettingsActivity extends AppCompatActivity {

    private MediaPlayerFragment mediaPlayerFragment;
    private MediaPlayer mediaPlayer;
    private Activity activity;
    private AppData appData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        activity = this;
        appData =((WikiAudioApp) getApplication()).getAppData();
        initMediaPlayer();

    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference myPref = (Preference) findPreference("privacy_policy");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    //open browser or intent here
                    Intent privacyPolicyIntent = new Intent(getActivity(), PrivacyPolicy.class);
                    startActivity(privacyPolicyIntent);
                    return true;
                }
            });
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
}