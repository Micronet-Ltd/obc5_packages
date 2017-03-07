package com.android.flymode;

import java.util.Locale;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;      
import java.io.IOException;  
import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import android.util.Log;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.content.ServiceConnection;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;

import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.android.flymode.Utilities;

public class FlyModeService extends Service
{
	private static final String TAG = "FlyModeService";
	private static boolean DBG = true;

	private static boolean flymode = false;

	Context mContext;
	
    private MyBinder myBinder = new MyBinder();

    // Internal messages
    private static final int POWER_ON = 1000;
		
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case POWER_ON:
					
					String result = Utilities.getFileInfo("/proc/rfkillpin");
					//if (DBG) log("read result:" + result);
					if(result.equals("0")&&flymode == false)
					{
					    flymode = true;
						if(!isAirplaneOn())
						{					 
						    setAirplaneModeOn(true);
							 
							SystemProperties.set("persist.sys.dclock.set_airplane", "true");		 
						}
					}
					else if(result.equals("1"))
					{
				        flymode = false;
	                    setOnlineMode();	 
					}

					sendModeMsg(POWER_ON,1000);
                    break;
    									
                default:
                    log("Handler received unknown message, what=" + msg.what);
            }
        }
    };

    public FlyModeService ()
    {
    	mContext = this;
    }
    
	public int sendModeMsg(int cmd, long delayMillis)
	{
		mHandler.sendMessageDelayed(mHandler.obtainMessage(cmd), delayMillis);
		return 0;
	} 

	private boolean isAirplaneOn() {
		return Settings.Global.getInt(mContext.getContentResolver(),
				Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
	}
	
	private void setAirplaneModeOn(boolean enabling) {
		// Change the system setting
		Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 
								enabling ? 1 : 0);
		
		// Post the intent
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", enabling);
		mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
	}

	private void setOnlineMode() {
		String setAirplane = SystemProperties.get("persist.sys.dclock.set_airplane", "false");
		
		if(setAirplane.equals("true"))
		{
			if (DBG) log("setAirplane is true!");
			if(isAirplaneOn())
			{
				setAirplaneModeOn(false);
		
				SystemProperties.set("persist.sys.dclock.set_airplane", "false");
			}
		}
	}
 
	@Override
	public void onCreate()
	{   
		super.onCreate();
		if (DBG) log("Creating!");
		
	}   
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (DBG) log("destory!");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return myBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		if (DBG) log("onUnbind!");
		return super.onUnbind(intent);
	}

	@Override
	public void onRebind(Intent intent)
	{
	    if (DBG) log("onUnbind!");
		super.onRebind(intent);
	}	
	
	public class MyBinder extends Binder {
	    public FlyModeService getService() {
	       return FlyModeService.this;
	    }
	}
	
    @Override
    public void onStart(Intent intent, int startId) {
    }    
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        log("onStartCommand: Power ON!");

        sendModeMsg(POWER_ON,0);
		
        return START_STICKY;
    }    
    
    protected void log(String msg) {
        Log.d(TAG, msg);
    }	
}

