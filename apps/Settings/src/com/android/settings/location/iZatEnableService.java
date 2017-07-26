package com.android.settings.location;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;

import com.android.settings.Utils;
import android.content.res.Resources;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;

public class iZatEnableService extends Service {
    private static String TAG = "iZatEnabler";
    private LocationManager mLocationMgr;
    private LocListener 	mListener;

	@Override
    public void onCreate() {
        if(!Utils.IsAutoEnableIZat()){
        	stopSelf();
        	return;
        }	
        try{    		
          	   mListener = new LocListener();	
          	   mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
          	   if(mLocationMgr != null) {
          		   //mLocationMgr.sendExtraCommand(LocationManager.GPS_PROVIDER, "force_xtra_injection", null);
          		   mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, mListener);
          		   Log.d(TAG, "TTFF LocationListener start");
          	   }
          	   
          	   ContentResolver cr = getContentResolver();
          	   
          	   if(1 != Settings.Global.getInt(cr, Settings.Global.ASSISTED_GPS_ENABLED, 2))
          			Settings.Global.putInt(cr, Settings.Global.ASSISTED_GPS_ENABLED, 1);
          	   
          	   int mode = Settings.Secure.getInt(cr, Settings.Secure.LOCATION_MODE);
          	   //Log.d(TAG, "TTFF mode" + mode);//temp!!!
          	   if(Settings.Secure.LOCATION_MODE_HIGH_ACCURACY != mode){

          		   Settings.Secure.putInt(cr, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);

          		   new Thread(new Runnable() {
          			   public void run() {
          			   try {
      					   int xcd = Resources.getSystem().getDisplayMetrics().widthPixels;
      					   int ycd = Resources.getSystem().getDisplayMetrics().heightPixels;
      					   
      					   Log.d(TAG, "TTFF tap izat " + (xcd * 7 / 8) + " " + (ycd * 2 / 3) + " display x=" + xcd + " y=" + ycd);
      					   
      					   ActivityManager AcivityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
						   Instrumentation inst = new Instrumentation();

						   for(int i = 0; i < 30; ++i){
							
          					   String currentPackageName = AcivityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
          							 
          					   Log.d(TAG, "TTFF " + currentPackageName);
          					   Thread.sleep(1000);

          					   if(currentPackageName.equals("com.qualcomm.location.XT")){
          						   try {
          							   //inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
          							   inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
		                            		SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, (xcd * 7 / 8), (ycd * 2 / 3), 0));
          							   inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
		                            		SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, (xcd * 7 / 8), (ycd * 2 / 3), 0));
          							   break;
          						   } catch (Exception e) {
		                            Log.e("Exception when sendKeyDownUpSync", e.toString());
          						   }

          					   }
          				   }//for
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
	        }
    	} catch(Exception e) {
    		
    	}

	}
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       	if(!Utils.IsAutoEnableIZat()){
       		stopSelf();
       		return 0;
       	}
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return null;
    }
/*    
    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return false;
    }
    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }
*/
    @Override
    public void onDestroy() {
        Log.d(TAG, " service destroyed");
    }

    private class LocListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }
    
}