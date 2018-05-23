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
public class CellularDataService extends Service {

    public static final String TAG = "CellularDataService";

    private Context context;
    private Handler mobileDataHandler;
    private int enabledCount;
    private String enabledCountValue;
    private int disabledCount;
    private String disabledCountValue;
    private int TEN_SECONDS = 12000;
    private int TWELVE_SECONDS = 12000;
    private int THIRTY_SECONDS = 30000; //Default
    private int postIntervalInSec = THIRTY_SECONDS;
    private boolean cellularDisabled = false;
    private int dataEnabledState = 0;
    private int dataDisabledState = 1;
    private boolean overrideUsersCellDataState;
    private String enabledInfo = "Cellular Data Enabled!";
    private String disabledInfo = "Cellular Data Disabled!";
    private static String[] managedStateCorrInfo = {"Managed-MobileDataState.txt did not exist!", "STEP1/3: New file with the default state created!", "STEP2/3: Attempt to Enable Cell Data", "STEP2/3: Cell Data Enabled Already", "STEP3/3: Cell Data Enabled"};
    private static String overideCellConfInfo = "CellularDataServiceConfig.txt did not exist! Created a new file with the default setting(true)";

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
            ReadWriteOperations.micronetServiceDir = new File(Root.getAbsolutePath() + "/MicronetService");
            ReadWriteOperations.serviceDir = new File(ReadWriteOperations.micronetServiceDir + "/CellularTemperatureService");
            if (!ReadWriteOperations.micronetServiceDir.exists()) {
                ReadWriteOperations.micronetServiceDir.mkdir();
            }
            if (!ReadWriteOperations.serviceDir.exists()) {
                ReadWriteOperations.serviceDir.mkdir();
            }
        }

        //File does not exist
        if (ReadWriteOperations.readFromFile(context).equals("")) {
            //Initializing enabledCount to 0 (When the service restarts)
            enabledCount = 0;
            enabledCountValue = Integer.toString(enabledCount);
            ReadWriteOperations.writeToFile(enabledCountValue, context);
        }
        //File exists
        else {
            enabledCountValue = ReadWriteOperations.readFromFile(context);
            enabledCount = Integer.parseInt(enabledCountValue);
        }
        //File does not exist
        if (ReadWriteOperations.readConfigurationFromFile(context).equals("")) {
            Log.d(TAG, "Since the config file doesn't exist, by DEFAULT set the OverrideCellStateConfig Flag to TRUE!");
            //TODO: The default should change back to false; This change was done specifically for inthinc because Amos confirmed that we cannot push configuration files via Redbend on an OBC5
            ReadWriteOperations.writeConfigurationToFile(true, context);
            try {
                ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, overideCellConfInfo, getContentResolver());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //File Exists
        else {
            overrideUsersCellDataState = ServiceConfiguration.getCellularDataConfig(context);
        }
        //File does not exist
        if (ReadWriteOperations.readDisabledCountFromFile(context).equals("")) {
            //Initializing disabledCount to 0 (When the service restarts)
            disabledCount = 0;
            disabledCountValue = Integer.toString(disabledCount);
            ReadWriteOperations.writeDisabledCountToFile(disabledCountValue, context);
        }
        //File exists
        else {
            disabledCountValue = ReadWriteOperations.readDisabledCountFromFile(context);
            disabledCount = Integer.parseInt(disabledCountValue);
        }
        //File does not exist
        if (ReadWriteOperations.readManagedStateFromFile(context).equals("")) {
            //Enable cellular data when the file MobileDataDisabled.txt doesn't exist
            Log.e(TAG, "MobileDataDisabled.txt doesn't exist! Proceed with creating a new file with the default(false) state!");
            try {
                ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, managedStateCorrInfo[0], getContentResolver());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Create a new file after with the default (false) and enable cell data
            ReadWriteOperations.writeManagedStateToFile(Integer.toString(0), context);
            try {
                ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, managedStateCorrInfo[1], getContentResolver());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!MobileDataManager.getMobileDataState(context)) {
                try {
                    ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, managedStateCorrInfo[1], getContentResolver());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!MobileDataManager.isAirplaneMode(getContentResolver())) {
                    Log.d(TAG, "Enabling mobile data state, State= " + true);
                    MobileDataManager.setDataEnabled(context, true); //Enabling Mobile data
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (MobileDataManager.getMobileDataState(context)) {
                        ReadWriteOperations.writeManagedStateToFile(Integer.toString(dataEnabledState), context);
                        try {
                            ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, managedStateCorrInfo[4], getContentResolver());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e(TAG, "Airplane mode enabled, Can't enable cellular data!");
                }
            } else {
                //Cell Data Enabled by default!
                try {
                    ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, managedStateCorrInfo[3], getContentResolver());
                    ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, managedStateCorrInfo[4], getContentResolver());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Cell Data Enabled By Default");
            }
        }
        //File exists
        else cellularDisabled = ServiceConfiguration.getModifiedCellularDataState(context);

        //Post a runnable that monitors the core temperatures every 30 seconds
        if (mobileDataHandler == null) {
            mobileDataHandler = new Handler(Looper.myLooper());
            mobileDataHandler.post(temperatureCheck);
        }

        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, micronet.com.cellular_data_temperature_controlled.MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);


    }

    //Function that increases the handler count for enabling
    private void increaseEnabledCount() throws IOException {
        enabledCount++;
        enabledCountValue = Integer.toString(enabledCount);
        ReadWriteOperations.writeToFile(enabledCountValue, context);
        Log.d(TAG, "Increased Enabled Count :" + enabledCountValue);
        ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, enabledInfo, getContentResolver());
    }

    //Function to increase the disabling count and write to a file
    private void increaseDisabledCount() throws IOException {
        disabledCount++;
        disabledCountValue = Integer.toString(disabledCount);
        ReadWriteOperations.writeDisabledCountToFile(disabledCountValue, context);
        Log.d(TAG, "Increased Disable Count : " + disabledCountValue);
        ReadWriteOperations.LogToFile(disabledCountValue, enabledCountValue, context, disabledInfo, getContentResolver());
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
                    ReadWriteOperations.writeManagedStateToFile(Integer.toString(dataEnabledState), context);

                }
            }
        } else
            Log.d(TAG, "Airplane mode is On, Can't enable cellular data");
    }

    final Runnable temperatureCheck = new Runnable() {
        @Override
        public void run() {
            overrideUsersCellDataState = ServiceConfiguration.getCellularDataConfig(context);
            boolean mobileDataState = MobileDataManager.getMobileDataState(context);
            boolean airplaneModeState = MobileDataManager.isAirplaneMode(getContentResolver());
            Log.d(TAG, "mobileDataState =" + String.valueOf(mobileDataState) + ", airplaneMode =" + String.valueOf(airplaneModeState) + ", overrideCellDataState =" + String.valueOf(overrideUsersCellDataState));
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
                        Log.d(TAG, "Post a recheck after 30 seconds");
                        mobileDataHandler.postDelayed(this, postIntervalInSec);
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
                            ReadWriteOperations.writeManagedStateToFile(Integer.toString(dataDisabledState), context);
                            Log.d(TAG, "High Temperatures observed! Mobile Data disabled successfully!");
                            cellularDisabled = true;
                            increaseDisabledCount();
                        }
                        Log.d(TAG, "Post a recheck after 30 seconds");
                        mobileDataHandler.postDelayed(this, postIntervalInSec);//setting post to thirty seconds
                        return;
                    }
                } else if (TemperatureValues.NormalTempResult) {
                    //If mobile data is disabled
                    Log.d(TAG, "Temperatures are in the Normal Range! Check Cellular Data States! ");
                    if (!MobileDataManager.getMobileDataState(context)) {
                        if (cellularDisabled) {
                            //The service disabled cellular data due to high temperatures and since the cores fell below 80, enable cellular data.
                            Log.d(TAG, "Service - Managed Cell Data State: " + ReadWriteOperations.readManagedStateFromFile(context) + "; Enabling Cellular Data, Reason: Service Initiated");
                            enableCellularData(false);
                            Log.d(TAG, "Post a recheck after 30 seconds");
                            mobileDataHandler.postDelayed(this, postIntervalInSec);//Setting post to ten seconds
                            return;
                        } else if (overrideUsersCellDataState) {
                            Log.d(TAG, "Override Cell Data State Configuration: " + ServiceConfiguration.getCellularDataConfig(context) + "; Enabling Cell Data, Reason: Always Data On Configured by User.");
                            enableCellularData(true);
                            Log.d(TAG, "Post a recheck after 30 seconds");
                            mobileDataHandler.postDelayed(this, postIntervalInSec);//Setting post to ten seconds
                            return;
                        } else {
                            Log.d(TAG, "Cellular Data is Disabled, No condition satisified to enable cell data in normal range!");
                        }
                    }
                    //If mobile data is enabled then do nothing
                    else {
                        Log.d(TAG, "Mobile Data is enabled and the cores are in the acceptable range! Do nothing! Post a recheck after 30 seconds");
                        Log.d(TAG, "Post a recheck after 30 seconds");
                        mobileDataHandler.postDelayed(this, TWELVE_SECONDS);//Do Nothing and set post to 30 seconds
                        return;
                    }
                }
                Log.d(TAG, "Post a recheck after 30 seconds");
                mobileDataHandler.postDelayed(this, postIntervalInSec);
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
        mobileDataHandler.removeCallbacks(temperatureCheck);
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