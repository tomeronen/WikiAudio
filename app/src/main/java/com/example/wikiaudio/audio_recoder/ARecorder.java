package com.example.wikiaudio.audio_recoder;

import android.media.AudioFormat;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class ARecorder {


    public String format= "wav";
    private Recorder recorder;

    public void startRecording(String fileName) {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override
                    public void onAudioChunkPulled(AudioChunk audioChunk) {
                    }
                }), new File(fileName));
        recorder.startRecording();
    }

    public void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stopRecording();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void pauseRecording() {

        if (recorder != null) {
                recorder.pauseRecording();
        }
    }


    private PullableSource mic() {
        return new PullableSource.NoiseSuppressor(
                new PullableSource.Default(
                        new AudioRecordConfig.Default(
                                MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                                AudioFormat.CHANNEL_IN_MONO, 44100
                        )
                )
        );
    }
}

