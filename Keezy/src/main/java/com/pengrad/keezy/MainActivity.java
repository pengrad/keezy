package com.pengrad.keezy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.pengrad.keezy.logic.*;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;

import static com.pengrad.keezy.Utils.log;

/**
 * User: stas
 * Date: 17.12.13 21:36
 */

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements View.OnTouchListener {

    private RecordManager recordManager;
    private PlayManager playManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordManager = new MediaRecordManager(2);
        playManager = new SoundPoolPlayManager(2);
        button1.setOnTouchListener(this);
    }

    @ViewById
    Button button1, button2, button3, button4, button5, button6, button7, button8;

    public static final String FILE_1 = Environment.getExternalStorageDirectory() + "/myaudio.3gp";
    public static final String FILE_2 = Environment.getExternalStorageDirectory() + "/myaudio2.3gp";

    @Touch
    protected synchronized void button1Touched(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startRecording(1);
                view.setPressed(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                view.setPressed(false);
                stopRecording(1);
                break;
        }
    }

    @Touch
    protected synchronized void button2Touched(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startRecording(2);
                view.setPressed(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                view.setPressed(false);
                stopRecording(2);
                break;
        }
    }

    @Touch
    protected synchronized void button3Touched(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startPlay(1);
                view.setPressed(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                view.setPressed(false);
                break;
        }
    }

    @Touch
    protected void button4Touched(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startPlay(2);
                view.setPressed(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                view.setPressed(false);
                break;
        }
    }


    @Background
    protected void startRecording(int i) {
        try {
            recordManager.startRecord(i - 1, i == 0 ? FILE_1 : FILE_2);
        } catch (IOException e) {
            log(e.toString());
        }
    }

    @Background
    protected void stopRecording(int i) {
        recordManager.stopRecord(i - 1);
        playManager.addSound(i - 1, i == 0 ? FILE_1 : FILE_2);
    }

    protected void startPlay(int i) {
        playManager.startPlay(i - 1);
    }


    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }
}