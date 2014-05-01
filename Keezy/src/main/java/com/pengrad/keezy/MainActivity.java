package com.pengrad.keezy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import com.pengrad.keezy.logic.MediaRecordManager;
import com.pengrad.keezy.logic.PlayManager;
import com.pengrad.keezy.logic.RecordManager;
import com.pengrad.keezy.logic.SoundPoolPlayManager;
import com.pengrad.keezy.ui.RecPlayButton;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pengrad.keezy.Utils.log;

/**
 * User: stas
 * Date: 17.12.13 21:36
 */

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements View.OnTouchListener {

    @ViewById
    protected RecPlayButton button1, button2, button3, button4, button5, button6, button7, button8;

    private String files[];
    private List<RecPlayButton> buttons;
    private RecordManager recordManager;
    private PlayManager playManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordManager = new MediaRecordManager();
        playManager = new SoundPoolPlayManager(8);
        File folder = new File(Environment.getExternalStorageDirectory() + "/keezy_records");
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                log("Can't create folder");
                //todo Dialog and exit
            }
        }
        files = new String[8];
        for (int i = 0; i < 8; i++) {
            files[i] = folder + "/record_" + i + ".3gp";
        }
    }

    @AfterViews
    protected void initViews() {
        buttons = new ArrayList<RecPlayButton>(8);
        Collections.addAll(buttons, button1, button2, button3, button4, button5, button6, button7, button8);
        for (View button : buttons) {
            button.setOnTouchListener(this);
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (!(view instanceof RecPlayButton)) return false;
        RecPlayButton button = (RecPlayButton) view;
        int index = buttons.indexOf(button);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                button.setPressed(true);
                if (button.isRec()) {
                    enableControls(button, false);
                    startRecord(index);
                } else {
                    startPlay(index);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                button.setPressed(false);
                if (button.isRec()) {
                    stopRecord(index);
                    enableControls(button, true);
                    button.makePlay();
                }
                break;
        }
        return true;
    }

    private void enableControls(View view, boolean enable) {
        for (View button : buttons) {
            if (button.getId() != view.getId()) button.setEnabled(enable);
        }
    }

    @Background
    protected void startRecord(int i) {
        try {
            recordManager.startRecord(files[i]);
        } catch (IOException e) {
            log(e.toString());
        } catch (RuntimeException e) {
            log(e.toString());
        }
    }

    @Background
    protected void stopRecord(int i) {
        recordManager.stopRecord();
        playManager.addSound(i, files[i]);
    }

    @Background
    protected void startPlay(int i) {
        playManager.startPlay(i);
    }


}