package com.pengrad.keezy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.pengrad.keezy.R;

/**
 * User: stas
 * Date: 23.04.14 21:04
 */

public class RecPlayButton extends ImageView {

    public RecPlayButton(Context context) {
        super(context);
        init();
    }

    public RecPlayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecPlayButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private int padding;
    private boolean recording;

    private void init() {
        padding = getResources().getDimensionPixelSize(R.dimen.button_press_padding);
        recording = true;
    }

    public void makeRec() {
        recording = true;
    }

    public void makePlay() {
        recording = false;
    }

    public boolean isRec() {
        return recording;
    }

    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        int padding = pressed ? this.padding : 0;
        ((View) getParent()).setPadding(padding, padding, padding, padding);
    }
}
