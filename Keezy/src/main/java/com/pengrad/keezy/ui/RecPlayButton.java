package com.pengrad.keezy.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
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
    private Drawable imageRecord;

    private void init() {
        recording = true;
        padding = getResources().getDimensionPixelSize(R.dimen.button_press_padding);
        imageRecord = getResources().getDrawable(R.drawable.ic_recording);
    }

    public void makeRec() {
        recording = true;
        setImageDrawable(imageRecord);
    }

    public void makePlay() {
        recording = false;
        setImageDrawable(null);
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
