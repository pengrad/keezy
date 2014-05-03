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

    public synchronized void startRecord(String path) throws IOException {
        try {
            recorder.setOutputFile(path);
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            prepareRecord(recorder);
            recorder.setOutputFile(path);
            recorder.prepare();
            recorder.start();
        }
    }

    public synchronized void stopRecord() {
        try {
            recorder.stop();
        } catch (IllegalStateException e) {
            // before start
        } catch (RuntimeException e) {
            // immediately after start
        } finally {
            prepareRecord(recorder);
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
