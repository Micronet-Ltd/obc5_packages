package com.micronet.alwaysonwifihotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by brigham.diaz on 12/12/2016.
 *
 * Responsible for starting service on device bootup
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // start the wifi hotspot monitoring service
        Intent service = new Intent(context, AlwaysOnWiFiHotspotService.class);
        context.startService(service);
    }
}
