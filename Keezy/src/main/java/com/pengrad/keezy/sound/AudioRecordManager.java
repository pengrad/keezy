package com.pengrad.keezy.sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: stas
 * Date: 07.05.14 23:40
 */

public class AudioRecordManager implements RecordManager {

    RecordAudio recordAudio = new RecordAudio();

    public void startRecord(String path) {
        recordAudio.file = path;
        new Thread(recordAudio).start();
    }

    public void stopRecord(Runnable endCallback) {
        recordAudio.endCallback = endCallback;
        recordAudio.cancel = true;
        recordAudio = new RecordAudio();
    }

    public void init() {
        //nothing to do
    }

    public void release() {
        //nothing to do
    }

    public static boolean isOK() {
//        if(1==1) return false;
        if (RecordAudio.BUFFER_SIZE <= 0) return false;
        AudioRecord audioRecord = RecordAudio.makeAudioRecord();
        return audioRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }

    private static class RecordAudio implements Runnable {
        public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
        public static final byte CHANNEL_COUNT = 1;
        public static final byte BITS_PER_SAMPLE = 16;
        public static final int SAMPLE_RATE = 44100;   // 22050, 11025, 16000, 8000  44100
        public static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

        public static AudioRecord makeAudioRecord() {
            return new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        }

        private volatile boolean cancel = false;
        private volatile Runnable endCallback;

        private String file;

        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            AudioRecord audioRecord = makeAudioRecord();
            List<byte[]> recordList = new ArrayList<byte[]>();

            audioRecord.startRecording();
            while (!cancel) {
                ByteBuffer bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
                int bufferReadResult = audioRecord.read(bb, BUFFER_SIZE);
                if (bb.hasArray()) {
                    recordList.add(bb.array());
                } else {
                    byte[] data = new byte[bufferReadResult];
                    bb.get(data, 0, data.length);
                    recordList.add(data);
                }
            }
            audioRecord.stop();
            audioRecord.release();

            WavFileCreator.writeWavFile(file, recordList, SAMPLE_RATE, BITS_PER_SAMPLE, CHANNEL_COUNT);

            if (endCallback != null) endCallback.run();
        }
    }

}
