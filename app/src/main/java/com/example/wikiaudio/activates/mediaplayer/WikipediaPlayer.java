package com.example.wikiaudio.activates.mediaplayer;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.example.wikiaudio.WikiAudioApp;
import com.example.wikiaudio.wikipedia.wikipage.Wikipage;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class WikipediaPlayer implements TextToSpeech.OnInitListener{


    private final Context context;
    private final ExecutorService threadPool;
    private Locale language;
    private final float speed;
    private boolean playingUrl = false;
    private boolean playingTextToSpeech = false;
    private MediaPlayer mp;
    private TextToSpeech engine;
    private String textToSpeak;
    private boolean paused = false;

    public WikipediaPlayer(Activity activity, Locale language, float speed) {
        this.context = activity.getApplicationContext();
        this.language = language;
        this.speed = speed;
        mp = new MediaPlayer();
        threadPool =((WikiAudioApp)activity.getApplication()).getExecutorService();

    }

    /**
     * plays the given wiki
     * @param wikipage
     */
    public void playWiki(Wikipage wikipage)
    {
        if(playingTextToSpeech || playingUrl) {
            // already playing
            stopPlaying();
        }
        if(canPlayWikipageAudio(wikipage)) {
            // has audio source;
            threadPool.execute(() -> { // preparing media player + loading url = long time -> thread
                Log.d("start playing", "from url source");
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
        else
        {
            threadPool.execute(() -> {
                Log.d("start playing", "from text to speech");
                playingTextToSpeech = true;
                String fullText = wikipage.getFullText();
                textToSpeak = fullText;
                engine = new TextToSpeech(context, this);
            });

        }
    }

    private boolean canPlayWikipageAudio(Wikipage wikipage) {
        return wikipage != null && wikipage.getAudioUrl() != null && !wikipage.getAudioUrl().equals("");
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
            playingTextToSpeech =false;
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
        if(paused)
        {
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
            //Setting speech Language
            engine.setLanguage(language);
            engine.setPitch(1);
            engine.setSpeechRate(speed);
            if(textToSpeak != null)
            {
                speakText(textToSpeak);
            }
            else
            {
                engine.speak("text to speech error.",
                        TextToSpeech.QUEUE_FLUSH,
                        null, null);
            }
        }
    }

    /**
     * 'specks' the given text.
     * (default engine can only handle small blocks of text.)
     * @param textToSpeak the text to speak.
     */
    private void speakText(String textToSpeak) {
        for (int start = 0; start < textToSpeak.length(); start += 200) {
            String curBlockText = textToSpeak.substring(start, Math.min(textToSpeak.length(), start + 200));
            engine.speak(curBlockText,
                    TextToSpeech.QUEUE_ADD,
                    null, null);
        }
    }

}
