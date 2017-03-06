package com.micronet.alwaysonwifihotspot;

import android.content.ContentResolver;
import android.provider.Settings;

/**
 * Created by eemaan.siddiqi on 1/19/2017.
 */

//To check airplane mode status
public class Utils {
    /**
     * Returns true if airplane mode is enabled, false otherwise.
     * @param content ContentResolver
     */
    public static boolean isAirplaneMode(ContentResolver content) {
        boolean isAirplaneOn=Settings.System.getInt(content,Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        return isAirplaneOn;
    }
}

