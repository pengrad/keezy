package com.pengrad.keezy.logic;

import java.io.IOException;

/**
 * User: stas
 * Date: 22.03.14 4:37
 */
public interface RecordManager {

    void startRecord(int index, String path) throws IOException;

    void stopRecord(int index);

}
