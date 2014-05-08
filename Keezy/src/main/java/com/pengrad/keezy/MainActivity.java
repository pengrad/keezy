package com.pengrad.keezy;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import com.pengrad.keezy.sound.AudioRecordManager;
import com.pengrad.keezy.sound.MediaPlayerManager;
import com.pengrad.keezy.sound.PlayManager;
import com.pengrad.keezy.sound.RecordManager;
import com.pengrad.keezy.ui.RecPlayButton;
import org.androidannotations.annotations.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pengrad.keezy.TouchListener.Callback;
import static com.pengrad.keezy.Utils.log;

/**
 * User: stas
 * Date: 17.12.13 21:36
 */

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends ActionBarActivity {

    //    public static final String PREFS_ITEM_NAME = "recordsState";
    public static final String PREFS_ITEM_NAME = "recordsState_v1.1 ";
    public static final int SIZE = 8;

    @ViewById
    protected RecPlayButton button1, button2, button3, button4, button5, button6, button7, button8;

    @OptionsMenuItem
    protected MenuItem menuEdit, menuDone;

    private String[] files;
    private List<RecPlayButton> buttons;
    private RecordManager recordManager;
    private PlayManager playManager;
    private TouchListener editTouchListener, recordListener, playListener;
    private int recordsState;
    private AnimationManager animationManager;

    @AfterViews
    protected void initViews() {
//        recordManager = new MediaRecordManager();
        recordManager = new AudioRecordManager();
//        playManager = new SoundPoolPlayManager(SIZE);
        playManager = new MediaPlayerManager(getApplicationContext(), SIZE);
        File folder = new File(Environment.getExternalStorageDirectory() + "/keezy_records");
        if (!folder.exists() && !folder.mkdir()) {
            log("Can't create folder");
            //todo Dialog and exit
        }
        files = new String[SIZE];
        for (int i = 0; i < SIZE; i++) files[i] = folder + "/record_" + i + ".wav";

        Callback<RecPlayButton> recordCallback = new Callback<RecPlayButton>() {
            public void onTouchDown(RecPlayButton view) {
                onRecDown(view);
            }

            public void onTouchUp(RecPlayButton view) {
                onRecUp(view);
            }
        };
        Callback<RecPlayButton> playCallback = new Callback<RecPlayButton>() {
            public void onTouchDown(RecPlayButton view) {
                onPlayDown(view);
            }
        };
        Callback<RecPlayButton> editCallback = new Callback<RecPlayButton>() {
            public void onTouchDown(RecPlayButton view) {
                onEditDown(view);
            }
        };

        playListener = new TouchListener<RecPlayButton>(RecPlayButton.class, playCallback);
        recordListener = new TouchListener<RecPlayButton>(RecPlayButton.class, recordCallback);
        editTouchListener = new TouchListener<RecPlayButton>(RecPlayButton.class, editCallback);

        buttons = new ArrayList<RecPlayButton>(SIZE);
        Collections.addAll(buttons, button1, button2, button3, button4, button5, button6, button7, button8);

        recordsState = getPreferences(MODE_PRIVATE).getInt(PREFS_ITEM_NAME, 0);

        animationManager = new AnimationManager(this);

        for (int i = 0; i < buttons.size(); i++) {
            int buttonBit = (int) Math.pow(2, i);
            RecPlayButton button = buttons.get(i);
            animationManager.setPaddingAnimation(button);
            if ((recordsState & buttonBit) == buttonBit) {
                button.makePlay();
                button.setOnTouchListener(playListener);
                playManager.addSound(i, files[i]);
            } else {
                button.setOnTouchListener(recordListener);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getPreferences(MODE_PRIVATE).edit().putInt(PREFS_ITEM_NAME, recordsState).commit();
    }

    @OptionsItem(R.id.menu_edit)
    protected void startEdit() {
        menuEdit.setVisible(false);
        menuDone.setVisible(true);
        for (RecPlayButton button : buttons) {
            button.makeEdit();
            button.setPressAnimation(null);
            button.setOnTouchListener(editTouchListener);
        }
    }

    @OptionsItem(R.id.menu_done)
    protected void endEdit() {
        menuDone.setVisible(false);
        menuEdit.setVisible(true);
        for (RecPlayButton button : buttons) {
            button.endEdit();
            animationManager.setPaddingAnimation(button);
            button.setOnTouchListener(button.isRec() ? recordListener : playListener);
        }
    }

    protected void disableOtherButtons(View enabledView, boolean enable) {
        for (View button : buttons) {
            if (button.getId() != enabledView.getId()) button.setEnabled(enable);
        }
    }

    public void onRecDown(RecPlayButton button) {
        int index = buttons.indexOf(button);
        startRecord(index);
        disableOtherButtons(button, false);
    }

    public void onRecUp(RecPlayButton button) {
        int index = buttons.indexOf(button);
        stopRecord(index);
        disableOtherButtons(button, true);
        button.makePlay();
        button.setOnTouchListener(playListener);
        recordsState = recordsState | (int) Math.pow(2, index);
    }

    public void onPlayDown(RecPlayButton button) {
        int index = buttons.indexOf(button);
        startPlay(index);
    }

    // Remove record, change button state
    public void onEditDown(RecPlayButton button) {
        int index = buttons.indexOf(button);
        button.makeRemove();
        playManager.removeSound(index);
        recordsState = recordsState ^ (int) Math.pow(2, index);
    }

    //    @Background
    protected void startRecord(int i) {
        try {
            recordManager.startRecord(files[i]);
        } catch (RuntimeException e) {
            log(e.toString());
        }
    }

    //    @Background
    protected void stopRecord(final int i) {
        recordManager.stopRecord(new Runnable() {
            public void run() {
                playManager.addSound(i, files[i]);
            }
        });
    }

    //    @Background
    protected void startPlay(int i) {
        playManager.startPlay(i);
    }
}