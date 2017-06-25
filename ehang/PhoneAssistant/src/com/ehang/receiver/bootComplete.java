package com.ehang.receiver;

import java.io.File;
import java.util.List;
import com.ehang.dbutil.PackageMethod;
import com.ehang.dbutil.dbImplement;
import com.ehang.services.phoneAssistantService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import com.ehang.phoneassistant.R;

public class bootComplete extends BroadcastReceiver {
	
	private String TAG = "bootComplete";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Intent in = new Intent(context, phoneAssistantService.class);
		long ts = System.currentTimeMillis();
		Log.e(TAG,"-----bootReceiever----");
		context.startService(in);
		File file = context.getApplicationContext().getDatabasePath("TSwitch.db");
		if(!file.exists()){
		PackageMethod	pm = new PackageMethod(context);
			List list2 = pm.getAppInfos();
			
			for (int i = 0; i < list2.size(); i++) {
				ApplicationInfo localapplicationInfo = (ApplicationInfo) list2
						.get(i);
				String packagename = localapplicationInfo.packageName;
				String[] items =context.getResources().getStringArray(R.array.white_list);
				
				int state = 0;
				Object param[] = { state, packagename };
				dbImplement dbimplement = new dbImplement();
				boolean flag = dbimplement.addData(param, context);
				for (int j = 0; j < items.length; j++) {
					if(packagename.equals(items[j])){
					int states = 1;
					Object params[] = { states, packagename };
					boolean flags = dbimplement.updateData(params, context);
					}
					
					
					
				}
				
			
			}
		}
		
	}

}
