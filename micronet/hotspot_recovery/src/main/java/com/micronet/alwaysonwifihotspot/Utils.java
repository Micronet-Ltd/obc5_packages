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

    public static String getWifiApStateName(int state) {
        switch (state) {
            case WiFiApManager.WIFI_AP_STATE_DISABLING:
                return "WIFI_AP_STATE_DISABLING";
            case WiFiApManager.WIFI_AP_STATE_DISABLED:
                return "WIFI_AP_STATE_DISABLED";
            case WiFiApManager.WIFI_AP_STATE_ENABLING:
                return "WIFI_AP_STATE_ENABLING";
            case WiFiApManager.WIFI_AP_STATE_ENABLED:
                return "WIFI_AP_STATE_ENABLED";
            case WiFiApManager.WIFI_AP_STATE_FAILED:
                return "WIFI_AP_STATE_FAILED";
            default:
                return "WIFI_AP_STATE_ UNKNOWN!";
        }
    }
}

