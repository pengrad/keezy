package com.pengrad.keezy.sound;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * User: stas
 * Date: 07.05.14 23:19
 */

public class MediaPlayerManager implements PlayManager {

    private MediaPlayer[] mediaPlayers;
    private Context context;

    public MediaPlayerManager(Context context, int size) {
        this.context = context;
        mediaPlayers = new MediaPlayer[size];
    }

    public void addSound(int index, String path) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(path));
        mediaPlayers[index] = mediaPlayer;
    }

    public void removeSound(int index) {
        MediaPlayer old = mediaPlayers[index];
        if (old != null) {
            old.release();
            mediaPlayers[index] = null;
        }
    }

    public void startPlay(int index) {
        MediaPlayer mediaPlayer = mediaPlayers[index];
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
        mediaPlayer.start();
    }

    public void release() {
        for (int i = 0; i < mediaPlayers.length; i++) {
            removeSound(i);
        }
    }
}
