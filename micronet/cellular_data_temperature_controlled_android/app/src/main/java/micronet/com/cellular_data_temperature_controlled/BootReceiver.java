package micronet.com.cellular_data_temperature_controlled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by eemaan.siddiqi on 12/15/2016.
 */
// This class is responsible for starting the service on boot
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Start Monitoring the temperature and Mobile Data Service
        Intent service = new Intent(context,Cellular_Data_Service.class);
        context.startService(service);

    }
}
