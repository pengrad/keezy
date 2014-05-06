package com.pengrad.keezy;

import android.content.Context;
import com.pengrad.keezy.ui.PaddingAnimation;
import com.pengrad.keezy.ui.RecPlayButton;

import java.util.HashMap;

/**
 * User: stas
 * Date: 06.05.14 23:53
 */

public class AnimationManager {

    public static final long PADDING_DURATION = 100;

    private HashMap<RecPlayButton, PaddingAnimation> paddingMap;
    private int padding;

    public AnimationManager(Context context) {
        paddingMap = new HashMap<RecPlayButton, PaddingAnimation>();
        padding = context.getResources().getDimensionPixelSize(R.dimen.button_press_padding);
    }

    public void setPaddingAnimation(RecPlayButton button) {
        PaddingAnimation animation = paddingMap.get(button);
        if (animation == null) {
            animation = new PaddingAnimation(padding, button.getContainer(), PADDING_DURATION);
            paddingMap.put(button, animation);
        }
        button.setPressAnimation(animation);
    }
}
