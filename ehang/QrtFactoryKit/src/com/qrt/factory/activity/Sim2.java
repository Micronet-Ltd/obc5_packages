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

public class Sim2 extends AbstractActivity {

    private static final String TAG = "SIM2 Test";

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

            TelephonyManager mTelephonyManager =
                    (TelephonyManager) getSystemService(
                            Service.TELEPHONY_SERVICE);
        String  IMSI = mTelephonyManager.getSubscriberId(1);
        int simState = mTelephonyManager.getSimState(1);

        if (!TextUtils.isEmpty(IMSI)) {
            showToast("IMSI: " + IMSI);
            pass();
        } else if (mTelephonyManager.getSimState(1) == TelephonyManager.SIM_STATE_READY) {
            showToast("SIM1 State: Ready");
            pass();
        } else {
            fail();
        }
    }
}
