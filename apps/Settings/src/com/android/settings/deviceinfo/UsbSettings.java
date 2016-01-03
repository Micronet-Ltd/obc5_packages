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

package com.android.settings.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.UserManager;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

/**
 * USB storage settings.
 */
public class UsbSettings extends SettingsPreferenceFragment {

    private static final boolean DEBUG = false;
    private static final String TAG = "UsbSettings";

    private static final String KEY_MTP = "usb_mtp";
    private static final String KEY_PTP = "usb_ptp";
    private static final String KEY_CHARGING = "usb_charging";
    private static final String KEY_SDCARD = "usb_sdcard";

    // We could not know what's the usb default mode config of each device, which
    // may be defined in some sh source file. So here use a hard code for reference,
    // you should modify this value according to device usb init config.

/* delete by shengweiguang for usb default mode 2015-08-13 begin */	
 //   private static final String USB_FUNCTION_DEFAULT = SystemProperties.get(
 //           "ro.sys.usb.default.config", "diag,serial_smd,serial_tty,rmnet_bam,mass_storage");
/* delete by shengweiguang for usb default mode 2015-08-13 end */	

    private UsbManager mUsbManager;
    private CheckBoxPreference mMtp;
    private CheckBoxPreference mPtp;
    private CheckBoxPreference mCharging;
    private CheckBoxPreference mSDCard;
    private boolean mUsbAccessoryMode;
    private boolean operateInprogress = false;

    /* Added by shengweiguang to add a new "pc connect" mode 2015-08-13 begin */
    private CheckBoxPreference mPCAdb;
    private static final String KEY_PC_ADB = "usb_pc_adb";
    private boolean mIsPcAdbChecked = false;
    /* Added by shengweiguang to add a new "pc connect" mode 2015-08-11 end */

    private StorageManager mStorageManager = null;

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UsbManager.ACTION_USB_STATE)) {
               mUsbAccessoryMode = intent.getBooleanExtra(UsbManager.USB_FUNCTION_ACCESSORY, false);
                /* Modified by shengweiguang to avoid USB_CONNECTED empty while USB UEVENT was sent before boot completed 2015-08-13 begin */
                boolean connected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, true);
                Log.e(TAG, "UsbAccessoryMode " + mUsbAccessoryMode + ", connected: " + connected);
                /* Modified by shengweiguang to avoid USB_CONNECTED empty while USB UEVENT was sent before boot completed 2015-08-13 end */
                if (!connected) {
                    finish();
                    return;
                } else {
                    // once USB connected agian, we take setting operation as completed
                    operateInprogress = false;
                    updateUsbFunctionState();
                }
            }
        }
    };

    private void updateUsbFunctionState() {
        String functions = SystemProperties.get("persist.sys.usb.config", "");
        if (functions.contains(UsbManager.USB_FUNCTION_DEFAULT)) {
            updateToggles(UsbManager.USB_FUNCTION_DEFAULT);
        } else {
            updateToggles(mUsbManager.getDefaultFunction());
        }
    }

    private boolean isMassStorageEnabled() {
        // Mass storage is enabled if primary volume supports it
        final StorageVolume[] volumes = mStorageManager.getVolumeList();
        for (StorageVolume v : volumes) {
            if (v.allowMassStorage()) {
                return true;
            }
        }
        return false;
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.usb_settings);
        root = getPreferenceScreen();

        mMtp = (CheckBoxPreference)root.findPreference(KEY_MTP);
        mPtp = (CheckBoxPreference)root.findPreference(KEY_PTP);
        mCharging = (CheckBoxPreference)root.findPreference(KEY_CHARGING);
/* delete by shengweiguang begin */
	  // mSDCard = (CheckBoxPreference)root.findPreference(KEY_SDCARD);
/* delete by shengweiguang end */
	   
/* Added by shengweiguang to add a new "pc connect" mode 2015-08-13 begin */
		mPCAdb = (CheckBoxPreference)root.findPreference(KEY_PC_ADB); 
/* Added by shengweiguang to add a new "pc connect" mode 2015-08-13 end */

        //not to show this mode if mass storage is not supported
        if (!isMassStorageEnabled()) {
            Log.d(TAG, "createPreferenceHierarchy mass_storage enabled");
            root.removePreference(mSDCard);
        }
        boolean isSimCardInserted = SystemProperties
                .getBoolean("persist.sys.sim.activate", false);
        boolean isUsbSecurityEnable = SystemProperties
                .getBoolean("persist.sys.usb.security", false);
        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)
                || (!isSimCardInserted && isUsbSecurityEnable)) {
            mMtp.setEnabled(false);
            mPtp.setEnabled(false);
            //mSDCard.setEnabled(false);
        }

        return root;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
    }

    private StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState,
                String newState) {
            if (DEBUG)
                Log.i(TAG, "onStorageStateChanged path= " + path
                        + " oldState = " + oldState + " newState= " + newState);
            final boolean isExternalPath = (Environment.getExternalStorageDirectory().getPath()
                    .equals(path));
            // just enable UMS for external
            if (!isExternalPath) return;
            if (newState.equals(Environment.MEDIA_SHARED)) {
                Toast.makeText(getActivity(), R.string.external_storage_turn_on,
                        Toast.LENGTH_SHORT).show();
            } else if (oldState.equals(Environment.MEDIA_SHARED)
                    && newState.equals(Environment.MEDIA_UNMOUNTED)) {
                Toast.makeText(getActivity(), R.string.external_storage_turn_off,
                    Toast.LENGTH_SHORT).show();
            }
            updateUsbFunctionState();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mStateReceiver);
        if (mStorageManager != null) {
            mStorageManager.unregisterListener(mStorageListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Make sure we reload the preference hierarchy since some of these settings
        // depend on others...
        createPreferenceHierarchy();

        // ACTION_USB_STATE is sticky so this will call updateToggles
        getActivity().registerReceiver(mStateReceiver,
                new IntentFilter(UsbManager.ACTION_USB_STATE));
        if (mStorageManager != null) {
            mStorageManager.registerListener(mStorageListener);
        }
        updateUsbFunctionState();
    }

    private void updateToggles(String function) {
        if (UsbManager.USB_FUNCTION_MTP.equals(function)) {
            mMtp.setChecked(true);
            mPtp.setChecked(false);
           //mSDCard.setChecked(false);
            mCharging.setChecked(false);
            mPCAdb.setChecked(false); // add by shengweiguang for "pc connect" mode 2015-08-13
        } else if (UsbManager.USB_FUNCTION_PTP.equals(function)) {
            mMtp.setChecked(false);
            mPtp.setChecked(true);
           //mSDCard.setChecked(false);
            mCharging.setChecked(false);
            mPCAdb.setChecked(false); // add by shengweiguang for "pc connect" mode 2015-08-13
        } else if (UsbManager.USB_FUNCTION_MASS_STORAGE.equals(function)) {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
            //mSDCard.setChecked(true);
            mCharging.setChecked(false);
            mPCAdb.setChecked(false); // add by shengweiguang for "pc connect" mode 2015-08-13
        } else if (UsbManager.USB_FUNCTION_CHARGING.equals(function)) {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
            //mSDCard.setChecked(false);
            mCharging.setChecked(true);
            mPCAdb.setChecked(false); // add by shengweiguang for "pc connect" mode 2015-08-13
        } else {
            mMtp.setChecked(false);
            mPtp.setChecked(false);
            //mSDCard.setChecked(false);
            mCharging.setChecked(false);
            mPCAdb.setChecked(true); // add by shengweiguang for "pc connect" mode 2015-08-13
        }
        mIsPcAdbChecked = mPCAdb.isChecked();// add by shengweiguang for "pc connect" mode 2015-08-13
		
         /* Modified by sunyaxi to add a new "pc connect" mode (QL1005B) SW00064026 2014-07-10 end */

        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            Log.e(TAG, "USB is locked down");
            mMtp.setEnabled(false);
            mPtp.setEnabled(false);
           // mSDCard.setEnabled(false);
        } else if (!mUsbAccessoryMode && !operateInprogress) {
            //Enable MTP and PTP switch while USB is not in Accessory Mode, otherwise disable it
            Log.e(TAG, "USB Normal Mode");
            getPreferenceScreen().setEnabled(true);
        } else {
            getPreferenceScreen().setEnabled(false);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        // Don't allow any changes to take effect as the USB host will be disconnected, killing
        // the monkeys
        if (Utils.isMonkeyRunning()) {
            return true;
        }
        // If this user is disallowed from using USB, don't handle their attempts to change the
        // setting.
        UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        if (um.hasUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER)) {
            return true;
        }

        //if choose none, we set the function as the default config
        String function = UsbManager.USB_FUNCTION_DEFAULT;
        if (preference == mMtp && mMtp.isChecked()) {
            function = UsbManager.USB_FUNCTION_MTP;
        } else if (preference == mPtp && mPtp.isChecked()) {
            function = UsbManager.USB_FUNCTION_PTP;
        } else if (preference == mCharging && mCharging.isChecked()) {
            function = UsbManager.USB_FUNCTION_CHARGING;
        }/* else if (preference == mSDCard && mSDCard.isChecked()) {
            function = UsbManager.USB_FUNCTION_MASS_STORAGE;
        }*/
/* Added by shengweiguang to add a new "pc connect" mode 2015-08-13 begin */
		else if (preference == mPCAdb && mPCAdb.isChecked()) {
            function =  UsbManager.USB_FUNCTION_DEFAULT;
        } else if (mIsPcAdbChecked) {
            mPCAdb.setChecked(true);
            return true;
        }
/* Added by shengweiguang to add a new "pc connect" mode 2015-08-13 end */
        operateInprogress = true;
        mUsbManager.setCurrentFunction(function, true);
        updateToggles(function);

        return true;
    }
}