package com.pengrad.keezy.sound;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

/**
 * User: stas
 * Date: 14.05.14 20:53
 */

public class RingtonePlayManager implements PlayManager {

    private Ringtone[] ringtones;
    private Context context;

    public RingtonePlayManager(Context context, int size) {
        this.context = context;
        ringtones = new Ringtone[size];
    }

    public void addSound(int index, String path) {
        Ringtone ringtone = RingtoneManager.getRingtone(context, Uri.parse(path));
        ringtones[index] = ringtone;
    }

    public void removeSound(int index) {
        Ringtone ringtone = ringtones[index];
        if (ringtone != null) {
            ringtone.stop();
            ringtones[index] = null;
        }
    }

    public void startPlay(int index) {
        Ringtone ringtone = ringtones[index];
        if (ringtone == null) return;
        if (ringtone.isPlaying()) {
            ringtone.stop();
        }
        ringtone.play();
    }

    public void release() {
        for (int i = 0; i < ringtones.length; i++) {
            removeSound(i);
        }
    }
}
