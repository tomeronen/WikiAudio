package com.example.wikiaudio.audio_recoder;

import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

//TODO WHAT IS THE BEST AUDIO FORMAT?

public class VoiceRecorderOptionB {
    private MediaRecorder recorder;
    public String format;

    public VoiceRecorderOptionB() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            format = "ogg";
        }
        else
        {
            format = "3gp";
        }
    }



    public void startRecording(String fileName) {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            recorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
            format = "ogg";
        }
        else
        {
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            format = "3gp";
        }
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("", "prepare() failed");
        }
        recorder.start();
    }

    public void stopRecording() {
        if(recorder != null)
        {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private void pauseRecording() {
        recorder.pause();
    }

}
