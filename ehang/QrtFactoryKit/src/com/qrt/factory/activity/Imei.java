package com.qrt.factory.activity;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.qrt.factory.R;
import com.qrt.factory.serial.Rs232Tester;

/**
 * Created by aitam on 26/06/2017.
 */

public class Imei extends AbstractActivity {

    
    private static final String TAG = Imei.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String deviceId=telephonyManager.getDeviceId();

        if (deviceId == null || !deviceId.startsWith("35740708") || deviceId.length()!=15 || !deviceId.matches("\\d+")) {
            Log.e(getTag(),"Bad IMEI: "+deviceId);
            fail();
        } else {
            Log.i(getTag(),"IMEI: "+deviceId);
            pass();
        }
    }


    @Override
    protected String getTag() {
        return TAG;
    }


}
