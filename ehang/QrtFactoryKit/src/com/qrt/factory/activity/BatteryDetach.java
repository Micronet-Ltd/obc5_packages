/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

public class BatteryDetach extends AbstractActivity {
    private int diff = 0;
    private int i;
    private String s = "";
    private static int average = 0;

    private TextView textViewNoBat = null;
    private TextView textViewWithBat = null;
    private TextView textViewHeaderText = null;
    private TextView textView = null;
    private TextView mTextView = null;

    private static final String TAG = "Battery Detach Test";

    private static String LBC_VBAT2VPH_DEBUG
            = "/sys/kernel/debug/qpnp_lbc/lbc_vbat2vph_debug";

    private static final String LBC_VBAT_NOW
            = "/sys/kernel/debug/qpnp_lbc/lbc_vbat_now";

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery_detach);
        textViewNoBat = (TextView) findViewById(R.id.withoutBatteryNumberOfTest);
        textViewWithBat = (TextView) findViewById(R.id.withBatteryNumderOfTest);
        textViewHeaderText = (TextView) findViewById(R.id.batteryDetachText);
        mTextView = (TextView) findViewById(R.id.batteryDetachText);
        mTextView.setText(getString(R.string.battery_detach_text));

        try {
            exec();
        } catch (Exception e) {
            fail();
        }
    }

    private boolean testBatteryDetach() {

         String[] cmdEcho0 = {"/system/bin/sh", "-c", "echo 0 > /sys/kernel/debug/qpnp_lbc/lbc_vbat2vph_debug"};
         String[] cmdEcho1 = {"/system/bin/sh", "-c", "echo 1 > /sys/kernel/debug/qpnp_lbc/lbc_vbat2vph_debug"};

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewNoBat.setText("Without battery: " + getString(R.string.battery_detach_number_of_test));
                textViewWithBat.setText("With battery: " + getString(R.string.battery_detach_number_of_test));
            }
        });

        try {
            // Be sure that lbc_vbat2vph_debug is zero
            if (getLbcVbat2VphDebug() != false) {
                Runtime.getRuntime().exec(cmdEcho0);
            }

            //If diff is negative repeat test
            while (diff <= 0) {
                if (diff < 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewHeaderText.setText(getString(R.string.battery_detach_text_fail));
                            textViewNoBat.setText("Without battery: " + getString(R.string.battery_detach_number_of_test));
                            textViewWithBat.setText("With battery: " + getString(R.string.battery_detach_number_of_test));
                        }
                    });
                }
                //Enter to debug mode by lbc_vbat2vph_debug = 1
                Process p = Runtime.getRuntime().exec(cmdEcho1);
                p.waitFor();
                Log.d(TAG, "BatteryDetach : getLbcVbat2VphDebug - changed to 1 (true): " + getLbcVbat2VphDebug());
                //Make average voltage from 10 measurement serious lbc_vbat_now
                int debAv = getAverageMeasurement(true);
                //Leave debug mode by lbc_vbat2vph_debug = 0
                p = Runtime.getRuntime().exec(cmdEcho0);
                p.waitFor();
                Log.d(TAG, "BatteryDetach : getLbcVbat2VphDebug - changed to 0 (false): " + getLbcVbat2VphDebug());
                //Make average voltage from 10 measurement serious lbc_vbat_now
                int notDebAv = getAverageMeasurement(false);
                //Compare two averages if diff is more 100 mAmps the test is passed
                diff = debAv - notDebAv;
                Log.d(TAG, "BatteryDetach : getAverageMeasurement - diff" + diff);
            }

            //If diff close to zero, the test is failed
            if (diff < 100000)
                return false;
            else
                return true;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    private int getAverageMeasurement(boolean b) {
        if (b == true) {
            s = "Without battery: ";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView = (TextView) findViewById(R.id.withoutBatteryNumberOfTest);
                }
            });
        } else {
            s = "With battery: ";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView = (TextView) findViewById(R.id.withBatteryNumderOfTest);
                }
            });
        }
        average = 0;
        for ( i = 0; i < 10; i++) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int j = i+1;
                    textView.setText(s + "Done: " + j + "/10");
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            average = average + getLbcVbatNow();
        }
        return average / 10;
    }


    private boolean getLbcVbat2VphDebug() {
        String tmp = Utilities.getFileInfo(LBC_VBAT2VPH_DEBUG);
        if (tmp != null && tmp.equals("vbat-to-vph debug disabled")) {
            return false;
        } else return true;
    }

    private int getLbcVbatNow() {
        String tmp = Utilities.getFileInfo(LBC_VBAT_NOW);
        if (tmp != null) {
            return Integer.parseInt(tmp.substring(0, tmp.indexOf(" ")));
        }
        return -1;
    }


    Handler mHandler = new Handler() {

        @Override
        public void dispatchMessage(android.os.Message msg) {
            boolean res = (Boolean) msg.obj;
            if (res) {
                pass();
            } else {
                fail();
            }
        }
    };

    void exec() {

        new Thread() {

            public void run() {
                try {
                    Message message = new Message();
                    message.obj = testBatteryDetach();
                    message.setTarget(mHandler);
                    message.sendToTarget();

                } catch (Exception e) {
                    logd(e);
                    Message message = new Message();
                    message.obj = false;
                    message.setTarget(mHandler);
                    message.sendToTarget();
                }
            }

        }.start();
    }
}
