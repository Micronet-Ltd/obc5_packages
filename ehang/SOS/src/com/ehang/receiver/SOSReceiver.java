package com.ehang.receiver;

import com.ehang.sos.MainUI;
import com.ehang.sos.R;
import com.ehang.sos.Setup_Mod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class SOSReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		Log.d("SOS", "SOSReceiver----onReceive");
		if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
			if (!MainUI.haveSetup){ //haveSetup false, means one: not enter MainUI, two: enter but not setup 
				if (!MainUI.isPreLoc) {//not enter MainUI 
					MainUI.isGpsOpen = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(),LocationManager.GPS_PROVIDER);
				}
				MainUI.openGps(context);
				new Setup_Mod(context).setup();
				MainUI.setup.setText(context.getString(R.string.setuped));
				MainUI.setup.setClickable(false);
			}else{//haveSetup
				Log.d("SOS", "SOSReceiver----MainUI.haveSetup ");
			}
		} else if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {//no card
			Toast.makeText(context, context.getString(R.string.nosim),
					Toast.LENGTH_SHORT).show();
		} else {//error card
			Toast.makeText(context, context.getString(R.string.errsim),
					Toast.LENGTH_SHORT).show();
		}			
	}
}
