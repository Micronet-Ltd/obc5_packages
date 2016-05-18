package com.security.controlrecorder;

import java.util.List;
import java.util.Map;

import com.security.db.shezhi;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneBroadcastReceiver extends BroadcastReceiver {
	private final String TAG = "BootCompleteReceiver";
	Context content;
	List<String> list;
	Main dd;

	public PhoneBroadcastReceiver() {

	}

	public PhoneBroadcastReceiver(Context content, List<String> list) {
		content = content;
		list = list;

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.e(TAG, "BootCompleteReceiver onReceive");
		dd = new Main(context);
		boolean boo = context.getSharedPreferences("switch",
				Context.MODE_PRIVATE).getBoolean("open", false);
		if (!boo)
			return;
		// 如果是拨打电话
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			// 将所有带有录音权限的应用设为禁止
			// dd.start();
			dd.dange();
			// shezhi ss=new shezhi();
			// List<Map<String,Object>> ff=ss.ll(context);
			// for (Map<String, Object> m : ff) {
			// int st=0;
			// for (String k : m.keySet()) {
			// String state= (String) m.get(k);
			// st=Integer.parseInt(state);
			// }
			// if(st==1){
			//
			// }

			// }

			// System.out.println("正在拨打电话，，你要干嘛？");
		} else {
			// 如果是来电
			TelephonyManager tManager = (TelephonyManager) context
					.getSystemService(Service.TELEPHONY_SERVICE);
			switch (tManager.getCallState()) {
			// 响铃的时候不执行任何操作
			case TelephonyManager.CALL_STATE_RINGING:
				dd.dange();
				break;
			// 通话中,不需要任何操作
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// //调用控制权限的方法
				// dd.start();
				// for (int i = 0; i < list2.size(); i++) {
				// String pname=list2.get(i);
				// dd.dange(pname);
				// }

				Toast.makeText(context, "防窃听已开启，请安心通话", Toast.LENGTH_SHORT)
						.show();
				break;
			// 无任何状态
			case TelephonyManager.CALL_STATE_IDLE:
				dd.close();
				//System.err.println("挂断没有，，，");

				break;
			}
		}
	}

}