package com.pengrad.keezy.sound;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;

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
        recordAudio.cancel();
        recordAudio = new RecordAudio();
    }

    public void release() {
        //nothing to do
    }

    public static int getBufSize() {
        return AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    private static class RecordAudio implements Runnable {
        public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
        public static final byte CHANNEL_COUNT = 1;
        public static final byte BITS_PER_SAMPLE = 16;
        public static final int SAMPLE_RATE = 44100;   // 22050, 11025, 16000, 8000  44100
        public static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

        private volatile boolean cancel = false;
        private volatile Runnable endCallback;

        private String file;

        public void cancel() {
            cancel = true;
        }

        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
            List<byte[]> recordList = new ArrayList<byte[]>();
//            int readSum = 0;

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
//                readSum += bufferReadResult;
            }
            audioRecord.stop();
            audioRecord.release();

            WavFileCreator.writeWavFile(file, recordList, SAMPLE_RATE, BITS_PER_SAMPLE, CHANNEL_COUNT);

            if (endCallback != null) endCallback.run();
        }
    }

}
