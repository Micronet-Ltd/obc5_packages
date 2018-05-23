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
    public static final String ACTION_STOP_SERVICE = "micronet.com.cellular_data_temperature_controlled.STOP_SERVICE";
    public static final String ACTION_OVERRIDE_CELL_DATA_STATE = "micronet.com.cellular_data_temperature_controlled.OVERRIDE_CELL_DATA_STATE";
    public static final String ACTION_OVERRIDE_CELL_DATA_PASS = "micronet.com.cellular_data_temperature_controlled.OVERRIDE_CELL_DATA_PASS";
    public static final String ACTION_OVERRIDE_CELL_DATA_FAIL = "micronet.com.cellular_data_temperature_controlled.OVERRIDE_CELL_DATA_FAIL";
    public static final String ACTION_OVERRIDE_CELL_DATA_NO_ACTION = "micronet.com.cellular_data_temperature_controlled.OVERRIDE_CELL_DATA_NO_ACTION";

    private final String overrideSetting = "OverrideCellState";
    private Boolean settingState = false;
    private Boolean currentState;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION_STOP_SERVICE)) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!Utils.isMyServiceRunning(context, CellularDataService.class)) {
                Log.d(TAG, "STOP_SERVICE Intent received! Service is not running!");
                return;
            } else {
                Intent service = new Intent(context, CellularDataService.class);
                context.stopService(service);
                Log.d(TAG, "STOP_SERVICE Intent received! Service Stopped by User!");
                ReadWriteOperations.serviceActivityLog(Utils.isMyServiceRunning(context, CellularDataService.class), "Pause Service", "Pause Intent Received", context);
            }
        } else if (intent.getAction().equals(ACTION_START_SERVICE)) {
            // start service
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Utils.isMyServiceRunning(context, CellularDataService.class)) {
                Log.d(TAG, "START_SERVICE Intent received! Service is already running!");
                return;
            } else {
                Intent service = new Intent(context, CellularDataService.class);
                context.startService(service);
                Log.d(TAG, "START_SERVICE Intent Received! Service Started by User!");
                ReadWriteOperations.serviceActivityLog(Utils.isMyServiceRunning(context, CellularDataService.class), "Start Service", "Start Intent Received", context);

            }
        } else if (intent.getAction() == ACTION_OVERRIDE_CELL_DATA_STATE) {
            //Get current state of the setting
            currentState = ServiceConfiguration.getCellularDataConfig(context);
            //Get intent extra value: if currentState != extra value, change the setting based on extra value
            boolean extra = intent.getBooleanExtra(overrideSetting, settingState);
            Log.d(TAG, "ACTION_OVERRIDE_CELL_DATA_STATE Intent Received!");
            if (extra != currentState) {
                if (Utils.isMyServiceRunning(context, CellularDataService.class)) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Intent service = new Intent(context, CellularDataService.class);
                    context.stopService(service);
                    Log.d(TAG, "Existing Service Stopped : Reconfiguring Parameter - Override Cell Data State to " + extra);
                    ReadWriteOperations.serviceActivityLog(Utils.isMyServiceRunning(context, CellularDataService.class), "Stop Service", "Reconfiguring Parameters", context);
                }

                ReadWriteOperations.writeConfigurationToFile(extra, context);

                if (ServiceConfiguration.getCellularDataConfig(context) == extra) {
                    Intent passIntent = new Intent();
                    passIntent.setAction(ACTION_OVERRIDE_CELL_DATA_PASS);
                    context.sendBroadcast(passIntent);
                } else {
                    Intent passIntent = new Intent();
                    passIntent.setAction(ACTION_OVERRIDE_CELL_DATA_FAIL);
                    context.sendBroadcast(passIntent);
                }

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent service = new Intent(context, CellularDataService.class);
                context.startService(service);
                Log.d(TAG, "Service Started after attempting to change configuration parameters! ");
                ReadWriteOperations.serviceActivityLog(Utils.isMyServiceRunning(context, CellularDataService.class), "Re-Start Service", "Reconfiguring Parameters", context);
            } else {
                //The values are the same, do not re-configure.
                Intent passIntent = new Intent();
                passIntent.setAction(ACTION_OVERRIDE_CELL_DATA_NO_ACTION);
                context.sendBroadcast(passIntent);
                Log.d(TAG, "There is no difference in override cell state configuration - Ignore intent!");
            }
        }
    }
}
