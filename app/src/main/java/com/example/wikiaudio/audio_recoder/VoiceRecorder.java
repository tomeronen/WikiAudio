package com.example.wikiaudio.audio_recoder;

import android.content.ContentValues;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.provider.MediaStore;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VoiceRecorder
{
    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int sampleRateInHz = 11025;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;


    /*
    Audio Format:
          (1) AudioFormat.ENCODING_DEFAULT
          (2) AudioFormat.ENCODING_INVALID
          (3) AudioFormat.ENCODING_PCM_16BIT
          (4) AudioFormat.ENCODING_PCM_8 BIT
    Among these four, our choices boil down to PCM 16-bit and PCM 8-bit.
    PCM stands for Pulse Code Modulation, which is essentially the raw audio samples.
    We can therefore set the resolution of each sample to be 16 bits or 8 bits.
    Sixteen bits will take up more space and processing power, while the representation of
    the audio will be closer to reality.
    */
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                                                                channelConfig,
                                                                ~ audioFormat);

    private AudioRecord audioRecord = new AudioRecord(audioSource,
                                                sampleRateInHz,
                                                channelConfig,
                                                ~ audioFormat,
                                                bufferSizeInBytes);

    private DataOutputStream dos;
    private short[] audioBuffer = new short[bufferSizeInBytes/4];

    public VoiceRecorder(FileOutputStream fileOutputStream) {

        // wrap output stream in buffered output stream for performance:
        BufferedOutputStream bOutputStream = new BufferedOutputStream(fileOutputStream);
         this.dos = new DataOutputStream(bOutputStream); // TODO -- why?

    }

    public int startRecording()
    {

        // Todo -- there is still some work here.
        //  (1) make asyncronized
        //  (2) make a stop function.
        //  (3) test.

        try {


            int bufferReadResult;
            audioRecord.startRecording();

            // read recorded values in to audioBuffer then to dos, until we do not have no more.
            do {

                // read values to buffer
                bufferReadResult = audioRecord.read(audioBuffer,
                        0, bufferSizeInBytes/4);

                // read values to output stream
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(audioBuffer[i]);
                }
            }
            while (bufferReadResult > 0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public int stopRecording()
    {
        try {
            this.audioRecord.stop();
            this.dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 1;
    }


}
