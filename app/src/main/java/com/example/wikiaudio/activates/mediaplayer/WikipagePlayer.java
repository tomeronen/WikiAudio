package com.example.wikiaudio.activates.mediaplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class WikipagePlayer implements TextToSpeech.OnInitListener{

    private static final String TAG = "WikipagePlayer";
    private static final Locale ENGLISH = Locale.ENGLISH;
    private static final float READING_SPEED = 1f;
    private static final float VOICE_PITCH = 1;

    private final Context context;

    private final ExecutorService threadPool;
    private MediaPlayer mp;
    private TextToSpeech engine;

    private boolean playingUrl = false;
    private boolean playingTextToSpeech = false;

    private boolean paused = false;
    private String textToSpeak;

    public WikipagePlayer(AppCompatActivity activity) {
        this.context = activity.getApplicationContext();
        threadPool =((WikiAudioApp)activity.getApplication()).getExecutorService();
        mp = new MediaPlayer();
    }

    /**
     * plays the given wikipage
     */
    public boolean playWikipage(Wikipage wikipage) {
        if (wikipage == null) {
            Log.d(TAG, "playWikipage: null wikipage");
            return false;
        }

        if (playingTextToSpeech || playingUrl) {
            // already playing something
            stopPlaying();
        }

        if (canPlayWikipageAudio(wikipage)) {
            playWikipageUsingAudio(wikipage);
            return true;
        } else {
            // we'll use the textToSpeak engine
            if (wikipage.getFullText().equals("")) {
                Log.d(TAG, "playWikipage: wikipage has no text, can't play an empty string :)");
                return false;
            } else {
                playWikipageUsingTextToSpeech(wikipage);
                return true;
            }
        }
    }

    private boolean canPlayWikipageAudio(Wikipage wikipage) {
        return wikipage != null && wikipage.getAudioUrl() != null && !wikipage.getAudioUrl().equals("");
    }

    private void playWikipageUsingAudio(Wikipage wikipage) {
        threadPool.execute(() -> {
            // preparing media player + loading url = long time -> thread
            Log.d(TAG, "playWikipageUsingAudio: playing wikipage from URL audio source");
            playingUrl = true;
            mp = new MediaPlayer();
            try {
                String audioUrl = wikipage.getAudioUrl();
                mp.setDataSource(audioUrl);
                mp.prepare();
                mp.start();
            } catch (IOException | IllegalStateException e) {
                // IllegalStateException happens when play button is pressed fast.
                e.printStackTrace();
            }
        });
    }

    private void playWikipageUsingTextToSpeech(Wikipage wikipage) {
        threadPool.execute(() -> {
            Log.d(TAG, "playWikipageUsingTextToSpeech: from text to speech");
            playingTextToSpeech = true;
            textToSpeak = wikipage.getFullText();
            engine = new TextToSpeech(context, this);
        });
    }

    /**
     * stops the playing.
     */
    public void stopPlaying() {
        if (playingUrl) {
            mp.stop();
            playingUrl = false;
        }
        if (playingTextToSpeech) {
            engine.stop();
            playingTextToSpeech = false;
        }
    }

    /**
     * pauses the playing.
     */
    public void pausePlaying() {
        if (playingUrl) {
            paused = true;
            mp.pause();
        }
        if (playingTextToSpeech) {
            engine.stop();
        }
    }

    /**
     * resumes playing after paused.
     */
    public void resumePlaying() {
        if(paused) {
            mp.start();
        }
    }


    /**
     * when the text to speech engine is ready. what do you do.
     * @param status
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            engine.setLanguage(ENGLISH);
            engine.setSpeechRate(READING_SPEED);
            engine.setPitch(VOICE_PITCH);
            if (textToSpeak != null) {
                speakText();
            } else {
                engine.speak("text to speech error.", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }

    /**
     * 'specks' the given text.
     * (default engine can only handle small blocks of text.)
     */
    private void speakText() {
        for (int start = 0; start < textToSpeak.length(); start += 200) {
            String curBlockText = textToSpeak.substring(start, Math.min(textToSpeak.length(),
                    start + 200));
            engine.speak(curBlockText, TextToSpeech.QUEUE_ADD, null, null);
        }
    }

}
