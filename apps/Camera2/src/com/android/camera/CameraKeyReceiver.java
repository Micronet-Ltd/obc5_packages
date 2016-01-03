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

public class CameraKeyReceiver extends BroadcastReceiver {
    Context mContext;
	@Override
    public void onReceive(Context context, Intent intent) {
		mContext = context;
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
		boolean isScreenOn = pm.isScreenOn();
		if (isScreenOn) {
			if (getLollipopRecentTask().equals(mContext.getPackageName())) {
		    }else {
				KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
				boolean isRestrictedKey = mKeyguardManager.inKeyguardRestrictedInputMode();
				boolean isSecure = mKeyguardManager.isKeyguardSecure();
				Bundle b = new Bundle();
				b.putBoolean("onKeyguard", true);
				if (isRestrictedKey & isSecure){
					Intent i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
					i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtras(b);
					context.startActivity(i);
				} else {
					Intent it = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
					it.putExtras(b);
					it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
