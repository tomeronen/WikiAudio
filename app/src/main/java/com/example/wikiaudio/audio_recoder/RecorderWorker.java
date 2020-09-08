package com.example.wikiaudio.audio_recoder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
public class RecorderWorker extends Worker {

    int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;

    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we
    // use only 1024
    int BytesPerElement = 2; // 2 bytes in 16bit format

    private void PlayShortAudioFileViaAudioTrack(String filePath) throws IOException
    {
        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath==null)
            return;

        //Reading the file..
        File file = new File(filePath); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
        byte[] byteData = new byte[(int) file.length()];
        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            in.read( byteData );
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Set and push to audio track..
        int intSize = android.media.AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING, intSize, AudioTrack.MODE_STREAM);
        if (at!=null) {
            at.play();
            // Write the byte array to the track
            at.write(byteData, 0, byteData.length);
            at.stop();
            at.release();
        }
    }




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

    private byte[] audioBuffer;
    private DataOutputStream dos;
    private int bufferSizeInBytes;
    private AudioRecord audioRecord;
    String filePath;

    public RecorderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

        super(context, workerParams);
        //        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
//                channelConfig,
//                ~ audioFormat);
        FileOutputStream fileOutputStream = null;
        try {
            filePath = getInputData().getString("filePath");
            fileOutputStream = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING);
        audioRecord = new AudioRecord.Builder()
//                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
//                .setAudioSource(MediaRecorder.AudioSource.MIC)
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
//                        .setSampleRate(RECORDER_SAMPLERATE)
//                        .setChannelMask(RECORDER_CHANNELS_IN)
//                        .build())
//                .setBufferSizeInBytes(bufferSizeInBytes)
//                .setBufferSizeInBytes(BufferElements2Rec * BytesPerElement)
                .build();

//        audioRecord = new AudioRecord(audioSource,
//                sampleRateInHz,
//                channelConfig,
//                ~ audioFormat,
//                bufferSizeInBytes);

        audioBuffer = new byte[bufferSizeInBytes];
        // wrap output stream in buffered output stream for performance:
        BufferedOutputStream bOutputStream = new BufferedOutputStream(fileOutputStream);
        this.dos = new DataOutputStream(bOutputStream); // TODO -- why?
    }

    @NonNull
    @Override
    public Result doWork() {
        short sData[] = new short[BufferElements2Rec];
        try {
            int bufferReadResult = 0;
            audioRecord.startRecording();
            // read recorded values in to audioBuffer then to dos, until we do not have no more.
                while (VoiceRecorder.record)
                {
                // read values to buffer
//                bufferReadResult = audioRecord.read(audioBuffer,
//                        0, bufferSizeInBytes);


//                dos.write(audioBuffer);
//                Log.d("bufferReadResult", Integer.toString(bufferReadResult));
                // read values to output stream

//                for (int i = 0; i < bufferReadResult; i++) {
//                    dos.writeShort(audioBuffer[i]);
//                }


                int temp = audioRecord.read(sData, 0, BufferElements2Rec);
                byte bData[] = short2byte(sData);
                dos.write(bData, 0, BufferElements2Rec * BytesPerElement);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
//            int bufferReadResult = audioRecord.read(audioBuffer,
//                    0, bufferSizeInBytes/4);
//            while (bufferReadResult > 0 && VoiceRecorder.record) {
//                // read values to buffer
//                bufferReadResult = audioRecord.read(audioBuffer,
//                        0, bufferSizeInBytes / 4);
//                Log.d("bufferReadResult", Integer.toString(bufferReadResult));
//                // read values to output stream
//                for (int i = 0; i < bufferReadResult; i++) {
//                    dos.write(audioBuffer[i]);
//                }
//            }
            this.audioRecord.stop();
            this.audioRecord.release();
            this.dos.close();

//            PlayShortAudioFileViaAudioTrack(filePath);
//            MediaPlayer mp = new MediaPlayer();
//            Log.d("file path:", filePath);
//            mp.setDataSource(filePath);
//            mp.prepare();
//            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("recording status", "stooped");
        return Result.success();
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

}
