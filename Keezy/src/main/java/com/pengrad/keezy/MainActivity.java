package com.pengrad.keezy;

import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
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
public class MainActivity extends ActionBarActivity {

    @ViewById
    protected RecPlayButton button1, button2, button3, button4, button5, button6, button7, button8;

    private String files[];
    private List<RecPlayButton> buttons;
    private RecordManager recordManager;
    private PlayManager playManager;
    private MenuItem menuEdit, menuDone;
    private TouchListener editTouchListener, recordListener, playListener;

    @AfterViews
    protected void initViews() {
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

        editTouchListener = new TouchListener<RecPlayButton>(RecPlayButton.class, new Callback<RecPlayButton>() {
            public void onTouchDown(RecPlayButton view) {
            }

            public void onTouchUp(RecPlayButton view) {
                onEditTouchUp(view);
            }
        });

        recordListener = new TouchListener<RecPlayButton>(RecPlayButton.class, new Callback<RecPlayButton>() {
            public void onTouchDown(RecPlayButton view) {
                onRecDown(view);
            }

            public void onTouchUp(RecPlayButton view) {
                onRecUp(view);
            }
        });

        playListener = new TouchListener<RecPlayButton>(RecPlayButton.class, new Callback<RecPlayButton>() {
            public void onTouchDown(RecPlayButton view) {
                onPlayDown(view);
            }

            public void onTouchUp(RecPlayButton view) {
            }
        });
        buttons = new ArrayList<RecPlayButton>(8);
        Collections.addAll(buttons, button1, button2, button3, button4, button5, button6, button7, button8);
        for (View button : buttons) {
            button.setOnTouchListener(recordListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        menuEdit = menu.findItem(R.id.action_edit);
        menuDone = menu.findItem(R.id.action_done);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                startEdit();
                return true;
            case R.id.action_done:
                endEdit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    protected void disableOtherButtons(View enabledView, boolean enable) {
        for (View button : buttons) {
            if (button.getId() != enabledView.getId()) button.setEnabled(enable);
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

    protected void startEdit() {
        menuEdit.setVisible(false);
        menuDone.setVisible(true);
        for (RecPlayButton button : buttons) {
            button.makeEdit();
            button.setOnTouchListener(editTouchListener);
        }
    }

    public void onEditTouchUp(RecPlayButton button) {
        button.makeRemove();
    }

    protected void endEdit() {
        menuDone.setVisible(false);
        menuEdit.setVisible(true);
        for (RecPlayButton button : buttons) {
            button.endEdit();
            button.setOnTouchListener(button.isRec() ? recordListener : playListener);
        }
    }
}