package micronet.com.cellular_data_temperature_controlled;

import android.content.Context;
import android.util.Log;

public class ServiceConfiguration {
    private static final String TAG = "CellDataService-Config";

    public static boolean getCellularDataConfig(Context context) {
        if (ReadWriteOperations.readConfigurationFromFile(context).equalsIgnoreCase("true")) {
            return true;
        } else if (ReadWriteOperations.readConfigurationFromFile(context).equalsIgnoreCase("false")) {
            return false;
        } else if (ReadWriteOperations.readConfigurationFromFile(context).equals("")) {
            Log.d(TAG, " Override Configuration File did not exist! Setting the default to true! ");
            //TODO: Change me back to false; This change was specific for Orbcomm
            ReadWriteOperations.writeConfigurationToFile(true, context);
            return false;
        } else {
            Log.d(TAG, " Invalid Values! ");
            return false;
        }
    }

    public static boolean getModifiedCellularDataState(Context context) {
        boolean state = true;
        String stateValueRead;
        int stateRead;
        stateValueRead = ReadWriteOperations.readManagedStateFromFile(context);
        if (stateValueRead == "") {
            //If the file corrupts due to some reason while the service is running, Enable cell data (Might override user's settings) if all the cores are below 80.
            Log.e(TAG, "Error: Managed-MobileDataState.txt didn't exist! Enabling cell data and setting disabled state to false!");
            ReadWriteOperations.writeManagedStateToFile(Integer.toString(0), context);
            state = false;
            return state;
        } else {
            stateRead = Integer.parseInt(stateValueRead);
            Log.d(TAG, "getModifiedCellularDataState(): Service - Managed Cell Data State:    " + stateRead);

            if (stateRead == 0) {
                state = false;
                return state;
            } else if (stateRead == 1) {
                state = true;
                return state;
            } else {
                Log.e(TAG, "Error: Managed-MobileDataState.txt doesn't contain a 0 or a 1!! Returning disabled state as true! ");
                return state;
            }

        }
    }
}
