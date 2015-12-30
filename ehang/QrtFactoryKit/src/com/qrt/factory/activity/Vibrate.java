/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;

public class Vibrate extends AbstractActivity {

    private static final String TAG = "Vibrate Test";

    private Handler mHandler = new Handler();

    private final long VIBRATOR_ON_TIME = 1000;

    private final long VIBRATOR_OFF_TIME = 500;

    private Vibrator mVibrator = null;

    private long[] pattern = {
            VIBRATOR_OFF_TIME, VIBRATOR_ON_TIME
    };

    @Override
    protected String getTag() {
        return TAG;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.vibrate);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (!isFinishing()) {
            showPassOrFailDialog(Vibrate.this, getString(R.string.vibrate_confirm),
                    getString(R.string.pass),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            pass();
                        }
                    }, getString(R.string.fail),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            fail();
                        }
                    }
            );
        }
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        mVibrator.cancel();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mHandler.postDelayed(mRunnable, 0);
        super.onResume();
    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            mHandler.removeCallbacks(mRunnable);
            mVibrator.vibrate(pattern, 0);
        }
    };
}
