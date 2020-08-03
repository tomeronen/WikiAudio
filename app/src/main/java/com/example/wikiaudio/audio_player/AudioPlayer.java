package com.example.wikiaudio.audio_player;

import android.media.MediaPlayer;

import java.io.IOException;

public class AudioPlayer{

    MediaPlayer mp;
    private static AudioPlayer instance;

    public AudioPlayer() {
        this.mp = new MediaPlayer();
        mp.setLooping(false);
    }

    public void playAudio(String dataSource)
    {
        try {
            this.mp.setDataSource(dataSource);
            this.mp.prepareAsync();
            this.mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekTo(int msec)
    {
        mp.seekTo(msec);
    }

    public void pause(int msec)
    {
        mp.pause();
    }

    public void stop()
    {
        mp.stop();
    }

    public void getDuration()
    {
        mp.getDuration();
    }
}
