package com.pengrad.keezy.sound;

import android.media.MediaRecorder;

import java.io.IOException;

/**
 * User: stas
 * Date: 22.03.14 4:43
 */

public class MediaRecordManager implements RecordManager {

    private MediaRecorder recorder;

    public MediaRecordManager() {
        recorder = new MediaRecorder();
        prepareRecord(recorder);
    }

    public synchronized void startRecord(String path) {
        try {
            recorder.setOutputFile(path);
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            prepareRecord(recorder);
            recorder.setOutputFile(path);
            try {
                recorder.prepare();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopRecord(Runnable endCallback) {
        try {
            recorder.stop();
        } catch (IllegalStateException e) {
            // before start
        } catch (RuntimeException e) {
            // immediately after start
        } finally {
            prepareRecord(recorder);
            if (endCallback != null) endCallback.run();
        }
    }

    private void prepareRecord(MediaRecorder recorder) {
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(2);
        recorder.setAudioEncodingBitRate(320000);
        recorder.setAudioSamplingRate(96000);
    }

}
