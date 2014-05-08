package com.pengrad.keezy.test;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.File;

/**
 * User: stas
 * Date: 05.05.14 19:24
 */

public class App extends Activity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    AudioRecordManager manager;
    Integer[] freq = new Integer[]{44100, 22050, 11025, 16000, 8000};

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

        buttonRec.setOnTouchListener(new TouchListener<Button>(Button.class, recordCallback));
//        buttonPlay.setOnTouchListener(new TouchListener<Button>(Button.class, playCallback));
        buttonPool.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonPLay2.setOnClickListener(this);

        manager = new AudioRecordManager();

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, freq);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }


    public void startRec() {
        manager.startRecord(file2);
    }

    public void stopRec() {
        manager.stopRecord(null);
    }

    MediaPlayer mediaPlayer;
    MediaPlayer mediaPlayer2;

    File folder = new File(Environment.getExternalStorageDirectory() + "/keezy_records");
    String file2 = folder.getAbsolutePath() + "/test_for_pool2.wav";

    public void onClick(View view) {
        if (view.getId() == R.id.pool) {

        } else if (view.getId() == R.id.save) {

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

    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("+++++", "Select: " + freq[i]);
        manager.serFrequency(freq[i]);
    }

    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
