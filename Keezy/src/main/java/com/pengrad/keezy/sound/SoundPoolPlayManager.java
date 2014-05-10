package com.pengrad.keezy.sound;

import android.media.AudioManager;
import android.media.SoundPool;

/**
 * User: stas
 * Date: 22.03.14 2:29
 */

public class SoundPoolPlayManager implements PlayManager {

    private SoundPool soundPool;
    private Sound[] sounds;

    public SoundPoolPlayManager(int size) {
        soundPool = new SoundPool(size, AudioManager.STREAM_MUSIC, 0);
        sounds = new Sound[size];
    }

    public void addSound(int index, String path) {
        Sound sound = sounds[index];
        if (sound != null) soundPool.unload(sound.soundId);
        int soundId = soundPool.load(path, 1);
        sounds[index] = new Sound(soundId);
    }

    public void removeSound(int index) {
        Sound sound = sounds[index];
        if (sound != null) {
            soundPool.unload(sound.soundId);
            sounds[index] = null;
        }
    }

    public void startPlay(int index) {
        Sound sound = sounds[index];
        if (sound != null) {
            if (sound.streamId != 0) soundPool.stop(sound.streamId);
            sound.streamId = soundPool.play(sound.soundId, 1, 1, 0, 0, 1);
        }
    }

    public void release() {
        for (int i = 0; i < sounds.length; i++) {
            removeSound(i);
        }
    }

    private static class Sound {
        int soundId, streamId;

        Sound(int soundId) {
            this.soundId = soundId;
            this.streamId = 0;
        }
    }
}
