package com.pengrad.keezy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * User: stas
 * Date: 23.04.14 21:04
 */

public class RecPlayButton extends Button {

    public RecPlayButton(Context context) {
        super(context);
    }

    public RecPlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecPlayButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //    public static final byte STATE_REC = 1;
//    public static final byte STATE_PLAY = 2;
    private boolean recording = true;

    public void makeRec() {
        recording = true;
    }

    public void makePlay() {
        recording = false;
    }

    public boolean isRec() {
        return recording;
    }
}
