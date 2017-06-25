package com.ehang.receiver;

import com.ehang.services.phoneAssistantService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.os.IBinder;


public class ScreenBroadcast extends BroadcastReceiver {
	
	private String TAG = "ScreenBroadcast";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		// TODO Auto-generated method stub
		if (intent.getAction().equals(intent.ACTION_SCREEN_OFF)) {
			//final boolean boo = context.getSharedPreferences("switch",
			//		Context.MODE_PRIVATE).getBoolean("open", false);
			Log.e(TAG,"----ACTION_SCREEN_OFF----");
			Intent mservices = new Intent(context, phoneAssistantService.class);
			//context.startService(in);
			ServiceConnection sc=new ServiceConnection() {
				
				@Override
				public void onServiceDisconnected(ComponentName name) {
				
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					
					MyMethodImpil	mbinder=(MyMethodImpil)service;
					Log.e(TAG, "----mbinder.getServicesMethod----");
					mbinder.getServicesMethod();		
				}
			};
			context.bindService(mservices, sc, context.BIND_AUTO_CREATE);

		}
	}
}
