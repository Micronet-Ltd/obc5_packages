/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import android.app.Service;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

public class Sim1 extends AbstractActivity {

    private static final String TAG = "SIM1 Test";

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        String IMSI = "";

            TelephonyManager mTelephonyManager =
                    (TelephonyManager) getSystemService(
                            Service.TELEPHONY_SERVICE);
            IMSI = mTelephonyManager.getSubscriberId(0);
        int simState = mTelephonyManager.getSimState(0);

        if (!TextUtils.isEmpty(IMSI)) {
            showToast("IMSI: " + IMSI);
            pass();
        } else if (simState == TelephonyManager.SIM_STATE_READY) {
            showToast("SIM1 State: Ready");
            pass();
        } else {
            fail();
        }
    }
}
