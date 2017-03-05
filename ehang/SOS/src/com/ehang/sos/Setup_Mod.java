package com.ehang.sos;

import java.util.List;

import com.ehang.location.Location;
import com.ehang.location.LocationApplication;
import com.baidu.location.LocationClient;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

public class Setup_Mod {
	String TAG = "SOS";
	String num1, num2, num3, num4, num5, lac, cid, preMmsMsg, longitude,
			latitude, address;
	String nums[] = { num1, num2, num3, num4, num5 };
	String msgString, cellNum, gpsAddr;
	TelephonyManager tm;
	GsmCellLocation gsmCell;
	String netOperator;
	Context context;
	Location myLocationActivity;
	LocationClient mLocationClient;
	LocationApplication mAppli;

	public Setup_Mod(Context c) {
		context = c;
	}

	public void initNumMms() {
		num1 = Storage.getInstance().getoutStorage(context, "num1");
		num2 = Storage.getInstance().getoutStorage(context, "num2");
		num3 = Storage.getInstance().getoutStorage(context, "num3");
		num4 = Storage.getInstance().getoutStorage(context, "num4");
		num5 = Storage.getInstance().getoutStorage(context, "num5");
		preMmsMsg = Storage.getInstance().getoutStorage(context, "preMmsMsg");
	}

	public void setup() {
		new CellNum(context).GetCellnum();// geted cell_num
		MainUI.isPreLoc = false;
		MainUI.haveSetup = true;
		new Location(context).initLocation();// start listening location
		new Location(context).enableGpsFirst();

	}

	public void sendCell_Num() {
		initNumMms();
		String nums[] = { num1, num2, num3, num4, num5 };
		mAppli = (LocationApplication) context.getApplicationContext();
		lac = mAppli.lac;
		cid = mAppli.cid;

		cellNum = context.getResources().getString(R.string.cellnum) + "\n" + "lac: "
				+ lac + " cid: " + cid;
		msgString = preMmsMsg + "\n" + cellNum + "\n";
		for (int i = 0; i < nums.length; i++) {// send to existing contacts list
			sendmsg(nums[i], msgString);
		}
		Log.d(TAG, "--------------------\n" +  "have send_Cellnum: " + msgString + "\n--------------------");

	}

	public void sendGpsAddr() {

		initNumMms();
		String nums[] = { num1, num2, num3, num4, num5 };
		mAppli = (LocationApplication) context.getApplicationContext();
		longitude = mAppli.longitude;
		latitude = mAppli.latitude;
		address = mAppli.address;
		
		if (longitude != null && longitude != ""
				&& !longitude.equals("4.9E-324") && latitude != null
				&& latitude != "" && !latitude.equals("4.9E-324")) {
			gpsAddr = context.getResources().getString(R.string.longi) + longitude
					+ "\n" + context.getResources().getString(R.string.lati)
					+ latitude;
			if (address != null && address != "") {
				gpsAddr = gpsAddr + "\n"
						+ context.getResources().getString(R.string.addr) + address;
			}
			msgString = preMmsMsg + "\n" + gpsAddr;

			for (int i = 0; i < nums.length; i++) {// send to existing contacts list
				sendmsg(nums[i], msgString);
			}
			Log.d(TAG, "--------------------\n" + "have sendGpsAddr: " + msgString + "\n--------------------");
		}

		// showing setup words and clickable
		Thread LooperThread = new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				Message message = new Message();
				message.what = 1;
				MainUI.txHandler.sendMessage(message);
				Looper.loop();
			}
		});
		LooperThread.start();
		
		Utils.getInstance(context).closeGps();
	}

	public void sendmsg(String num, String msg) {
		if (!num.equals("")) {
			SmsManager smsManager = SmsManager.getDefault();
			msg = context.getResources().getString(R.string.thisText) + "\n" + msg;

			if (msg.length() > 70) {
				List<String> contents = smsManager.divideMessage(msg);
				for (String sms : contents) {
					smsManager.sendTextMessage(num, null, sms, null, null);
				}
				MainUI.sendedVibrator(context);
			} else if (msg.equals("")) {// no msg
			} else {// short msg
				smsManager.sendTextMessage(num, null, msg, null, null);
				MainUI.sendedVibrator(context);
			}
		}

	}

	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				sendCell_Num();
				break;
			case 2:
				mLocationClient = ((LocationApplication) context.getApplicationContext()).mLocationClient;
				mLocationClient.stop();
				sendGpsAddr();
				Utils.getInstance(context).closeGps();
				
				MainUI.isPreLoc = false;
				MainUI.haveSetup = false;
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	

}
