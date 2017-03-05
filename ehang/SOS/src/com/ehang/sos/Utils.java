package com.ehang.sos;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.provider.Settings;
import android.content.Intent;

public class Utils {
	
	Context context;
	public Utils(Context c){
		context = c;
	}
	
	private static Utils instance = null;

	public static Utils getInstance(Context ct) {
		if (instance == null) {
			instance = new Utils(ct);
		}
		return instance;
	}
	
	public Boolean hasCard(){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
			return true;
		} else if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {//no card
			Toast.makeText(context, context.getString(R.string.nosim),Toast.LENGTH_SHORT).show();
			return false;
		} else {//error card
			Toast.makeText(context, context.getString(R.string.errsim),Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	public void openGps() {
			Log.d("SOS", "gpsTurnOn--openGps()");
			int mode = android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
			Intent mIntent = new Intent("com.android.settings.location.MODE_CHANGING");
			mIntent.putExtra("NEW_MODE", mode);
			context.sendBroadcast(mIntent,android.Manifest.permission.WRITE_SECURE_SETTINGS);
			Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, mode);
	}

	public void closeGps() {
		Log.d("SOS", "closeGps()---isGpsOpen= " + MainUI.isGpsOpen);
		if (MainUI.isGpsOpen == false) {
			Log.d("SOS", "gpsTurnDown--closeGps()");
			int modeOff = android.provider.Settings.Secure.LOCATION_MODE_OFF;
	        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
	        intent.putExtra("NEW_MODE", modeOff);
	        context.sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
	        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, modeOff);
		}
	}
	
	

}
