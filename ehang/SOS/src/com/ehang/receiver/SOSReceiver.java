package com.ehang.receiver;

import com.ehang.sos.MainUI;
import com.ehang.sos.R;
import com.ehang.sos.Setup_Mod;
import com.ehang.sos.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.Message;

public class SOSReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Log.d("SOS", "SOSReceiver----onReceive");
		if (Utils.getInstance(context).hasCard()) {
			if (!MainUI.haveSetup){
				if (!MainUI.isPreLoc) {//not enter MainUI 
					MainUI.isGpsOpen = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(),LocationManager.GPS_PROVIDER);
				}
				Utils.getInstance(context).openGps();
				new Setup_Mod(context).setup();
				MainUI.haveSetup = true;
				
				Message msg = new Message();
				msg.what = 2;
				msg.obj = context.getResources().getString(R.string.setuped);
				MainUI.txHandler.sendMessage(msg);
				
			}else{//haveSetup
				Log.d("SOS", "SOSReceiver----MainUI.haveSetup ");
			}
		}
			
		
		
	}
}
