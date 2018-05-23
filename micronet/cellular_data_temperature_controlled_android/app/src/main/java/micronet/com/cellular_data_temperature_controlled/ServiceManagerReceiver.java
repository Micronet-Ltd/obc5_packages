package micronet.com.cellular_data_temperature_controlled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static java.lang.Thread.sleep;


/**
 * Created by eemaan.siddiqi on 1/13/2017.
 */
public class ServiceManagerReceiver extends BroadcastReceiver {
    public static final String TAG = "ServiceManagerReciever";
    public static final String ACTION_START_SERVICE = "micronet.com.cellular_data_temperature_controlled.START_SERVICE";
    public static final String ACTION_PAUSE_SERVICE = "micronet.com.cellular_data_temperature_controlled.PAUSE_SERVICE";
    public static final String ACTION_OVERRIDE_CELL_DATA_STATE = "micronet.com.cellular_data_temperature_controlled.OVERRIDE_CELL_DATA_STATE";
    public static final String ACTION_OVERRIDE_CELL_DATA_PASS = "micronet.com.cellular_data_temperature_controlled.OVERRIDE_CELL_DATA_PASS";
    public static final String ACTION_OVERRIDE_CELL_DATA_FAIL = "micronet.com.cellular_data_temperature_controlled.OVERRIDE_CELL_DATA_FAIL";
    public static final String ACTION_OVERRIDE_CELL_DATA_NO_ACTION = "micronet.com.cellular_data_temperature_controlled.OVERRIDE_CELL_DATA_NO_ACTION";

    private final String overrideSetting = "OverrideCellState";
    private Boolean settingState = false;
    private Boolean currentState;

    public volatile static boolean pauseStatus;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_PAUSE_SERVICE)) {
            //  pause service
            pauseStatus = true;
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Read_Write_File.serviceActivityLog(pauseStatus, context);
            Intent service = new Intent(context, Cellular_Data_Service.class);
            boolean res = context.stopService(service);
            Log.d(TAG, "Service Stopped by User   status=" + res);

        } else if (intent.getAction().equals(ACTION_START_SERVICE)) {
            // start service
            pauseStatus = false;
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent service = new Intent(context, Cellular_Data_Service.class);
            context.startService(service);
            Log.d(TAG, "Service Started by User");
        } else if (intent.getAction() == ACTION_OVERRIDE_CELL_DATA_STATE) {
            //Get current state of the setting
            currentState = ServiceConfiguration.getCellularDataConfig(context);
            //Get intent extra value: if currentState != extra value, change the setting based on extra value
            boolean extra = intent.getBooleanExtra(overrideSetting, settingState);
            if (extra != currentState) {
                if (Utils.isMyServiceRunning(context, Cellular_Data_Service.class)) {
                    pauseStatus = true;
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Read_Write_File.serviceActivityLog(pauseStatus, context);
                    Intent service = new Intent(context, Cellular_Data_Service.class);
                    boolean res = context.stopService(service);
                    Log.d(TAG, "Existing Service Stopped : Reconfiguring Parameters! Servic Status= " + res);
                }
                Read_Write_File.writeConfigurationToFile(extra, context);
                if (ServiceConfiguration.getCellularDataConfig(context) == extra) {
                    Intent passIntent = new Intent();
                    passIntent.setAction(ACTION_OVERRIDE_CELL_DATA_PASS);
                    context.sendBroadcast(passIntent);
                } else {
                    Intent passIntent = new Intent();
                    passIntent.setAction(ACTION_OVERRIDE_CELL_DATA_FAIL);
                    context.sendBroadcast(passIntent);
                }
                pauseStatus = false;
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Read_Write_File.serviceActivityLog(pauseStatus, context);
                Intent service = new Intent(context, Cellular_Data_Service.class);
                context.startService(service);
                Log.d(TAG, "Service Started after attempting to change configuration parameters! ");
            } else {
                //The values are the same, do not re-configure.
                Intent passIntent = new Intent();
                passIntent.setAction(ACTION_OVERRIDE_CELL_DATA_NO_ACTION);
                context.sendBroadcast(passIntent);
            }
        }
    }
}
