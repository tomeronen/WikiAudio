package com.example.wikiaudio.audio_recoder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class VoiceRecorder
{
    static boolean record = true;
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
    private int audioFormat = AudioFormat.ENCODING_DEFAULT;


    private short[] audioBuffer;
    private DataOutputStream dos;
    private int bufferSizeInBytes;
    private AudioRecord audioRecord;
    private UUID workId = null;
    private FileOutputStream fileOutputStream;
    private String fileOutPutPath;
    private Audio_Record a;
    private Context context;

    public VoiceRecorder(String fileOutPutPath, Context context) {
        this.context = context;
        try {
            a = new Audio_Record(fileOutPutPath, context);
            this.fileOutPutPath = fileOutPutPath;
            this.fileOutputStream = new FileOutputStream(fileOutPutPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void startRecording()
    {
        //
        a.startRecording();

        //
//
//
//        record = true;
//        WorkRequest recordReq =
//                new OneTimeWorkRequest
//                        .Builder(RecorderWorker.class)
//                        .setInputData(new Data.Builder()
//                        .putString("filePath", fileOutPutPath).build())
//                        .build();
//        WorkManager.getInstance().enqueue(recordReq);
//        workId = recordReq.getId();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }).toString();
    }


    public void stopRecording() throws IOException {
        if(a != null)
        {
            a.stopRecording();
        }
        record = false;
//        if(workId != null)
//        {
//            Log.d("workId", workId.toString());
//            WorkManager.getInstance().cancelWorkById(workId);
//            workId = null;
//        }
    }

    public int pauseRecording()
    {
        return 1;
    }

}
