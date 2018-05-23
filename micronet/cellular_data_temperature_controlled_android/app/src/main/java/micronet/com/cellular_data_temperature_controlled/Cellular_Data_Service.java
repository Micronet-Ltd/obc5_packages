package micronet.com.cellular_data_temperature_controlled;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

/**
 * Created by eemaan.siddiqi on 12/20/2016.
 */
public class Cellular_Data_Service extends Service {

    public static final String TAG = "Cellular_Data_Service";

    private Context context;
    private Handler mobileDataHandler;
    private int TEN_SECONDS = 10000;
    private int TWELVE_SECONDS = 12000;
    private int enabledCount;
    private String enabledCountValue;
    private int disabledCount;
    private String disabledCountValue;
    private boolean cellularDisabled = false;
    private int dataEnabledState = 0;
    private int dataDisabledState = 1;
    private boolean overrideUsersCellDataState;
    private String enabledInfo = "Cellular Data Enabled!";
    private String disabledInfo = "Cellular Data Disabled!";
    private String fileCorruptionInfo = "The file Managed-MobileDataState.txt did not exist! Service has enabled cell data and set the state to false!";

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            Log.d(TAG, String.format("CellularDataService v%s started.", version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Creating a Directory if it isn't available
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            File Root = Environment.getExternalStorageDirectory(); //Creating File Storage
            Read_Write_File.micronetServiceDir = new File(Root.getAbsolutePath() + "/MicronetService");
            Read_Write_File.serviceDir = new File(Read_Write_File.micronetServiceDir + "/CellularTemperatureService");
            if (!Read_Write_File.micronetServiceDir.exists()) {
                Read_Write_File.micronetServiceDir.mkdir();
            }
            if (!Read_Write_File.serviceDir.exists()) {
                Read_Write_File.serviceDir.mkdir();
            }
        }

        //File does not exist
        if (Read_Write_File.readFromFile(context).equals("")) {
            //Initializing enabledCount to 0 (When the service restarts)
            enabledCount = 0;
            enabledCountValue = Integer.toString(enabledCount);
            Read_Write_File.writeToFile(enabledCountValue, context);
        }
        //File exists
        else {
            enabledCountValue = Read_Write_File.readFromFile(context);
            enabledCount = Integer.parseInt(enabledCountValue);
        }
        //File does not exist
        if (Read_Write_File.readConfigurationFromFile(context).equals("")) {
            Read_Write_File.writeConfigurationToFile(false, context);
        }
        //File Exists
        else {
            overrideUsersCellDataState = ServiceConfiguration.getCellularDataConfig(context);
        }
        //File does not exist
        if (Read_Write_File.readDisabledCountFromFile(context).equals("")) {
            //Initializing disabledCount to 0 (When the service restarts)
            disabledCount = 0;
            disabledCountValue = Integer.toString(disabledCount);
            Read_Write_File.writeDisabledCountToFile(disabledCountValue, context);
        }
        //File exists
        else {
            disabledCountValue = Read_Write_File.readDisabledCountFromFile(context);
            disabledCount = Integer.parseInt(disabledCountValue);
        }
        //File does not exist
        if (Read_Write_File.readManagedStateFromFile(context).equals("")) {
            /*
             * Enable cellular data when the file MobileDataDisabled.txt doesn't exist
             * */
            if (!MobileDataManager.isAirplaneMode(getContentResolver())) {
                MobileDataManager.setDataEnabled(context, true); //Enabling Mobile data
                Log.d(TAG, "Enabling mobile data state, State= " + true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (MobileDataManager.getMobileDataState(context)) {
                    //Write 0 to the file after enabling cell data when the doesn't exist
                    Log.e(TAG, "Error: MobileDataDisabled.txt doesn't exist! Created a new file, enabled cell data and the disabled state is set to false!!");
                    Read_Write_File.writeManagedStateToFile(Integer.toString(dataEnabledState), context);
                    try {
                        Read_Write_File.LogToFile(disabledCountValue, enabledCountValue, context, fileCorruptionInfo, getContentResolver());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else
                Log.d(TAG, "Airplane mode enabled, Can't enable cellular data!");
            cellularDisabled = true;
        }
        //File exists
        else cellularDisabled = ServiceConfiguration.getModifiedCellularDataState(context);

        //Post a runnable that monitors the core temperatures every 10 seconds
        if (mobileDataHandler == null) {
            mobileDataHandler = new Handler(Looper.myLooper());
            mobileDataHandler.post(Temperature_Check);
        }

        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, micronet.com.cellular_data_temperature_controlled.MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);


    }

    //Function that increases the handler count for enabling
    private void increaseEnabledCount() throws IOException {
        enabledCount++;
        enabledCountValue = Integer.toString(enabledCount);
        Read_Write_File.writeToFile(enabledCountValue, context);
        Log.d(TAG, "Increased Enabled Count :" + enabledCountValue);
        Read_Write_File.LogToFile(disabledCountValue, enabledCountValue, context, enabledInfo, getContentResolver());
    }

    //Function to increase the disabling count and write to a file
    private void increaseDisabledCount() throws IOException {
        disabledCount++;
        disabledCountValue = Integer.toString(disabledCount);
        Read_Write_File.writeDisabledCountToFile(disabledCountValue, context);
        Log.d(TAG, "Increased Disable Count : " + disabledCountValue);
        Read_Write_File.LogToFile(disabledCountValue, enabledCountValue, context, disabledInfo, getContentResolver());
    }

    private void enableCellularData(boolean overrideSetting) throws IOException {
        MobileDataManager.isAirplaneMode(getContentResolver());
        if (!MobileDataManager.isAirplaneMode(getContentResolver())) {

            MobileDataManager.setDataEnabled(context, true);//Enabling Mobile data
            //Log.d(TAG, "Enabling mobile data state, State= " + true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Validate if it enabled it successfully
            if (MobileDataManager.getMobileDataState(context)) {
                increaseEnabledCount();
                Log.d(TAG, "Cellular Data Enabled Successfully at " + Utils.formatDate(System.currentTimeMillis()));
                //Printing and Managing configuration files
                if (!overrideSetting) {
                    cellularDisabled = false;
                    Read_Write_File.writeManagedStateToFile(Integer.toString(dataEnabledState), context);

                }
            }
        } else
            Log.d(TAG, "Airplane mode is On, Can't enable cellular data");
    }

    final Runnable Temperature_Check = new Runnable() {
        @Override
        public void run() {
            overrideUsersCellDataState = ServiceConfiguration.getCellularDataConfig(context);
            boolean mobileDataState = MobileDataManager.getMobileDataState(context);
            boolean airplaneModeState = MobileDataManager.isAirplaneMode(getContentResolver());
            Log.d(TAG, "*********************CHECK*******************");
           // Log.d(TAG, "mobileDataState =" + String.valueOf(mobileDataState) + ", airplaneMode =" + String.valueOf(airplaneModeState) + ", overrideCellDataState =" + String.valueOf(overrideUsersCellDataState));
            try {
                TemperatureValues.HigherTemp(context);
                TemperatureValues.NormalTemp(context);
                cellularDisabled = ServiceConfiguration.getModifiedCellularDataState(context);
                Log.d(TAG, "Cellular Data - Enabled Count =" + enabledCount);
                Log.d(TAG, "Cellular Data - Disabled Count =" + disabledCount);
                Log.d(TAG, "Service - Managed Cell Data State = " + cellularDisabled);

                if (TemperatureValues.HighTempResult) {
                    if (!MobileDataManager.getMobileDataState(context)) {
                        Log.d(TAG, "High Temperatures observed! Mobile Data is disabled!");
                        mobileDataHandler.postDelayed(this, TWELVE_SECONDS);
                        return;
                    } else {
                        Log.d(TAG, "High Temperatures observed! Mobile Data is enabled --> Disabling Cell Data!");
                        MobileDataManager.setDataEnabled(context, false);   //Disabling cellular data
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!MobileDataManager.getMobileDataState(context)) {
                            Read_Write_File.writeManagedStateToFile(Integer.toString(dataDisabledState), context);
                            Log.d(TAG, "High Temperatures observed! Mobile Data disabled successfully!");
                            cellularDisabled = true;
                            increaseDisabledCount();
                        }
                        mobileDataHandler.postDelayed(this, TEN_SECONDS);//setting post to thirty seconds
                        return;
                    }
                } else if (TemperatureValues.NormalTempResult) {
                    //If mobile data is disabled
                    Log.d(TAG, "Temperatures are in the Normal Range! Check Cellular Data States! ");
                    if (!MobileDataManager.getMobileDataState(context)) {
                        if (cellularDisabled) {
                            //The service disabled cellular data due to high temperatures and since the cores fell below 80, enable cellular data.
                            Log.d(TAG, "Service - Managed Cell Data State: " + Read_Write_File.readManagedStateFromFile(context) + "; Enabling Cellular Data, Reason: Service Initiated");
                            enableCellularData(false);
                            mobileDataHandler.postDelayed(this, TEN_SECONDS);//Setting post to ten seconds
                            return;
                        } else if (overrideUsersCellDataState) {
                            Log.d(TAG, "Override Cell Data State Configuration: " + ServiceConfiguration.getCellularDataConfig(context) + "; Enabling Cell Data, Reason: Always Data On Configured by User.");
                            enableCellularData(true);
                            mobileDataHandler.postDelayed(this, TEN_SECONDS);//Setting post to ten seconds
                            return;
                        }
                        else{
                            Log.d(TAG, "Cellular Data is Disabled, No condition satisified to enable cell data in normal range!");
                        }
                    }
                    //If mobile data is enabled then do nothing
                    else {
                        Log.d(TAG, "Mobile Data is enabled and the cores are in the acceptable range! Do nothing!");
                        mobileDataHandler.postDelayed(this, TWELVE_SECONDS);//Do Nothing and set post to 10 seconds
                        return;
                    }
                }
                Log.d(TAG, "mobileDataHandler.postDelayed(this, TEN_SECONDS)");
                mobileDataHandler.postDelayed(this, TEN_SECONDS);
            } catch (Exception e) {
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
        Log.e(TAG, "STOP");
        mobileDataHandler.removeCallbacks(Temperature_Check);
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
    }

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