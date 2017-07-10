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
    public volatile static boolean pauseStatus;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ACTION_PAUSE_SERVICE)) {
            //  pause service
            pauseStatus=true;
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Read_Write_File.serviceActivityLog(pauseStatus,context);
            Intent service = new Intent(context,Cellular_Data_Service.class);
            boolean res = context.stopService(service);
            Log.d(TAG, "Service Stopped by User   status="+res);

        } else if(intent.getAction().equals(ACTION_START_SERVICE) ) {
            // start service
            pauseStatus=false;
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent service = new Intent(context,Cellular_Data_Service.class);
            context.startService(service);
            Log.d(TAG, "Service Started by User");     }
    }
}
