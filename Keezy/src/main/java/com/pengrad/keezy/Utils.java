package com.pengrad.keezy;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * User: stas
 * Date: 17.12.13 23:20
 */

public class Utils {

    public static void log(String text) {
//        todo убирать из релиз-версии
//        if (text != null && !text.trim().equals(""))
//            Log.d("++++++", text);
    }

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

}
