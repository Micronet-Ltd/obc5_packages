package micronet.com.cellular_data_temperature_controlled;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by eemaan.siddiqi on 12/20/2016.
 */
public class MobileDataManager {

    public static final String TAG = "MobileDataManager";

    //Function to Enable/Disable Cellular Data
    public static void setDataEnabled(Context context, boolean enabled) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        try {
            Method setMobileDataEnabledMethod = null;
            setMobileDataEnabledMethod = telephonyManager.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            if (null != setMobileDataEnabledMethod)
            {
                setMobileDataEnabledMethod.invoke(telephonyManager, enabled);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    //Function that gets the current Mobile Data State
    public static Boolean getMobileDataState(Context context) {
        TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");
            getMobileDataEnabledMethod.setAccessible(true);
            if (null != getMobileDataEnabledMethod) {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
                return mobileDataEnabled;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error getting mobile data state", e);
        }
        return false;
    }
    public static boolean isMobileConnected(Context context) {
        return isConnected(context, ConnectivityManager.TYPE_MOBILE);
    }
    public static boolean isWifiConnected(Context context) {
        return isConnected(context, ConnectivityManager.TYPE_WIFI);
    }
    private static boolean isConnected(Context context, int type) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connMgr.getAllNetworks();
        NetworkInfo networkInfo;
        for (Network network : networks) {
            networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo != null && networkInfo.getType() == type && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }
    public static boolean isAirplaneMode(ContentResolver content) {
        boolean isAirplaneOn= Settings.System.getInt(content,Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        return isAirplaneOn;
    }

    public static boolean getModifiedCellularDataState(Context context){
        boolean state=true;
        String stateValueRead;
        int stateRead;
        stateValueRead=Read_Write_File.readManagedStateFromFile(context);
            if(stateValueRead==""){
                //If the file corrupts due to some reason while the service is running, Enable cell data (Might override user's settings) if all the cores are below 80.
                Log.e(TAG, "Error: MobileDataState.txt didn't exist! Enabling cell data and setting disabled state to false!");
                Read_Write_File.writeManagedStateToFile(Integer.toString(0),context);
                state= false;
                return state;
            }
            else {
                stateRead=Integer.parseInt(stateValueRead);
             if(stateRead==0){
                 state= false;
                 Log.d(TAG, "Read from file: CellularDataState:    " + state);
                 return state;
             }
            else if(stateRead==1){
                 state= true;
                 Log.d(TAG, "Read from file: CellularDataState:    " + state);
                 return state;
            }
            else
                Log.e(TAG, "Error: MobileDataState.txt doesn't contain a 0 or a 1!! Returning disabled state as true! ");
                return state;
            }
    }
}
