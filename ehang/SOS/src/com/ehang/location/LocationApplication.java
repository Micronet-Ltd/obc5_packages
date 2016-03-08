package com.ehang.location;

import java.util.Date;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.ehang.sos.MainUI;
import com.ehang.sos.R;
import com.ehang.sos.Setup_Mod;

import android.app.Application;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class LocationApplication extends Application {

	public String TAG = "SOS";
	public LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;

	public String longitude, latitude, address, netLong, netLati, netAddr, lac, cid;
	public TextView mLocationResult;
	public Boolean fromGps = false;
	String btnString;

	public void onCreate() {
		super.onCreate();
		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		
		longitude = "";
		latitude = "";
		address = "";
		netAddr = "";
		netLong = "";
		netLati = "";

	}

	class MyLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.d(TAG, "location listening......  location.getLocType()= " + location.getLocType());
			//61-TypeGpsLocation, 62\63\66\67-networkFalse, 161-TypeNetWorkLocation, 162-noLib.so, 166-keyNotAvailuable
			//167-noPermission, 501~700-keyfalse
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				
				longitude = Double.toString(location.getLongitude());
				latitude = Double.toString(location.getLatitude());
				address = location.getAddrStr();

				if (setupStatu() && longitude != null && longitude != ""
						&& !longitude.equals("4.9E-324") && latitude != null
						&& latitude != "" && !latitude.equals("4.9E-324")) {
					fromGps = true;//use gps location data first if gps available
					mLocationClient.stop();
					
					Thread LooperThread = new Thread(new Runnable() {
						public void run() {
							Looper.prepare();
							Message message = new Message();
							message.what = 2;
							new Setup_Mod(getApplicationContext()).mHandler.sendMessage(message);// send gps_addr msg
							Looper.loop();
						}
					});
					LooperThread.start();
				}
				
				Log.d(TAG, "gps_location: longi=" + longitude + " addr=" + address);

			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {//TypeNetWorkLocation==161
				netLong = Double.toString(location.getLongitude());
				netLati = Double.toString(location.getLatitude());
				netAddr = location.getAddrStr();
				Log.d(TAG, "net_location: netLati= " + netLati + "  netAddr= " + netAddr);
			} else try {
					Log.d(TAG, "neither  gps_location  nor  net_location");
					//67:offline locationing failed
				} catch (Exception e) {
					// such as network weak,enable, and gps weak;
					e.printStackTrace();
					Log.e(TAG, "Exception: can't listening ", e);
				}

		}

	}

	public Boolean setupStatu() {
		Boolean stat = new MainUI().setup.getText().toString()
				.equals(getString(R.string.setuped));
		return stat;
	}

}
