package com.pengrad.keezy.test;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * User: stas
 * Date: 07.05.14 23:40
 */

public class AudioRecordManager {

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

    public void serFrequency(int frequency) {
        recordAudio.FREQUENCY = frequency;
    }


    private static class RecordAudio implements Runnable {
        public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
        //        public static final int FREQUENCY = 44100;   // 22050, 11025, 16000, 8000  44100
        public static final int RECORDER_BPP = 16;


        private volatile boolean cancel = false;
        private volatile Runnable endCallback;

        private String file;
        private AudioRecord audioRecord;
        private int FREQUENCY = 44100;

        public void cancel() {
            cancel = true;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

            int BUFFER_SIZE = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIG, AUDIO_FORMAT);

            Log.d("++++++", "Start record with freq:" + FREQUENCY);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
            List<byte[]> recordList = new ArrayList<byte[]>();
            int readSum = 0;
            audioRecord.startRecording();
            while (!cancel) {
                ByteBuffer bb = ByteBuffer.allocateDirect(BUFFER_SIZE);
                int bufferReadResult = audioRecord.read(bb, BUFFER_SIZE);
//                if(bb.hasArray()) recordList.add(bb.array());
                byte[] data = new byte[bufferReadResult];
                bb.get(data, 0, data.length);
                recordList.add(data);
                readSum += bufferReadResult;
            }
            audioRecord.stop();
            audioRecord.release();

//            byte[] result = new byte[readSum];
//            int j = 0;
//            for (byte[] data : recordList) {
//                for (int i = 0; i < data.length; i++, j++) result[j] = data[i];
//            }

            writeWaveFile(file, recordList, readSum);

            if (endCallback != null) endCallback.run();
        }

        private void writeWaveFile(String outFilename, List<byte[]> data, int size) {
            long longSampleRate = FREQUENCY;
            int channels = 1;
            long byteRate = RECORDER_BPP * FREQUENCY * channels / 8;
            Log.d("+++++", "byteRate: " + byteRate);
            try {
                FileOutputStream out = new FileOutputStream(outFilename);
                int totalAudioLen = size;
                int totalDataLen = totalAudioLen + 36;

                writeWaveFileHeader(out, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);

                for (byte[] bytes : data) out.write(bytes);

                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate) throws IOException {
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
    }
}
