package com.example.wikiaudio.audio_recoder;

import android.media.MediaRecorder;

import java.io.IOException;

public class AudioRecorder
{
    static AudioRecorder singleton = null;
    MediaRecorder recorder;

    private AudioRecorder()
    {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        prepare();
    }

    public void prepare() {
        if(singleton != null)
        {
            try {
                this.recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOutPutFile(String pathName) {
        if(singleton != null)
        {
            this.recorder.setOutputFile(pathName);
        }
    }

    public AudioRecorder getAudioRecorder() {
        if(singleton != null)
        {
            return singleton;
        }
        else
        {
            singleton = new AudioRecorder();
            return singleton;
        }
    }

    static void startRecording()
    {
        singleton.recorder.start();   // Recording is now started
    }

    static void pauseRecording()
    {

    }

    static void stopRecording()
    {

    }
}
