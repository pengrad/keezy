package com.pengrad.keezy.sound;

import java.io.IOException;

/**
 * User: stas
 * Date: 22.03.14 4:37
 */
public interface RecordManager {

    void startRecord(String path);

    void stopRecord(Runnable endCallback);

}
