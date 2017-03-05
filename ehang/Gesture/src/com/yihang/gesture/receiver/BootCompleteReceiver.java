package com.yihang.gesture.receiver;

import com.yihang.gesture.provider.Profile;
import com.yihang.gesture.provider.gestureProvider;
import com.yihang.gesture.service.ServerManager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.os.UserHandle;
public class BootCompleteReceiver extends BroadcastReceiver {

	private final String TAG = "BootCompleteReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		int userId = intent.getIntExtra("com.securespaces.android.intent.EXTRA_USER_HANDLE", -1);
		Log.d(TAG, "BootCompleteReceiver onReceive"+intent+"    id    "+UserHandle.myUserId()+"    user=    "+userId);
		if(userId!=-1&&userId!=UserHandle.myUserId()){
			return;
		}
		ContentResolver contentResolver=context.getContentResolver();  
	    Cursor cursor=contentResolver.query(gestureProvider.GESTURE_URI , null, null, null, null);
	    while (cursor.moveToNext()) {  
	    	if(cursor.getInt(0) == 1) {
	    		Intent service = new Intent(context, com.yihang.gesture.service.GestureService.class);
				context.startService(service);
				ServerManager.getInstance().Opentp();
				Log.e(TAG, "cursor.getInt(0) == 1"+"ServerManager.Open_tp");
			}else {
				ServerManager.getInstance().Closetp();
				Log.e(TAG, "cursor.getInt(0) != 1"+"ServerManager.Close_tp");
			}
	    }
	    cursor.close();  
	    
	    Profile profile = Profile.getInstance(context);
	    int state = profile.getGloveSwitchstate();
	    if(state == 1){
	    	ServerManager.getInstance().OpenGloveMode();
	    }else{
	    	ServerManager.getInstance().CloseGloveMode();
	    }
	}
}
