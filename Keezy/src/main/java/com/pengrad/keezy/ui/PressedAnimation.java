package com.pengrad.keezy.ui;

import android.view.animation.Animation;

/**
 * User: stas
 * Date: 06.05.14 23:49
 */

public abstract class PressedAnimation extends Animation {

    protected boolean isPressed;

    public void setPressed(boolean isPressed) {
        this.isPressed = isPressed;
    }
}
