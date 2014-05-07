package com.pengrad.keezy.test;

import android.app.Activity;
import android.media.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: stas
 * Date: 05.05.14 19:24
 */

public class App extends Activity implements View.OnClickListener {

    boolean isRecording = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        Button buttonPlay = (Button) findViewById(R.id.play);
        Button buttonRec = (Button) findViewById(R.id.rec);
        Button buttonPool = (Button) findViewById(R.id.pool);
        Button buttonSave = (Button) findViewById(R.id.save);
        Button buttonPLay2 = (Button) findViewById(R.id.play2);

        TouchListener.Callback<Button> recordCallback = new TouchListener.Callback<Button>() {
            public void onTouchDown(Button view) {
                startRec();
            }

            public void onTouchUp(Button view) {
                stopRec();
            }
        };
        TouchListener.Callback<Button> playCallback = new TouchListener.Callback<Button>() {
            public void onTouchDown(Button view) {
//                startPlay();
                play();
            }
        };

        buttonRec.setOnTouchListener(new TouchListener<Button>(Button.class, recordCallback));
//        buttonPlay.setOnTouchListener(new TouchListener<Button>(Button.class, playCallback));
        buttonPool.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonPLay2.setOnClickListener(this);

        recordAudio = new RecordAudio();
    }

    RecordAudio recordAudio;
    AudioTrack audioTrack;

    public void startRec() {
//        Log.d("++++++", "start rec");
//        new RecordAudioMem().execute();
        new Thread(recordAudio).start();
    }

    byte[] res;
    int len;

    public void stopRec() {
//        Log.d("++++++", "stop rec");
//        isRecording = false;
        recordAudio.cancel();

//        len = recordAudio.readSum;
//        res = new byte[len];
//        int j = 0;
//        for (byte[] data : recordList)
//            for (int i = 0; i < data.length; i++, j++)
//                res[j] = data[i];
//
//        Log.d("++++", "res:" + recordList.size() + " len: " + len);
//        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfigurationOut, audioEncoding, len, AudioTrack.MODE_STATIC);
        recordAudio = new RecordAudio();
    }

    public void play() {
        Log.d("+++++++", audioTrack.getState() + "  -  " + audioTrack.getPlayState());
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.pause();
            audioTrack.flush();
            audioTrack.reloadStaticData();
        }
        audioTrack.write(res, 0, len);
        Log.d("+++++++", audioTrack.getState() + "  -  " + audioTrack.getPlayState());
        audioTrack.play();
        Log.d("+++++++", audioTrack.getState() + "  -  " + audioTrack.getPlayState());
    }

    boolean reload = false;
    int readSum = 0;

    public void startPlay() {
        if (audioTrack == null) return;
        if (reload) {
            Log.d("++++++", "reload");
            audioTrack.reloadStaticData();
            reload = false;
        }
//        } else {
        Log.d("++++++", "play");
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) audioTrack.stop();
        audioTrack.setNotificationMarkerPosition(readSum / 4);

//        audioTrack.reloadStaticData();
        audioTrack.setPlaybackHeadPosition(0);
        audioTrack.play();
//        }
    }

    int frequency = 44100;
    int channelConfigurationIn = AudioFormat.CHANNEL_IN_MONO;
    int channelConfigurationOut = AudioFormat.CHANNEL_OUT_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    volatile List<byte[]> recordList;

    SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    int sound = 0, streamId = 0;
    MediaPlayer mediaPlayer;
    MediaPlayer mediaPlayer2;

    File folder = new File(Environment.getExternalStorageDirectory() + "/keezy_records");
    String file2 = folder.getAbsolutePath() + "/test_for_pool2.wav";

    public void onClick(View view) {
        if (view.getId() == R.id.pool) {
            if (streamId != 0) soundPool.stop(streamId);
            streamId = soundPool.play(sound, 1, 1, 0, 0, 1);
        } else if (view.getId() == R.id.save) {
            if (mediaPlayer == null) return;
            if (mediaPlayer.isPlaying()) {
//                mediaPlayer.stop();
//                mediaPlayer.prepare();
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
            }
            mediaPlayer.start();
        } else if (view.getId() == R.id.play2) {
            if (mediaPlayer2 == null) {
                mediaPlayer2 = MediaPlayer.create(this, Uri.parse(file2));
            }
            if (mediaPlayer2.isPlaying()) {
//                mediaPlayer.stop();
//                mediaPlayer.prepare();
                mediaPlayer2.pause();
                mediaPlayer2.seekTo(0);
            }
            mediaPlayer2.start();
        }

    }

    private class RecordAudio implements Runnable {
        private volatile boolean cancel = false;
        int frequency = 44100;
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        int bufferSize;
        volatile int readSum = 0;
        AudioRecord audioRecord;
        byte[] result;


        RecordAudio() {
            bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
//            byte[] buffer = new byte[bufferSize];
            recordList = new ArrayList<byte[]>();
            audioRecord.startRecording();
            while (!cancel) {
                ByteBuffer bb = ByteBuffer.allocateDirect(bufferSize);
//                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                int bufferReadResult = audioRecord.read(bb, bufferSize);
//                byte[] data = Arrays.copyOfRange(buffer, 0, bufferReadResult);
//                recordList.add(data);
                recordList.add(bb.array());
                readSum += bufferReadResult;
            }
            audioRecord.stop();
            audioRecord.release();

            byte[] result = new byte[readSum];
            int j = 0;
            for (byte[] data : recordList)
                for (int i = 0; i < data.length; i++, j++)
                    result[j] = data[i];


            File folder = new File(Environment.getExternalStorageDirectory() + "/keezy_records");
            String file = folder.getAbsolutePath() + "/test_for_pool.wav";
            copyWaveFile(file, result);

            mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(file));

            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                public void onLoadComplete(SoundPool soundPool, int i, int i2) {
                    sound = i;
                }
            });
            int soundId = soundPool.load(folder.getAbsolutePath() + "/test_for_pool.wav", 1);
        }

        public void cancel() {
            cancel = true;
        }

        public byte[] getResult() {
            return result;
        }
    }

    private static final int RECORDER_BPP = 16;
    private static final int RECORDER_SAMPLERATE = 44100;

    private void copyWaveFile(String outFilename, byte[] data) {
        FileOutputStream out = null;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 1;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;


        try {
            out = new FileOutputStream(outFilename);
            int totalAudioLen = data.length;
            int totalDataLen = totalAudioLen + 36;


            WriteWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

            out.write(data);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = RECORDER_BPP;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }


    private class RecordAudioMem extends AsyncTask<Void, Integer, Void> implements AudioTrack.OnPlaybackPositionUpdateListener {
        @Override
        protected Void doInBackground(Void... params) {
            List<byte[]> recordList = new ArrayList<byte[]>();
            isRecording = true;
            try {

                int frequency = 44100, channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
                int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);

                byte[] buffer = new byte[bufferSize];
                audioRecord.startRecording();
                readSum = 0;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    readSum += bufferReadResult;
                    byte[] data = Arrays.copyOfRange(buffer, 0, bufferReadResult);
                    recordList.add(data);
                }
                audioRecord.stop();
                Log.d("++++++", "Size record: " + readSum);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, AudioFormat.CHANNEL_OUT_MONO, audioEncoding, readSum, AudioTrack.MODE_STATIC);
                audioTrack.setPlaybackPositionUpdateListener(this);
                byte[] result = new byte[readSum];
                int j = 0;
                for (byte[] data : recordList)
                    for (int i = 0; i < data.length; i++, j++)
                        result[j] = data[i];

                audioTrack.write(result, 0, result.length);
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
//                7472-7478/? W/AudioPolicyManagerBase﹕ stopOutput() refcount is already 0 for output 2
            }
            return null;
        }

        public void onMarkerReached(AudioTrack audioTrack) {
            //end
            Log.d("++++++", "Marker: " + audioTrack.getNotificationMarkerPosition());
            audioTrack.stop();
            audioTrack.flush();
            reload = true;
        }

        public void onPeriodicNotification(AudioTrack audioTrack) {
            // don't care
        }
    }
}
