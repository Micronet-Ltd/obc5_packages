package com.ehangcompass;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import android.app.Application;
import android.app.Service;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;


public class LocationApplication extends Application {
	private static final String TAG = "LocationApplication";
    public LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;

    public TextView mLocationResult,logMsg;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        
        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        Log.e(TAG, "appli_registerListener");
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
			String addressOfGps = null;
            String addressOfNetwork = null;
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();

			sb.append(getString(R.string.location_longitude_latitude));
			if (latitude >= 0.0f) {
				sb.append(getString(R.string.location_north,
						getLocationString(latitude)));
			} else {
				sb.append(getString(R.string.location_south,
						getLocationString(-1.0 * latitude)));
			}

			sb.append("    ");
			if (longitude >= 0.0f) {
				sb.append(getString(R.string.location_east,
						getLocationString(longitude)));
			} else {
				sb.append(getString(R.string.location_west,
						getLocationString(-1.0 * longitude)));
			}

            
            if (location.getLocType() == BDLocation.TypeGpsLocation){
            	Log.e(TAG, "listenning_netType = TypeGpsLocation");
            	addressOfGps = location.getAddrStr();

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
            	Log.e(TAG, "listenning_netType = TypeNetWorkLocation");
            	addressOfNetwork = location.getAddrStr();        
                
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                sb.append("\n" + getString(R.string.location_hint));
                sb.append(getString(R.string.location_error1));
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\n" + getString(R.string.location_error));
                sb.append(getString(R.string.location_error2));
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\n" + getString(R.string.location_error));
                sb.append(getString(R.string.location_error3));
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\n" + getString(R.string.location_error));
                sb.append(getString(R.string.location_error4));
            }
            sb.append("\n" + getString(R.string.location_address));
            if(addressOfGps != null && addressOfGps.length() != 0) {
            	sb.append(addressOfGps);
            } else if(addressOfNetwork != null && addressOfNetwork.length() != 0) {
            	sb.append(addressOfNetwork);
            } else {
            	sb.append(getString(R.string.location_none));
            }
            
            logMsg(sb.toString());
            Log.i("onReceiveLocation", sb.toString());
        }
    }

	private String getLocationString(double input) {
		int degree = (int) input;
		int minute = (((int) ((input - degree) * 3600))) / 60;
		int second = (((int) ((input - degree) * 3600))) % 60;
		return String.valueOf(degree) + "\u00B0" + String.valueOf(minute) + "'"
				+ String.valueOf(second) + "\"";
	}


    public void logMsg(String str) {
        try {
            if (mLocationResult != null)
                mLocationResult.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
