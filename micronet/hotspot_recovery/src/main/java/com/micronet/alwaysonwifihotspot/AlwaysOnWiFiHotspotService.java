package com.micronet.alwaysonwifihotspot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by brigham.diaz on 12/12/2016.
 *
 * Service monitors the state of the Wifi AP and re-enables the tethering if it's
 * ever turned off (disabled). The monitoring is performed using a receiver and handler. The
 * receiver immediately enables the AP if it's ever disabled. The handler monitors the WifiApState
 * and enables it in a disabled state. The handler is more of a fallback in case the receiver failed
 * to enable hotspot or it didn't receive change.
 */
public class AlwaysOnWiFiHotspotService extends Service {
    private static String TAG = "AOWHS - Service";
    private final BroadcastReceiver wifiApStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // first make sure that action received is wifi ap
            if (WiFiApManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                // get Wi-Fi Hotspot state
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WiFiApManager.WIFI_AP_STATE_FAILED);
                Log.d(TAG, String.format("WIFI_AP_STATE_CHANGED_ACTION, new state=%s(%d)", Utils.getWifiApStateName(state), state));
                if (state == WiFiApManager.WIFI_AP_STATE_DISABLED) {
                    Log.d(TAG, "Attempting to enable hotspot since Wifi AP is disabled");
                    enableWiFi();
                }
            }
        }
    };

    /**
     * The handler should post to a Runnable when it's first called and afterwards, every 60 seconds.
     * If AP state is WIFI_AP_STATE_DISABLED or WIFI_AP_STATE_FAILED then
     *  attempt to re-enable hotspot.
     *  set post for 60 seconds after
     * If the AP state is WIFI_AP_STATE_ENABLED then
     *  do nothing
     *  set post for 60 seconds
     * If the AP state is disabling or enabling then
     *  do nothing
     *  set post for 10 seconds
     */
    private Handler wifiApHandler;
    private int wifiApvalue;
    private final long SIXTY_SECONDS = 60000;
    private final long TEN_SECONDS = 10000;
    private Context context;
    private static int handlerCount;
    private String handlerValue;
    private File Dir;

    private void enableWiFi() {
        // re-enable Wifi AP
        if (Utils.isAirplaneMode(getContentResolver()) == false) {
            WiFiApManager.setWiFiApState(context, true);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int state = WiFiApManager.getWifiApState(context);
            Log.d(TAG, String.format("State after attempting to enable hotspot %s(%d)", Utils.getWifiApStateName(state), state));
            if (WiFiApManager.getWifiApState(context) == WiFiApManager.WIFI_AP_STATE_ENABLED
                    || WiFiApManager.getWifiApState(context) == WiFiApManager.WIFI_AP_STATE_ENABLING) {
                increaseHC();
                Log.d(TAG, "enable wifi count=" + handlerValue);
            }
        } else {
            Log.d(TAG, "Airplane mode is On, Cant disable");
        }
        Log.d(this.toString(), "getWiFiApState=" + WiFiApManager.getWifiApState(context));
        if(WiFiApManager.getWifiApState(context)==WiFiApManager.WIFI_AP_STATE_ENABLED
                ||WiFiApManager.getWifiApState(context)==WiFiApManager.WIFI_AP_STATE_ENABLING )
        {
            increaseHC();
            Log.d(TAG, "enableWififunc:" +handlerValue);
        }
        else {
            Log.d(TAG, "Airplane mode is On, Cant enable");
        }
    }
    //Function that increases th handler count
    private void increaseHC(){
        handlerCount++;
        handlerValue=Integer.toString(handlerCount);
        writeToFile(handlerValue,context);
        Log.d(TAG, "increaseHC:" +handlerValue);
    }
    //Write function
    public void writeToFile(String handlerValue, Context context){

        File file = new File(Dir, "HotspotEnabledCount.txt"); //Created a Text File
             if(!file.exists()) {
                 handlerValue = "0";
             }
        try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(handlerValue.getBytes());
                fileOutputStream.close();
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    //Read Function
    private String readFromFile(Context context) {

        String ret = "";
        File file = new File(Dir, "HotspotEnableCount.txt"); //Created a Text File
        if(!file.exists()) { return ret;}
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            fileReader.close();
            ret = stringBuilder.toString();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }
    @Override
    public void onCreate() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            Log.d(TAG, String.format("AlwaysOnWifiHotspotService v%s started.", version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // The service is being created
        IntentFilter intentFilter = new IntentFilter(WiFiApManager.WIFI_AP_STATE_CHANGED_ACTION);
        registerReceiver(wifiApStatusReceiver, intentFilter);
        context = this;
        if (wifiApHandler == null) {
            wifiApHandler = new Handler(Looper.myLooper());
            wifiApHandler.postDelayed(wifiApCheck, TEN_SECONDS);
        }
        //Creating a Directory if it isn't available
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File Root = Environment.getExternalStorageDirectory(); //Creating File Storage
            Dir = new File(Root.getAbsolutePath() + "/MicronetService");
            if (!Dir.exists()) {
                Dir.mkdir();
            }
        }
        //readFromFile(context);
        if (this.readFromFile(context)==""){
            //Initializing handler Count to 0 (When the service restarts)
            handlerCount=0;
            handlerValue = Integer.toString(handlerCount);
            writeToFile(handlerValue, context);
        }
        else {
            handlerValue=this.readFromFile(context);
            handlerCount=Integer.parseInt(handlerValue);
        }
		PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, com.micronet.alwaysonwifihotspot.MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    final Runnable wifiApCheck = new Runnable() {
        @Override
        public void run() {
            wifiApvalue = WiFiApManager.getWifiApState(context);
            Log.d(TAG, String.format("Current AP State=%s(%d)", Utils.getWifiApStateName(wifiApvalue), wifiApvalue));

            try {
                switch (wifiApvalue) {
                    case WiFiApManager.WIFI_AP_STATE_DISABLING: //WiFi AP is currently disabling
                        //Doing Nothing and Setting post to 10s
                        wifiApHandler.postDelayed(this, TEN_SECONDS);
                        break;
                    case WiFiApManager.WIFI_AP_STATE_DISABLED: //WiFi AP is currently disabled
                        //Re-enable the WiFi Hotspot state if Airplane Mode is Off
                        enableWiFi();
                        /* WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                           wifi.setWifiEnabled(true);*/ //To disable Wi-Fi
                        //Seting post to 60s
                        wifiApHandler.postDelayed(this, SIXTY_SECONDS);
                        break;
                    case WiFiApManager.WIFI_AP_STATE_ENABLING: //Wifi AP is currently enabling
                        //Do nothing Setting post to 10s
                        wifiApHandler.postDelayed(this, TEN_SECONDS);
                        break;
                    case WiFiApManager.WIFI_AP_STATE_ENABLED:// WiFi AP is currently enabled
                        // Do nothing
                        wifiApHandler.postDelayed(this, SIXTY_SECONDS);
                        break;
                    case WiFiApManager.WIFI_AP_STATE_FAILED://WiFi AP failed
                        // Re enable the WiFi Hotspot state if airplane mode is off
                        enableWiFi();
                        wifiApHandler.postDelayed(this, SIXTY_SECONDS);
                        break;
                    default:
                        wifiApHandler.postDelayed(this, 60000);
                        break;
                }
            } catch (Exception e) {
                Log.d(TAG, "run: bh");
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        wifiApHandler.removeCallbacks(wifiApCheck);
        unregisterReceiver(wifiApStatusReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return null;
    }
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
}
