package com.example.wikiaudio.wikipedia;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;

public class WikipediaPlayer implements TextToSpeech.OnInitListener{


    private final Context context;
    private Locale language;
    private final float speed;
    private boolean playingUrl = false;
    private boolean playingTextToSpeach = false;
    private MediaPlayer mp;
    private TextToSpeech engine;
    private String textToSpeak;
    private boolean paused = false;

    public WikipediaPlayer(Context context, Locale language, float speed) {
        this.context = context;
        this.language = language;
        this.speed = speed;
        mp = new MediaPlayer();
    }

    /**
     * plays the given wiki
     * @param wikiPage
     */
    public void playWiki(Wikipage wikiPage)
    {
        if(playingTextToSpeach || playingUrl) {
            // already playing
            stopPlaying();
        }
        if(wikiPage.getAudioUrl() != null && !wikiPage.getAudioUrl().equals("")) {
            // has audio source;
            Log.d("start playing", "from url source");
            playingUrl = true;
            try {
                    String audioUrl = wikiPage.getAudioUrl();
                    mp.setDataSource(audioUrl);
                    mp.prepare();
                    mp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Log.d("start playing", "from text to speech");
            playingTextToSpeach = true;
            playingTextToSpeach = true;
            String fullText = wikiPage.getFullText();
            textToSpeak = fullText;
            engine = new TextToSpeech(context, this);
        }
    }

    /**
     * stops the playing.
     */
    public void stopPlaying() {
        if (playingUrl) {
            mp.stop();
        }
        if (playingTextToSpeach) {
            engine.stop();
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
        if (playingTextToSpeach) {
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
