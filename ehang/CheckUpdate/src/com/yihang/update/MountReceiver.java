package com.yihang.update;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class MountReceiver extends BroadcastReceiver {
	private String TAG ="com.yihang.update";
	private Context mContext;
	private int CHECK_UPDATE_PACKAGE = 1;
	private String sdRoot = "/storage/sdcard1";
	private String UPDATEP_PACKAGE_PATH =  sdRoot+"/update.zip";
	private String UPDATEP_PACKAGE_PATH_FIXED = sdRoot+"/update_fixed.zip";
	private String UPDATE_ACTION = "android.intent.action.AdupsFota.WriteCommandReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "-----onReceive intent ="+intent.toString());
		mContext = context;
		handler.sendEmptyMessage(CHECK_UPDATE_PACKAGE);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.d(TAG, "-----handleMessage");
			if (hasUpdatePackage()) {
				Log.d(TAG, "-----found update package");
				Intent intent = new Intent(UPDATE_ACTION);
				intent.putExtra("PackageFileName", UPDATEP_PACKAGE_PATH_FIXED);
				mContext.sendBroadcast(intent);
			}else{
				Log.d(TAG, "-----not found update package.");
			}
		}
	};
	
	private boolean hasUpdatePackage(){
		try{
			File f = new File(UPDATEP_PACKAGE_PATH);
			if(f.exists()){
				File fixedFile=new File(UPDATEP_PACKAGE_PATH_FIXED); 
				return f.renameTo(fixedFile);
			}
		}catch(Exception e){
			Log.e(TAG, "Check update package error. " + e.toString());
			return false;
		}
		return false;
	}
}
