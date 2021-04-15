package com.github.sdwfqin.matisse.internal.utils;

import android.os.Build;

/**
 * @author JoongWon Baik
 */
public class Platform {
    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
