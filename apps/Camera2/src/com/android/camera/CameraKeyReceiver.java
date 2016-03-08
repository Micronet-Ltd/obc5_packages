package com.android.camera;

import java.lang.reflect.Field;
import java.util.List;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

public class CameraKeyReceiver extends BroadcastReceiver {
    Context mContext;
	String TAG = "CameraKeyReceiver";
	public static String KEYGUARD_LOCKED = "KEYGUARD_LOCKED";
	@Override
    public void onReceive(Context context, Intent intent) {
		mContext = context;
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
		boolean isScreenOn = pm.isScreenOn();
		
		Log.d(TAG, "------CameraKeyReceiver--isScreenOn = " + isScreenOn);
		if (isScreenOn) {
			KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			boolean isLocked = mKeyguardManager.isKeyguardLocked();
			boolean isSecure = mKeyguardManager.isKeyguardSecure();
			if (getLollipopRecentTask().equals(mContext.getPackageName())) {
				if (isLocked){
					Intent mIntent = new Intent("CAMERA_SHOW_WHEN_LOCKED"); 
					context.sendBroadcast(mIntent);
					Log.d(TAG, "------getLollipopRecentTask");
				}
			}else {
				if (isLocked && isSecure){
					Log.d(TAG, "----Open SecureCamera");
					Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
					i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
					context.startActivity(i);
				} else {
					Log.d(TAG, "----Open GeneralCamera");
					Intent it = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
					it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
					if(isLocked){
						it.putExtra(CameraKeyReceiver.KEYGUARD_LOCKED, true);
					}
					context.startActivity(it);
					
				}
		    }
		}
    }

	private String getLollipopRecentTask() {
		final int PROCESS_STATE_TOP = 2;
		try {
			Field processStateField = ActivityManager.RunningAppProcessInfo.class
					.getDeclaredField("processState");
			List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager) mContext
					.getSystemService(Context.ACTIVITY_SERVICE))
					.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo process : processes) {
				if (process.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
						&& process.importanceReasonCode == 0) {
					int state = processStateField.getInt(process);
					if (state == PROCESS_STATE_TOP) {
						String[] packname = process.pkgList;
						return packname[0];
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return "";
	}
}
