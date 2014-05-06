package com.pengrad.keezy.ui;

import android.view.View;
import android.view.animation.Transformation;

/**
 * User: stas
 * Date: 06.05.14 23:49
 */

public class PaddingAnimation extends PressedAnimation {
    private View view;
    private int paddingPressed;

    public PaddingAnimation(int paddingPressed, View view, long duration) {
        this.paddingPressed = paddingPressed;
        this.view = view;
        setDuration(duration);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int padding = paddingPressed;
        if (!isPressed) {
            interpolatedTime = Math.abs(interpolatedTime - 1);
            padding = view.getPaddingLeft();
        }
        padding *= interpolatedTime;
        view.setPadding(padding, padding, padding, padding);
    }
}