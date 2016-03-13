package com.android.flymode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.SystemProperties;

import com.android.flymode.FlyModeService;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("FlyModeService","received message!!! intent.getAction:" + intent.getAction());
		// TODO Auto-generated method stub
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			Log.d("FlyModeService","received ACTION_BOOT_COMPLETED message!!!");
			Intent Intent = new Intent(context,FlyModeService.class);
			context.startService(Intent);
		}	
	}	
}
