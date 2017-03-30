package com.ehang.services;

import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.ehang.receiver.MyMethodImpil;
import com.ehang.receiver.ScreenBroadcast;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import com.ehang.dbutil.dbImplement;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.provider.Settings;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;


public class phoneAssistantService extends Service {
	
	private String TAG = "DetectService";
	private ArrayList localArrayList;
	long bootTime;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		methods bindMethod = new methods();
		return bindMethod;
	}

	@Override
	public void onCreate() {
		Log.e(TAG,"----oncreate----");
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(new ScreenBroadcast(), filter);
		try {
			AppOpsManager mAppOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_PERMISSIONS);
			mAppOpsManager.setMode(43, packageInfo.applicationInfo.uid,
					getPackageName(), AppOpsManager.MODE_ALLOWED);

		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		bootTime = System.currentTimeMillis();
		super.onCreate();
	}

	@Override
	public boolean stopService(Intent name) {
		// TODO Auto-generated method stub
		unregisterReceiver(new ScreenBroadcast());
		return super.stopService(name);
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		Log.e(TAG,"----onStart----");
		super.onStart(intent, startId);
	}

	public class methods extends Binder implements MyMethodImpil {

		@Override
		public void getServicesMethod() {

			// TODO Auto-generated method stub
			AudioManager audiomanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			boolean isMusicactive = audiomanager.isMusicActive();
			Log.e(TAG,"----isMusicactive----:" + isMusicactive);
			if (isMusicactive) {
				return;
			} else {

				KillAllApp();
			}

		}

	}

	public void KillAllApp() {
		dbImplement dbimplement = new dbImplement();
		List<Map<String, Object>> listName = dbimplement.queryPackageNames(this);
		String runtop_name = getForegroundApp();
		Log.e(TAG,"----runtop_name----:" + runtop_name);
		if (runtop_name == null) {
			return;
		}
		for (Map<String, Object> m : listName) {
			for (String k : m.keySet()) {
				String names = (String) m.get(k);
				if (runtop_name.equals(names)) {

				} else {

					killApp(names);
				}
			}
		}
	}

	private String getForegroundApp() {
		long currenttime = System.currentTimeMillis();
		UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
		List<UsageStats> queryUsageStats = mUsageStatsManager.queryUsageStats(
				UsageStatsManager.INTERVAL_BEST, bootTime, currenttime);
		if (queryUsageStats == null || queryUsageStats.isEmpty()) {
			return null;
		}
		UsageStats recentStats = null;
		for (UsageStats usageStats : queryUsageStats) {
			if (recentStats == null
					|| recentStats.getLastTimeUsed() < usageStats
							.getLastTimeUsed()) {
				recentStats = usageStats;
			}
		}
		return recentStats.getPackageName();
	}

	private void killApp(String name) {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		Method[] methods = activityManager.getClass().getMethods();
		for (Method method : methods) {
			try {
				if (method.getName().equals("forceStopPackage")) {

					try {
						method.invoke(activityManager, name);

					} catch (IllegalAccessException e) {

					} catch (InvocationTargetException e) {

					}
					break;
				}
			} catch (IllegalArgumentException e) {
			}
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}

}
