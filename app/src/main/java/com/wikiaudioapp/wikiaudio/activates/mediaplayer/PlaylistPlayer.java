package com.wikiaudioapp.wikiaudio.activates.mediaplayer;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaTimestamp;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.wikiaudioapp.wikiaudio.WikiAudioApp;
import com.wikiaudioapp.wikiaudio.activates.playlist.Playlist;
import com.wikiaudioapp.wikiaudio.data.Holder;
import com.wikiaudioapp.wikiaudio.wikipedia.wikipage.Wikipage;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

/**
 * The wrapper of Google's MediaPlayer & TextToSpeech for actual playing playlists
 */
public class PlaylistPlayer implements TextToSpeech.OnInitListener{
    //For logs
    private static final String TAG = "PlaylistPlayer";

    //Constant Vars
    private static final Locale ENGLISH = Locale.ENGLISH;
    private static final float READING_SPEED = 1f;
    private static final float VOICE_PITCH = 1;

    //Vars
    private AppCompatActivity activity;
    private final ExecutorService threadPool;

    //Playlist Vars
    private Playlist playlist = null;
    private int index = -1;
    private boolean paused = false;

    //MediaPlayer Vars
    private MediaPlayer mp;
    private boolean playingUrl = false;

    //TextToSpeech Vars
    private TextToSpeech ttsEngine;
    private boolean playingTextToSpeech = false;
    private boolean isEngineReady = false;
    private String textToSpeak;
    private int textBlocksLeft = 0;
    private float audioSpeed = 1.0f;


    public PlaylistPlayer(AppCompatActivity activity) {
        this.activity = activity;
        ttsEngine = new TextToSpeech(activity.getApplicationContext(), this);
        threadPool =((WikiAudioApp)activity.getApplication()).getExecutorService();
        try{
            audioSpeed = Float.parseFloat(PreferenceManager
                    .getDefaultSharedPreferences(activity)
                    .getString("audio_speed", "1")); // update speed.
        }
        catch (Exception e)
        {
            audioSpeed = 1.0f;
        }
    }

    /**
     * Use this function for playing a wikipage or a playlist. We always need to select from which
     * index to start playing the playlist.
     */
    public boolean playPlaylistFromIndex(Playlist playlist, int index) {
        try{
            audioSpeed = Float.parseFloat(PreferenceManager
                    .getDefaultSharedPreferences(activity)
                    .getString("audio_speed", "1")); // update speed.
        }
        catch (Exception e)
        {
            audioSpeed = 1.0f;
        }
        if (!isValidPlaylistAndIndex(playlist, index)) {
            Log.d(TAG, "playPlaylist: null playlist or bad index");
            return false;
        }
        //if already playing something, stop
        if (playingTextToSpeech || playingUrl) {
            stopPlayer();
        }
        this.playlist = playlist;
        this.index = index;
        playWikipageByIndex();
        return true;
    }

    /**
     * Plays a wikipage given a playlist and an index
     * Checks whether it has audio URL, if yes: uses MediaPlayer; ow, uses TextToSpeech
     */
    private void playWikipageByIndex() {
        if (isValidPlaylistAndIndex(playlist, index)) {
            if (canPlayWikipageAudio(playlist.getWikipageByIndex(index))) {
                playWikipageByIndexWithAudioURL();
            } else {
                playWikipageByIndexWithTextToSpeech();
            }
        } else {
            Log.d(TAG, "playWikipageByIndex: null playlist or bad index");
        }
    }

    private void playWikipageByIndexWithAudioURL() {
        if (isValidPlaylistAndIndex(playlist, index)
                && canPlayWikipageAudio(playlist.getWikipageByIndex(index))) {
            String audioUrl = playlist.getWikipageByIndex(index).getAudioUrl();
            threadPool.execute(() -> {
                Log.d(TAG, "playWikipageByIndexWithAudioURL: playing wikipage from URL audio source");

                mp = new MediaPlayer();

                mp.setAudioAttributes(new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());


                // Called when the end of a media source is reached during playback.
                // we want to release resources and play the next wikipage on the playlist
                mp.setOnCompletionListener(mp -> {
                    if (mp != null) {
                        mp.release();
                        mp = null;
                        playNext();
                    }
                });

                try {
                    mp.setDataSource(audioUrl);
                    mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(audioSpeed));
                    MediaTimestamp timestamp = mp.getTimestamp();
                    mp.prepare(); // this one takes time
                    mp.start();
                    playingUrl = true;
                } catch (IOException | IllegalStateException e) {
                    e.printStackTrace();
                    playingUrl = false;
                }
            });
        } else {
            Log.d(TAG, "playWikipageByIndexWithAudioURL: null playlist, bad index or bad wikipage");
            playingUrl = false;
        }
    }

    private void playWikipageByIndexWithTextToSpeech() {
        if (isValidPlaylistAndIndex(playlist, index)
                && playlist.getWikipageByIndex(index).getFullText() != null
                && !playlist.getWikipageByIndex(index).getFullText().equals("")) {
            Log.d(TAG, "playWikipageByIndexWithTextToSpeech: playing from text to speech");
            threadPool.execute(() -> {
                textToSpeak = playlist.getWikipageByIndex(index).getFullText();
                speakOut();
                playingTextToSpeech = true;
            });
        } else {
            Log.d(TAG, "playWikipageByIndexWithTextToSpeech: null playlist, bad index or bad wikipage");
            playingTextToSpeech = false;
        }
    }

    /**
     * Used for when we finished playing the current wikipage and we want to play the next one
     * on the playlist
     */
    private void playNext() {
        //update visuals
        activity.runOnUiThread(() -> {
            if (Holder.playlistsManager != null &&
                    Holder.playlistsManager.getMediaPlayer() != null) {
                Holder.playlistsManager.getMediaPlayer().updateNextWikipage();
            } else {
                Log.d(TAG, "playNext: next wikipage is being " +
                        "played but getMediaPlayer() returns null so cannot update");
            }
        });
        index = index + 1;
        playWikipageByIndex();
    }


    /**
     * Stops what is playing and releases resources
     */
    public void stopPlayer() {
        if (playingUrl && mp != null) {
            mp.release();
            mp = null;
            playingUrl = false;
        }
        if (playingTextToSpeech) {
            ttsEngine.stop();
            playingTextToSpeech = false;
        }
    }

    /**
     * pauses the playing.
     */
    public void pausePlaying() {
        if (playingUrl && mp != null) {
            mp.pause();
            paused = true;
            return;
        }
        if (playingTextToSpeech && isEngineReady) {
            // There's no option to pause on TextToSpeech, so we have to stop
            ttsEngine.stop();
            paused = false;
        }
    }

    /**
     * Resumes playing after paused.
     */
    public void resumePlaying() {
        if(playingUrl && mp != null && paused) {
            Log.d(TAG, "resumePlaying: mediaplayer section");
            mp.start();
            paused = false;
            return;
        }
        if (playingTextToSpeech && isEngineReady) {
            // There's no option to pause on TextToSpeech, so we re-play :(
            Log.d(TAG, "resumePlaying: TextToSpeech section");
            playWikipageByIndexWithTextToSpeech();
            paused = false;
        }
        Log.d(TAG, "resumePlaying: ");
    }

    /**
     * A TextToSpeech instance can only be used to synthesize text once it has completed its
     * initialization. Implement the TextToSpeech.OnInitListener to be notified of the completion
     * of the initialization.
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (ttsEngine.setLanguage(ENGLISH) == TextToSpeech.SUCCESS &&
                    ttsEngine.setSpeechRate(READING_SPEED) == TextToSpeech.SUCCESS &&
                    ttsEngine.setPitch(VOICE_PITCH) == TextToSpeech.SUCCESS ) {
                isEngineReady = true;
            } else {
                isEngineReady = false;
                Log.d(TAG, "onInit: got an error when ");
            }
        } else {
            isEngineReady = false;
            Log.d(TAG, "onInit: TextToSpeech got bad status, initialization failed");
        }
    }

    /**
     * 'Speaks' the given text.
     * Default engine can only handle small blocks of text.
     */
    private void speakOut() {
        if (isEngineReady && textToSpeak != null && !textToSpeak.equals("")){
            textBlocksLeft = (textToSpeak.length() / 200) + 1; // java floors int division
            for (int start = 0; start < textToSpeak.length(); start += 200) {
                String curBlockText = textToSpeak.substring(start, Math.min(textToSpeak.length(),
                        start + 200));
                ttsEngine.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {}

                    @Override
                    public void onDone(String utteranceId) {
                        textBlocksLeft--;
                        if (textBlocksLeft == 0) {
                            Log.d(TAG, "onDone: textBlocksLeft == 0");
                            // the tts engine finished speaking all blocks
                            playNext();
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {}
                });
                if (TextToSpeech.ERROR ==
                        ttsEngine.speak(curBlockText, TextToSpeech.QUEUE_ADD, null, "UniqueID")) {
                    Log.d(TAG, "speakOut: got an error from ttsEngine.speak(), aborting");
                    break;
                }
            }
        } else {
            Log.d(TAG, "speakOut: TextToSpeech engine is not ready or bad input text");
        }
    }

    private boolean isValidPlaylistAndIndex(Playlist playlist, int index) {
        return (playlist != null && index > -1 && index < playlist.size());
    }

    private boolean canPlayWikipageAudio(Wikipage wikipage) {
        return wikipage != null && wikipage.getAudioUrl() != null && !wikipage.getAudioUrl().equals("");
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return this.playlist;
    }

}
