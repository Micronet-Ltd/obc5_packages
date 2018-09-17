/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.android.settings.R;

public class DeviceXinxi extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.device_xinxi);
        Preference rom = (Preference) findPreference("jsneicun");
        Preference ram = (Preference) findPreference("yxneicun");
        rom.setSummary(getRom());
        ram.setSummary(getRam());

    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    
    private String getRom() {
        String fileName = "/sys/class/block/mmcblk0/size";
        String line = null;
        int rom = 0;
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                if (line != null) {
                    rom = ((int)(Integer.parseInt(line) / 2000000.0)) + 1;
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Integer.toString(rom) + "GB";
    }

    private String getRam() {
        String fileName = "/proc/meminfo";
        String line = null;
        int ram = 0;
        String memTotal = "";
        String mapped = "";
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("MemTotal")) {
                    memTotal = line.replaceAll("\\s", "");
                    memTotal = memTotal.substring(line.indexOf(":") + 1);
                    memTotal = memTotal.substring(0, memTotal.length() - 2);
                }
                else if (line.contains("Mapped")) {
                    mapped = line.replaceAll("\\s", "");
                    mapped = mapped.substring(line.indexOf(":") + 1);
                    mapped = mapped.substring(0, mapped.length() - 2);
                }
            }
            ram = (int) ((Integer.parseInt(memTotal) + Integer.parseInt(mapped)) / 1024000.0);
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Integer.toString(ram) + "GB";
    }

}
