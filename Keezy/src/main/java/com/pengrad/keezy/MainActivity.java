package com.pengrad.keezy;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import com.pengrad.keezy.logic.MediaRecordManager;
import com.pengrad.keezy.logic.PlayManager;
import com.pengrad.keezy.logic.RecordManager;
import com.pengrad.keezy.logic.SoundPoolPlayManager;
import com.pengrad.keezy.ui.RecPlayButton;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.pengrad.keezy.Utils.log;
import static com.pengrad.keezy.Utils.toast;

/**
 * User: stas
 * Date: 17.12.13 21:36
 */

@EActivity(R.layout.activity_main)
public class MainActivity extends Activity implements View.OnTouchListener {

    @ViewById
    protected RecPlayButton button1, button2, button3, button4, button5, button6, button7, button8;

    private RecPlayButton[] buttons;
    private RecordManager recordManager;
    private PlayManager playManager;

    public static final String FILE_1 = Environment.getExternalStorageDirectory() + "/myaudio.3gp";
    public static final String FILE_2 = Environment.getExternalStorageDirectory() + "/myaudio2.3gp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordManager = new MediaRecordManager();
        playManager = new SoundPoolPlayManager(8);
    }

    @AfterViews
    protected void initViews() {
        buttons = new RecPlayButton[]{button1, button2, button3, button4, button5, button6, button7, button8};
        List<Button> b = new ArrayList<Button>();
        b.indexOf(null);
        for (View button : buttons) {
            button.setOnTouchListener(this);
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (!(view instanceof RecPlayButton)) return false;
        RecPlayButton button = (RecPlayButton) view;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                button.setPressed(true);
                if (button.isRec()) {
                    enableControls(button, false);
                    rec(true);
                } else {
                    play(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                button.setPressed(false);
                if (button.isRec()) {
                    rec(false);
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

    protected void rec(boolean start) {
        toast(this, start ? "start rec" : "stop rec");
    }

    protected void play(boolean start) {
        toast(this, start ? "start play" : "stop play");
    }

    @Background
    protected void startRecording(int i) {
        try {
            recordManager.startRecord(i == 0 ? FILE_1 : FILE_2);
        } catch (IOException e) {
            log(e.toString());
        }
    }

    @Background
    protected void stopRecording(int i) {
        recordManager.stopRecord();
        playManager.addSound(i - 1, i == 0 ? FILE_1 : FILE_2);
    }

    protected void startPlay(int i) {
        playManager.startPlay(i - 1);
    }


}