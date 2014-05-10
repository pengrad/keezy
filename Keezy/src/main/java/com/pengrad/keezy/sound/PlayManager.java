package com.pengrad.keezy.sound;

/**
 * User: stas
 * Date: 22.03.14 2:35
 */
public interface PlayManager {

    void addSound(int index, String path);

    void removeSound(int index);

    void startPlay(int index);

    void release();

}
