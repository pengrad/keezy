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

    public static final int ALPHA_FULL = 255;
    public static final int ALPHA_SEMI = 150;

    private boolean recording;
    private Drawable imageRecord;
    private Drawable imageRemove;
    private PressedAnimation animation;

    private void init() {
        recording = true;
        imageRecord = getResources().getDrawable(R.drawable.ic_recording);
        imageRemove = getResources().getDrawable(R.drawable.ic_remove);
    }

    @Override
    public void setPressed(final boolean pressed) {
        if (animation != null) {
            animation.setPressed(pressed);
            startAnimation(animation);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        getBackground().setAlpha(enabled ? ALPHA_FULL : ALPHA_SEMI);
    }

    public View getContainer() {
        return (View) getParent();
    }

    public void setPressAnimation(PressedAnimation animation) {
        this.animation = animation;
    }

    public boolean isRec() {
        return recording;
    }

    public void makePlay() {
        recording = false;
        setImageDrawable(null);
    }

    public void makeEdit() {
        if (recording) {
            setEnabled(false);
        } else {
            setImageDrawable(imageRemove);
        }
    }

    public void endEdit() {
        if (!isEnabled()) setEnabled(true);
        setImageDrawable(recording ? imageRecord : null);
    }

    public void makeRemove() {
        recording = true;
        setImageDrawable(imageRecord);
        setEnabled(false);
    }
}
