package com.ehang.location;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.ehang.sos.MainUI;
import com.ehang.sos.Setup_Mod;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

public class Location {
	Context context;

	public Location(Context ct) {
		context = ct;
		mLocationClient = ((LocationApplication) context.getApplicationContext()).mLocationClient;
		mAppli = (LocationApplication) context.getApplicationContext();
	}

	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "gcj02";
	LocationClient mLocationClient;
	LocationApplication mLocationApplication;
	Handler handler;
	int runCount;
	LocationApplication mAppli;
	String lac, cid;

	public void initLocation() {
		
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);
		option.setCoorType(tempcoor);
		int span = 1000; // location frequency

		option.setPriority(LocationClientOption.GpsFirst);
		option.setScanSpan(span);
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setLocationNotify(false);
		option.setIgnoreKillProcess(true);
		option.setEnableSimulateGps(false);
		option.disableCache(false);//adding
		option.setIsNeedLocationDescribe(false);
		option.setIsNeedLocationPoiList(false);
		mLocationClient.setLocOption(option);

		mAppli.longitude = "";
		mAppli.latitude = "";
		mAppli.address = "";
		mAppli.netLong = "";
		mAppli.netLati = "";
		mAppli.netAddr = "";
		mLocationClient.start();

	}

	public void enableGpsFirst() {// We pick the location by network if gps haven't geted location after some times
		handler = new Handler();
		Runnable runnable = new Runnable() {
			public void run() {

				if (!((LocationApplication) context.getApplicationContext()).fromGps) {//network location result
					Log.d("SOS", "enableGpsFirst---fromGps = false ");
					mLocationClient.stop();
					Message message = new Message();
					message.what = 2;
					mAppli.longitude = mAppli.netLong;
					mAppli.latitude = mAppli.netLati;
					mAppli.address = mAppli.netAddr; 
					new Setup_Mod(context).mHandler.sendMessage(message);// send gps_addr msg
				} else {
					((LocationApplication) context.getApplicationContext()).fromGps = false;
				}

			}
		};
		handler.postDelayed(runnable, 90000);
	}
	

}
