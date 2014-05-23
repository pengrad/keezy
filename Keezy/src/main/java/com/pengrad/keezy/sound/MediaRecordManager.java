package com.pengrad.keezy.sound;

import android.media.MediaRecorder;

import java.io.IOException;

/**
 * User: stas
 * Date: 22.03.14 4:43
 */

public class MediaRecordManager implements RecordManager {

    private MediaRecorder recorder;

    public void startRecord(String path) {
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

    public void stopRecord(Runnable endCallback) {
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

    public void init() {
        recorder = new MediaRecorder();
        prepareRecord(recorder);
    }

    public void release() {
        recorder.release();
    }

    private void prepareRecord(MediaRecorder recorder) {
        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
    }

}
