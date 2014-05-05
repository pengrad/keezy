package com.pengrad.keezy.test;

import android.view.MotionEvent;
import android.view.View;

/**
 * User: stas
 * Date: 03.05.14 1:11
 */

public class TouchListener<T> implements View.OnTouchListener {

    public abstract static class Callback<V> {
        void onTouchDown(V view) {
        }

        void onTouchUp(V view) {
        }
    }

    private Callback<T> callback;
    private Class<T> clazz;

    public TouchListener(Class<T> clazz, Callback<T> callback) {
        this.callback = callback;
        this.clazz = clazz;
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (!clazz.isInstance(view)) return false;
        T tView = clazz.cast(view);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                view.setPressed(true);
                callback.onTouchDown(tView);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_OUTSIDE:
                view.setPressed(false);
                callback.onTouchUp(tView);
                break;
            case MotionEvent.ACTION_CANCEL:
                view.setPressed(false);
                break;
        }
        return true;
    }
}