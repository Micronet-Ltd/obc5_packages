/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.wifi;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.provider.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.WifiManager;

import android.util.Log;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.R;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;

import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import android.os.SystemProperties;


public final class WifiStatusReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiStatusReceiver";
	private Context mContext;
	private boolean isWifiConfigured = false;
	private boolean isHotspotEnabled = true;
    private static final String AP_CONFIG_FILE = "/data/misc/wifi/softap.conf";

    private static final int AP_CONFIG_FILE_VERSION = 1;
    WifiConfiguration config = new WifiConfiguration();
	WifiManager mWifiManager;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    
        String action = intent.getAction();
        Log.v(TAG, "Received: " + action);
		
        mContext = context;
		mWifiManager =(WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		isWifiConfigured = SystemProperties.getBoolean("persist.sys.iswificonfigured", false);
		isHotspotEnabled = SystemProperties.getBoolean("persist.sys.isHotspotEnable", true);
			
        if (action.equals("com.android.telephony.GET_IMEI_DONE") && !isWifiConfigured) {
					
		    TelephonyManager telephonyManager = TelephonyManager.getDefault();
			
			String imei = telephonyManager.getImei(0);
		       
			if(imei == null || imei.length() < 15){
	            Log.v(TAG, "imei is invalid,return ");
				return;
			}else{						   
				Log.e(TAG, "IMEI=" + imei);
						
				setDefaultApConfiguration(imei);
				SystemProperties.set("persist.sys.iswificonfigured", "true");
				starthotspot(true);		
			}
        }else if(action.equals("android.intent.action.BOOT_COMPLETED") && isWifiConfigured){ 		  
            starthotspot(true);			
		}else if(action.equals("android.intent.action.AIRPLANE_MODE") && isWifiConfigured){
		    boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
	        if(!isAirplaneMode && isHotspotEnabled) {
	            Log.e(TAG, "isAirplaneMode is closed,open host ap");
				starthotspot(true);
	        } else {
	            Log.e(TAG, "isAirplaneMode is open,do nothing");
	        }
		}
    }

	private void starthotspot(boolean enable){
		final ContentResolver cr = mContext.getContentResolver();
		
		int wifiState = mWifiManager.getWifiState();

		Log.e(TAG, "enable =" + enable);
		if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) || 
		(wifiState == WifiManager.WIFI_STATE_ENABLED))) { 
			mWifiManager.setWifiEnabled(false); 
			Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1); 
		} 
        if (isHotspotEnabled) {
		    mWifiManager.setWifiApEnabled(null, enable);
        }
	}

	private void setDefaultApConfiguration(String imei) {
						
			String ssid_prefix = android.os.Build.MODEL;

			config.SSID = ssid_prefix + "_" + imei.substring(9);
			
			Log.e(TAG, "SSID=" + config.SSID);
				
			if (TextUtils.isEmpty(config.SSID)) {
				config.SSID = mContext.getString(R.string.wifi_tether_configure_ssid_default);
			}
	
			config.allowedKeyManagement.set(mContext.getResources().getBoolean(
					R.bool.set_wifi_hotspot_security_none) ? KeyMgmt.NONE : KeyMgmt.WPA2_PSK);
	        config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);			
			config.preSharedKey = imei.substring(7);
			
			if (TextUtils.isEmpty(config.preSharedKey)) {
				String randomUUID = UUID.randomUUID().toString();
				config.preSharedKey = randomUUID.substring(0, 8)
						+ randomUUID.substring(9, 13);
			}
			
	        Log.e(TAG, "preSharedKey=" + config.preSharedKey);
			
			mWifiManager.setWifiApConfiguration(config);
		}

	   
}
