/*
 * Copyright (C) 2013 The Android Open Source Project
 * Copyright (C) 2013, The Linux Foundation. All rights reserved.
 * Not a Contribution.
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

package com.android.settings.location;

import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.preference.SwitchPreference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import android.view.View;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.android.location.XT.IXTSrv;
import com.android.location.XT.IXTSrvCb;
import com.android.location.XT.IXTSrvCb.Stub;
import android.text.Html;
import android.content.Intent;
import android.util.Log;
import com.android.settings.Utils;
import android.content.res.Resources;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;

import com.android.settings.R;

/**
 * A page with 3 radio buttons to choose the location mode.
 *
 * There are 3 location modes when location access is enabled:
 *
 * High accuracy: use both GPS and network location.
 *
 * Battery saving: use network location only to reduce the power consumption.
 *
 * Sensors only: use GPS location only.
 */
public class LocationMode extends LocationSettingsBase
        implements RadioButtonPreference.OnClickListener {
    private static final String KEY_HIGH_ACCURACY = "high_accuracy";
    private RadioButtonPreference mHighAccuracy;
    private static final String KEY_BATTERY_SAVING = "battery_saving";
    private RadioButtonPreference mBatterySaving;
    private static final String KEY_SENSORS_ONLY = "sensors_only";
    private RadioButtonPreference mSensorsOnly;
    private WrappingIZatSwitchPreference mIZat;
    private static final String KEY_LOCATION_IZAT = "location_izat";
    private static final String KEY_ENHANCED_LOCATION = "enhanced_location";
    private static final String TAG = "LocationMode";

    private static final int IZat_MENU_TEXT = 0;
    private static final int IZat_SUB_TITLE_TEXT = 1;
    private static final int POPUP_BOX_DISAGREE = 0;
    private static final int POPUP_BOX_AGREE = 1;
    private static final int PRINT = 1;

    private IXTSrv mXTService = null;
    private XTServiceConnection mServiceConn = null;
    //This variable is used to record the IZat service connection result
    private boolean izatConnResult = false;
    private boolean IsDialogUp = false;
    private LocationManager mLocationMgr;
    private LocListener mListener;

    //This is the IZat handler
    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case PRINT:
                    if(null != mIZat) {
                        if(POPUP_BOX_DISAGREE == msg.arg1){
                            mIZat.setChecked(false);
                        }else if(POPUP_BOX_AGREE == msg.arg1){
                            mIZat.setChecked(true);
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private IXTSrvCb mCallback = new IXTSrvCb.Stub() {
        public void statusChanged(boolean status) {
            IsDialogUp = false;
            if(false == status)
            {
                mHandler.sendMessage(mHandler.obtainMessage(PRINT, 0, 0));
            }else
            {
                mHandler.sendMessage(mHandler.obtainMessage(PRINT, 1, 0));
            }
        }
    };

    /**
     * Bind Izat service
     */
    private void initUserPrefService(){
        mServiceConn = new XTServiceConnection();
        Intent i = new Intent(IXTSrv.class.getName());
        i.setPackage("com.qualcomm.location.XT");
        izatConnResult = getActivity().bindService(i, mServiceConn, Context.BIND_AUTO_CREATE);
    }
///////////
    private class LocListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }
    
////////////    
    /**
     * IZat service connection
     */
    private class XTServiceConnection implements ServiceConnection{
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            mXTService = IXTSrv.Stub.asInterface((IBinder)service);
            Log.d(TAG, "onServiceConnected, service=" + mXTService);
            try{
                if(null != mXTService){
                    String izatMenuTitle = mXTService.getText(IZat_MENU_TEXT);
                    String izatSubtitle = mXTService.getText(IZat_SUB_TITLE_TEXT);
                    if(null != mIZat){
                        mIZat.setTitle(izatMenuTitle);
                        mIZat.setSummary(Html.fromHtml(izatSubtitle));
                    }
                    mXTService.registerCallback(mCallback);
                }
            }catch(RemoteException e){
                Log.d(TAG,"Failed connecting service!");
            }
          }

        @Override
        public void onServiceDisconnected(ComponentName name){
            if (null != mXTService){
            try {
                mXTService.unregisterCallback(mCallback);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
                mXTService = null;
            }
        }
    }
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if(Utils.IsAutoEnableIZat()){
			mListener = new LocListener();	
	        mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	        if(mLocationMgr != null) {
		        //mLocationMgr.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_xtra_injection", null);
	            mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, mListener);
                Log.d(TAG, "TTFFstart");
	        }
        	initUserPrefService();
			createPreferenceHierarchy();
			if(Utils.IsAutoEnableIZat()){
				Log.d(TAG, "auto SET LOCATION_MODE_HIGH_ACCURACY");
				setLocationMode(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
	            mHighAccuracy.setChecked(true);
	            mBatterySaving.setChecked(false);
	            mSensorsOnly.setChecked(false);
			}
			
		}
	}


    @Override
    public void onStart() {
        super.onStart();
		if(!Utils.IsAutoEnableIZat()){
        	initUserPrefService();
		}
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mServiceConn);
    }

    @Override
    public void onResume() {
        super.onResume();
		if(!Utils.IsAutoEnableIZat()){
        	createPreferenceHierarchy();
		}
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.location_mode);
        root = getPreferenceScreen();

        mHighAccuracy = (RadioButtonPreference) root.findPreference(KEY_HIGH_ACCURACY);
        mBatterySaving = (RadioButtonPreference) root.findPreference(KEY_BATTERY_SAVING);
        mSensorsOnly = (RadioButtonPreference) root.findPreference(KEY_SENSORS_ONLY);
        mHighAccuracy.setOnClickListener(this);
        mBatterySaving.setOnClickListener(this);
        mSensorsOnly.setOnClickListener(this);

        PreferenceCategory enhancedLocation = (PreferenceCategory)
        root.findPreference(KEY_ENHANCED_LOCATION);
        mIZat = (WrappingIZatSwitchPreference) root.findPreference(KEY_LOCATION_IZAT);
        if(!izatConnResult){
            root.removePreference(enhancedLocation);
        }else{
            try{
                if (null != mXTService){
                    String izatMenuTitle = mXTService.getText(IZat_MENU_TEXT);
                    String izatSubtitle = mXTService.getText(IZat_SUB_TITLE_TEXT);
                    if(null != mIZat) {
                        mIZat.setTitle(izatMenuTitle);
                        mIZat.setSummary(Html.fromHtml(izatSubtitle));
                        mIZat.setChecked(mXTService.getStatus());
                    }
                }
             }catch(RemoteException e){
                 Log.d(TAG, "Service connection error!");
             }
        }
        if(null != mIZat){
            mIZat.setOnPreferenceChangeListener(
                new Preference.OnPreferenceChangeListener(){
                    public boolean onPreferenceChange(Preference preference, Object newValue){
                        final boolean chooseValue = (Boolean) newValue;
                        if(chooseValue){
                            try{
									if(null != mXTService){
										Log.d(TAG, "mXTService.showDialog()");
										mXTService.showDialog();
										IsDialogUp = true;
                                    }
                                }catch(RemoteException e){
                                    e.printStackTrace();
                                }
                        }else{
                            try{
                                if(null != mXTService){
                                    mXTService.disable();
                                }
                            }catch(RemoteException e){
                                e.printStackTrace();
                            }
                        }
                        return true;
                    }
                }
            );
        }
		refreshLocationMode();
		if(Utils.IsAutoEnableIZat()){
			new Thread(new Runnable() {
				public void run() {
					try {
						if(!mIZat.isChecked()){
							Thread.sleep(100);
							int xcd = Resources.getSystem().getDisplayMetrics().widthPixels;
					        int ycd = Resources.getSystem().getDisplayMetrics().heightPixels;
							Log.d(TAG, "auto tap izat - display x " + xcd + " y " + ycd);
							//ShellUtils.execCommand("input tap 148 496", false);
							if(IsDialogUp == false) {
								ShellUtils.execCommand("input tap " + (xcd * 7 / 8) + " " + (ycd * 2 / 3), false); 
								Thread.sleep(1000);
							}
							Log.d(TAG, "auto tap izat dialog " + (xcd * 7 / 8) + " " + (ycd * 2 / 3));
							ShellUtils.execCommand("input tap " + (xcd * 7 / 8) + " " + (ycd * 2 / 3), false); 
							//Thread.sleep(1000);
							//ShellUtils.execCommand("input tap 622 708", false);
//							400 491
						}
						Thread.sleep(100);
//						Log.d(TAG, "auto SET LOCATION_MODE_HIGH_ACCURACY");
//						setLocationMode(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
					
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				 }
			}).start();
		}
        return root;
    }

    private void updateRadioButtons(RadioButtonPreference activated) {
        if (activated == null) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(false);
        } else if (activated == mHighAccuracy) {
            mHighAccuracy.setChecked(true);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(false);
        } else if (activated == mBatterySaving) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(true);
            mSensorsOnly.setChecked(false);
        } else if (activated == mSensorsOnly) {
            mHighAccuracy.setChecked(false);
            mBatterySaving.setChecked(false);
            mSensorsOnly.setChecked(true);
        }
    }

    @Override
    public void onRadioButtonClicked(RadioButtonPreference emiter) {
        int mode = Settings.Secure.LOCATION_MODE_OFF;
        if (emiter == mHighAccuracy) {
            mode = Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
        } else if (emiter == mBatterySaving) {
            mode = Settings.Secure.LOCATION_MODE_BATTERY_SAVING;
        } else if (emiter == mSensorsOnly) {
            mode = Settings.Secure.LOCATION_MODE_SENSORS_ONLY;
        }
        setLocationMode(mode);
    }

    @Override
    public void onModeChanged(int mode, boolean restricted) {
        switch (mode) {
            case Settings.Secure.LOCATION_MODE_OFF:
                updateRadioButtons(null);
                break;
            case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                updateRadioButtons(mSensorsOnly);
                break;
            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                updateRadioButtons(mBatterySaving);
                break;
            case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                updateRadioButtons(mHighAccuracy);
                break;
            default:
                break;
        }

        boolean enabled = (mode != Settings.Secure.LOCATION_MODE_OFF) && !restricted;
        mHighAccuracy.setEnabled(enabled);
        mBatterySaving.setEnabled(enabled);
        mSensorsOnly.setEnabled(enabled);
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_location_access;
    }
}


    class WrappingIZatSwitchPreference extends SwitchPreference {

        public WrappingIZatSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public WrappingIZatSwitchPreference(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onBindView(View view) {
            super.onBindView(view);
            TextView title = (TextView) view.findViewById(android.R.id.title);
            if (title != null) {
                title.setSingleLine(false);
                title.setMaxLines(3);
            }
        }
}
