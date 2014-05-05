package com.pengrad.keezy.test;

import android.app.Activity;
import android.media.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: stas
 * Date: 05.05.14 19:24
 */

public class App extends Activity {

    AudioTrack audioTrack;
    boolean isRecording = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button buttonPlay = (Button) findViewById(R.id.play);
        Button buttonRec = (Button) findViewById(R.id.rec);

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
                startPlay();
            }
        };

        buttonRec.setOnTouchListener(new TouchListener<Button>(Button.class, recordCallback));
        buttonPlay.setOnTouchListener(new TouchListener<Button>(Button.class, playCallback));
    }

    public void startRec() {
        Log.d("++++++", "start rec");
        new RecordAudioMem().execute();
    }

    public void stopRec() {
        Log.d("++++++", "stop rec");
        isRecording = false;
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
                int r = 0;
                readSum = 0;
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    readSum += bufferReadResult;
                    byte[] data = Arrays.copyOfRange(buffer, 0, bufferReadResult);
                    recordList.add(data);
                    publishProgress(r);
                    r++;
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
