package com.yihang.gesture.receiver;

import com.yihang.gesture.service.ServerManager;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver {

	private final String TAG = "BootCompleteReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e(TAG, "BootCompleteReceiver onReceive");
		ContentResolver contentResolver=context.getContentResolver();  
	    Uri uri=Uri.parse("content://com.yihang.gesture.provider/switchstate"); 
	    Cursor cursor=contentResolver.query(uri, null, null, null, null);
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
	}
}
