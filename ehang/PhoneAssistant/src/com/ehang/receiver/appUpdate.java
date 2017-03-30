package com.ehang.receiver;

import java.util.List;
import com.ehang.phoneassistant.R;
import com.ehang.dbutil.dbImplement;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.BroadcastReceiver;

public class appUpdate extends BroadcastReceiver {
	
	private String TAG = "appUpdate";

	@Override
	public void onReceive(Context context, Intent intent) {
		dbImplement dbimplement = new dbImplement();
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			Log.e(TAG,"----intent.action.PACKAGE_ADDED----");
			String packageName = intent.getDataString();
			String[] strs = packageName.split("[:]");
			String pname = strs[1];
			int state = 0;
			Object param[] = { state, pname };
			boolean flag = dbimplement.addData(param, context);
			String[] items = context.getResources().getStringArray(R.array.white_list);
			for (int i = 0; i < items.length; i++) {
				if(pname.equals(items[i])){
				int states= 1;
				Object params[] = { states, pname };
				boolean flags = dbimplement.updateData(params,context);
				}
			}
			
		}
		
		if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
			Log.e(TAG,"----intent.action.PACKAGE_REMOVED----");
			String packageName = intent.getDataString();
			String[] strs = packageName.split(":");
			String pname = strs[1];
			Object param1[] = { pname };
			boolean flag1 = dbimplement.deleteData(param1, context);
			
		}
	}
  

}
