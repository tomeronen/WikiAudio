package com.example.wikiaudio.activates.mediaplayer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.activates.playlist.Playlist;
import com.example.wikiaudio.data.Holder;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class PlaylistPlayer implements TextToSpeech.OnInitListener{

    private static final String TAG = "PlaylistPlayer";
    private static final Locale ENGLISH = Locale.ENGLISH;
    private static final float READING_SPEED = 1f;
    private static final float VOICE_PITCH = 1;

    private final Context context;
    private AppCompatActivity activity;
    private Playlist playlist = null;
    private int index = -1;

    private final ExecutorService threadPool;
    private MediaPlayer mp;
    private TextToSpeech ttsEngine;
    private int textBlocksLeft = 0;

    private boolean playingUrl = false;
    private boolean playingTextToSpeech = false;

    private boolean paused = false;
    private String textToSpeak;
    private boolean isEngineReady = false;

    public PlaylistPlayer(AppCompatActivity activity) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        ttsEngine = new TextToSpeech(context, this);
        threadPool =((WikiAudioApp)activity.getApplication()).getExecutorService();
    }

    /**
     * Use this function for playing a wikipage or a playlist. We always need to select from which
     * index to start playing the playlist.
     * //todo perhaps void is better than returning a bool
     */
    public boolean playPlaylistFromIndex(Playlist playlist, int index) {
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
                        .build()
                );

                // Called when the end of a media source is reached during playback.
                // we want to release resources and play the next wikipage in the playlist
                mp.setOnCompletionListener(mp -> {
                    if (mp != null) {
                        mp.release();
                        mp = null;
                        playNext();
                    }
                });

                try {
                    mp.setDataSource(audioUrl);
                    mp.prepare(); // this one takes time
                    mp.start();
                    playingUrl = true;
                } catch (IOException | IllegalStateException e) {
                    // IllegalStateException happens when play button is pressed fast.
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
     * resumes playing after paused.
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

    /**
     * The java.lang.Object.finalize() is called by the garbage collector on an object when
     * garbage collection determines that there are no more references to the object. A subclass
     * overrides the finalize method to dispose of system resources or to perform other cleanup.
     *
     * We want to make sure we remove the MediaPlayer and the ttsEngine.
     */
    protected void finalize() {
        Log.d(TAG, "finalize: am I really being destroyed?");
        if (ttsEngine != null) {
            ttsEngine.stop();
            ttsEngine.shutdown();
        }
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }

    public void setPlaylist(Playlist playlist) {
        this.playlist = playlist;
    }

    public Playlist getPlaylist() {
        return this.playlist;
    }

}
