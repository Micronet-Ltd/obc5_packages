package com.yihang.gesture.service;

import java.util.ArrayList;
import java.util.List;

import com.yihang.gesture.R;
import com.yihang.gesture.provider.Profile;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class GestureService extends Service {

	private final String TAG = "GestureService";
	private Listener lis;
	private int Gesture = 1;
	private ServerManager servermanager;
	ArrayList<Integer> allow_code = new ArrayList<Integer>();
	private boolean allow_flag = false;

	private void wakeUpScreen() {
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
		wl.acquire();
		wl.release();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (getResources().getBoolean(R.bool.light_screen)) {
			allow_code.add(36);
		}
		if (getResources().getBoolean(R.bool.open_camera)) {
			allow_code.add(52);
		}
		if (getResources().getBoolean(R.bool.open_counter)) {
			allow_code.add(50);
		}
		servermanager = ServerManager.getInstance();
		Log.d(TAG, "Service onCreate");
		lis = new Listener();
		Thread t = new Thread(lis);
		t.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Service onDestroy");
		lis.stop();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "Service onStart");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Service onStartCommand");
		//flags = Service.START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private boolean isRunningTop() { 
		ActivityManager activityManager = (ActivityManager)getSystemService(android.content.Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = activityManager
				.getRunningTasks(1);
		if (runningTaskInfos != null) {
			ComponentName f = runningTaskInfos.get(0).topActivity;
			return f.getClassName().startsWith(this.getPackageName());
		}
		return false;
	}

	class Listener implements Runnable {
		boolean t = true;

		public void run() {
			while (t) {
				try {
					Gesture = servermanager.GetGesture();
					for (int i = 0; i < allow_code.size(); i++) {
						if (Gesture == allow_code.get(i)) {
							allow_flag = true;
							break;
						}
					}
					
					if (allow_flag) {
						Log.d(TAG, "Gesture = " + Gesture + " is defined");
						Profile pro = Profile.getInstance(getBaseContext());
						if (pro.getSwitchstate() == 1) {
							if (Gesture == 36) {
								wakeUpScreen();
							} else {
								Intent i = new Intent();
								i.setClass(getBaseContext(), com.yihang.gesture.activity.ShowGesture.class);
								i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
								i.putExtra("code", Gesture);
								i.putExtra("isRunningTop", isRunningTop());
								startActivity(i);
							}
						}
						allow_flag = false;
					} else {
						Log.d(TAG, "Gesture = " + Gesture + " is not defined");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void stop() {
			t = false;
		}
	}
}