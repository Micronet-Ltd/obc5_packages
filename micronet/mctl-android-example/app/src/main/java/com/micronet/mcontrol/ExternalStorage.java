package com.micronet.mcontrol;

import android.os.Environment;

public class ExternalStorage {
    /**
     * Get the sd card path of the device
     * @return the sd card path of the device
     */
    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/";
    }
}