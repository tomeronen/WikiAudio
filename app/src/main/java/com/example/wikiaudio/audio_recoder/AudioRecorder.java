package com.example.wikiaudio.audio_recoder;

import android.content.ContentValues;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.provider.MediaStore;

import java.io.IOException;

public class AudioRecorder
{
    public MediaRecorder recorder;
    String filePath;

    public AudioRecorder(Context activity, MediaRecorder mr, String fileName)
    {
            this.recorder = mr;
            this.recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            this.recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            this.recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            String a =  activity.getFilesDir() + "/" + fileName;
            this.recorder.setOutputFile( a);
    }

    public void startRecording()
    {
        this.recorder.start();   // Recording is now started
    }

    public void pauseRecording()
    {

    }

    public void stopRecording()
    {
        this.recorder.stop();
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(filePath);
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
