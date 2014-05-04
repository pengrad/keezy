package com.pengrad.keezy;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import com.pengrad.keezy.sound.MediaRecordManager;
import com.pengrad.keezy.sound.PlayManager;
import com.pengrad.keezy.sound.RecordManager;
import com.pengrad.keezy.sound.SoundPoolPlayManager;
import com.pengrad.keezy.ui.RecPlayButton;
import org.androidannotations.annotations.*;

import java.io.File;
import java.io.IOException;
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

    @ViewById
    protected RecPlayButton button1, button2, button3, button4, button5, button6, button7, button8;

    @OptionsMenuItem
    protected MenuItem menuEdit, menuDone;

    private String files[];
    private List<RecPlayButton> buttons;
    private RecordManager recordManager;
    private PlayManager playManager;
    private TouchListener editTouchListener, recordListener, playListener;

    @AfterViews
    protected void initViews() {
        final int size = 8;
        recordManager = new MediaRecordManager();
        playManager = new SoundPoolPlayManager(size);
        File folder = new File(Environment.getExternalStorageDirectory() + "/keezy_records");
        if (!folder.exists() && !folder.mkdir()) {
            log("Can't create folder");
            //todo Dialog and exit
        }
        files = new String[size];
        for (int i = 0; i < size; i++) files[i] = folder + "/record_" + i + ".3gp";

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

        buttons = new ArrayList<RecPlayButton>(8);
        Collections.addAll(buttons, button1, button2, button3, button4, button5, button6, button7, button8);
        for (View button : buttons) button.setOnTouchListener(recordListener);
    }

    @OptionsItem(R.id.menu_edit)
    protected void startEdit() {
        menuEdit.setVisible(false);
        menuDone.setVisible(true);
        for (RecPlayButton button : buttons) {
            button.makeEdit();
            button.setOnTouchListener(editTouchListener);
        }
    }

    @OptionsItem(R.id.menu_done)
    protected void endEdit() {
        menuDone.setVisible(false);
        menuEdit.setVisible(true);
        for (RecPlayButton button : buttons) {
            button.endEdit();
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
        disableOtherButtons(button, false);
        startRecord(index);
    }

    public void onRecUp(RecPlayButton button) {
        int index = buttons.indexOf(button);
        stopRecord(index);
        disableOtherButtons(button, true);
        button.makePlay();
        button.setOnTouchListener(playListener);
    }

    public void onPlayDown(RecPlayButton button) {
        int index = buttons.indexOf(button);
        startPlay(index);
    }

    // Remove record, change button state
    public void onEditDown(RecPlayButton button) {
        button.makeRemove();
        playManager.removeSound(buttons.indexOf(button));
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