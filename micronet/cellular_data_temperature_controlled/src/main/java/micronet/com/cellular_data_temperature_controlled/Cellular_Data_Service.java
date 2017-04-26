package micronet.com.cellular_data_temperature_controlled;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
//import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * Created by eemaan.siddiqi on 12/20/2016.
 */
public class Cellular_Data_Service extends Service {

    private Context context;
    private Handler mobileDataHandler;
    private int TEN_SECONDS= 10000;
    private int TWELVE_SECONDS = 12000;
    private int enabledCount;
    private String enabledCountValue;
    private int disabledCount;
    private String disabledCountValue;
	private boolean cellularDisabled=false;
	
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context = this;
        if (mobileDataHandler == null) {
            mobileDataHandler = new Handler(Looper.myLooper());
            mobileDataHandler.post(Temperature_Check);
        }
        //Creating a Directory if it isn't available
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File Root = Environment.getExternalStorageDirectory(); //Creating File Storage
            Read_Write_File.Dir = new File(Root.getAbsolutePath() + "/MicronetService");
            if (!Read_Write_File.Dir.exists()) {
                Read_Write_File.Dir.mkdir();
            }
        }

        if (Read_Write_File.readFromFile(context) == "") {
            //Initializing handler Count to 0 (When the service restarts)
                enabledCount = 0;
                enabledCountValue = Integer.toString(enabledCount);
                Read_Write_File.writeToFile( enabledCountValue, context);
        }
        else {
                enabledCountValue = Read_Write_File.readFromFile(context);
                enabledCount = Integer.parseInt(enabledCountValue);
        }
        if (Read_Write_File.readDisabledCountFromFile(context)==""){
                disabledCount=0;
                disabledCountValue=Integer.toString(disabledCount);
                Read_Write_File.writeDisabledCountToFile(disabledCountValue,context);
        }
        else {
            disabledCountValue = Read_Write_File.readDisabledCountFromFile(context);
            disabledCount = Integer.parseInt( disabledCountValue);
        }
		PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, micronet.com.cellular_data_temperature_controlled.MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }
    //Function that increases the handler count for enabling
    private void increaseEnabledCount()
    {
        enabledCount++;
        enabledCountValue = Integer.toString(enabledCount);
        Read_Write_File.writeToFile( enabledCountValue, context);
        Log.d(TAG, "increased Enabled Count :" +  enabledCountValue);
        Read_Write_File.LogToFile(disabledCountValue, enabledCountValue, context);
        //Log.d(TAG, "Timestamp:"+Read_Write_File.timestamp +TemperatureValues.temperaturevalues +enabledCountValue);
    }
    //Function to increase the disabling count and write to a file
    private void increaseDisabledCount()
    {
            disabledCount++;
            disabledCountValue=Integer.toString(disabledCount);
            Read_Write_File.writeDisabledCountToFile(disabledCountValue,context);
            Log.d(TAG, "Increased Disable Count : "+disabledCountValue);
            Read_Write_File.LogToFile(disabledCountValue,enabledCountValue, context);
            //Log.d(TAG, "Timestamp:"+Read_Write_File.timestamp +TemperatureValues.temperaturevalues +enabledCountValue);
    }
    private void enableCellularData(){
        MobileDataManager.isAirplaneMode(getContentResolver());
        if(MobileDataManager.isAirplaneMode(getContentResolver())==false){
            MobileDataManager.setDataEnabled(context, true);//Enabling Mobile data
            Log.d(TAG, "Setting mobile data state: " + true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (MobileDataManager.getMobileDataState(context)==true)
            {
                increaseEnabledCount();
                Log.d(TAG, "Increased Enabling Count :" + enabledCount);
            }
        }
        else
            Log.d(TAG,"Airplane mode is On, Cant enable cellular data");
    }



    final Runnable Temperature_Check = new Runnable() {
        @Override
        public void run() {
            boolean mobileDataState = MobileDataManager.getMobileDataState(context);
            boolean mobileConnectionState = MobileDataManager.isMobileConnected(context);
            Log.d(TAG, String.format("mobileDataState=%b, mobileConnectionState=%b", mobileDataState, mobileConnectionState));
            try {
                TemperatureValues.HigherTemp(context);
                Log.d(TAG, "enabledcount=" + enabledCount);
                TemperatureValues.NormalTemp(context);
            
                if (TemperatureValues.HighTempResult == true)
                    {
                        if (MobileDataManager.getMobileDataState(context) == false) {
                            mobileDataHandler.postDelayed(this, TWELVE_SECONDS);
                        return;
                        }
                        else
                            MobileDataManager.setDataEnabled(context, false);   //Disabling the data
                         try {
                            Thread.sleep(1000);
                             }
                         catch (InterruptedException e) {
                            e.printStackTrace();
                            }
                        if(MobileDataManager.getMobileDataState(context)==false)
                            {
                            increaseDisabledCount();
                            }
                            Log.d(TAG, "Setting mobile data state: " + false);
                            Log.d(TAG, "Current Disabled Count :" + disabledCount);
                            mobileDataHandler.postDelayed(this, TEN_SECONDS);//setting post to thirty seconds
                            return;
                    }
					
					    else if (TemperatureValues.NormalTempResult == true && cellularDisabled==true) {
                    if (MobileDataManager.isMobileConnected(context) == true){//If Cellular Data is already turned ON
                        mobileDataHandler.postDelayed(this, TWELVE_SECONDS);//Do Nothing and set post to 10 seconds
                        return;
                    }
                    else if (MobileDataManager.getMobileDataState(context) == false) {
                        enableCellularData();
                        mobileDataHandler.postDelayed(this, TEN_SECONDS);//Setting post to ten seconds
                        return;
                    }
                }

                Log.d(TAG, "mobileDataHandler.postDelayed(this, TWELVE_SECONDS)");
                mobileDataHandler.postDelayed(this, TEN_SECONDS);
            }
            catch (Exception e)
            {
                e.printStackTrace();
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
        super.onDestroy();
        Log.e(TAG,"STOP");
        mobileDataHandler.removeCallbacks(Temperature_Check);
        Toast.makeText(this,"Service Stopped",Toast.LENGTH_LONG).show();
    }

    //@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
}
