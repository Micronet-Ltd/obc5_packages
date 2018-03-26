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
import android.widget.TextView;

import java.text.DecimalFormat;

public class Battery extends AbstractActivity {

    private static final String TAG = "Battery Test";

    private static final String CAPACITY
            = "/sys/class/power_supply/battery/capacity";

    private static final String VOLTAGE_NOW_FILE
            = "/sys/class/power_supply/battery/voltage_now";

    private static final String TEMPERATURE_FILE
            = "/sys/class/power_supply/battery/temperature";

    private static final String STATUS_FILE
            = "/sys/class/power_supply/battery/status";

    private static final String POWER_USB_FILE
            = "/sys/class/power_supply/usb/online";

    private static final String POWER_AC_FILE
            = "/sys/class/power_supply/ac/online";

    private static final int PASSING_CAPACITY = 60;

    private TextView mTextView = null;

    private int mCountdown = 30;

    private int testCount = 0;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(0);
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mTextView = new TextView(this);
        mTextView.setTextSize(20);
        setContentView(mTextView);

        mHandler.sendMessage(createMessage(mCountdown));
        mHandler.sendEmptyMessage(0);
    }

    private Message createMessage(int countdown) {
        Message message = new Message();
        message.what = 1;
        Bundle data = new Bundle();
        data.putInt("countdown", countdown);
        message.setData(data);
        return message;
    }

    private void testBattery() { //end the test
/*begin :modified by tianfangzhou for battery test ,2013.10.14*/
//        String powerUsbValue = Utilities.getFileInfo(POWER_USB_FILE);
//        String powerAcValue = Utilities.getFileInfo(POWER_AC_FILE);
//
//        if ("1".equals(powerAcValue) || "1".equals(powerUsbValue)) {

            boolean ret = batteryStatusInfo();
            initBartteryVoltageInfo();
            initBatteryTemperatureInfo();
            boolean sufficientBatteryCapacity = initBatteryCapacityInfo(); // Order is important because we don't want to have short circuiting.

            if (ret) {
                if(sufficientBatteryCapacity) {
                    pass();
                }
                else {
                    fail();
                }
            } else {
                if (testCount < 30) {
                    mHandler.sendEmptyMessageDelayed(2, 500);
                } else {
                    fail();
                }
            }
        testCount++;
//        } else {
//            if (!isFinishing()) {
//                mHandler.sendEmptyMessageDelayed(0, 3000);
//            }
//        }
/*end :modified by tianfangzhou for battery test ,2013.10.14*/
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) { //re-test
            	/*begin :modified by tianfangzhou for battery test ,2013.10.14*/
                if (!isFinishing()) {
                    String powerUsbValue = Utilities.getFileInfo(POWER_USB_FILE);
                    String powerAcValue = Utilities.getFileInfo(POWER_AC_FILE);
                    if ("1".equals(powerAcValue) || "1".equals(powerUsbValue)) {
                        mHandler.sendEmptyMessageDelayed(2, 500);
                    }else{
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    }
                }

            } else if (msg.what == 1) {  //show countdown
                if (!isFinishing()) {
                    mTextView.setText(getString(R.string.battery_insert,msg.getData().getInt("countdown")));
                    if (mCountdown == 0) {
                        mResultBuffer.append(getString(R.string.time_out));
                        fail();
                    } else {
                        mHandler.sendMessageDelayed(createMessage(--mCountdown), 1000);
                    }
                }
            }else if(msg.what == 2){ 
            	testBattery();
            } 
            /*end :modified by tianfangzhou for battery test ,2013.10.14*/ 
        }
    };

    private void initBatteryTemperatureInfo() {
        String tmp = Utilities.getFileInfo(TEMPERATURE_FILE);
        if (tmp != null) {
            mResultBuffer.append("\n" + formateTemperature(tmp));
        }
    }

    private String formateTemperature(String tmp) {
        float temperature = 0.1f * Float.valueOf(tmp);
        return getString(R.string.battery_temperature) + new DecimalFormat(
                "###.#").format(temperature) + "° \n";
    }

    private void initBartteryVoltageInfo() {
        String tmp = Utilities.getFileInfo(VOLTAGE_NOW_FILE);
        if (tmp != null) {
            mResultBuffer.append("\n" + formatVoltage(tmp));
        }
    }

    private boolean initBatteryCapacityInfo() {
        String tmp = Utilities.getFileInfo(CAPACITY);
        if(tmp != null) {
            mResultBuffer.append("\n" + formatCapacity(tmp));
        }

        return (Integer.parseInt(tmp) >= PASSING_CAPACITY);
    }

    private String formatVoltage(String tmp) {
        float voltage = Float.valueOf(tmp);
        if (voltage > 1000000) {
            voltage = voltage / 1000000;
        } else if (voltage > 1000) {
            voltage = voltage / 1000;
        }
        return getString(R.string.battery_voltage) + voltage + "V";
    }

    private String formatCapacity(String tmp) {
        return getString(R.string.battery_capacity) + tmp + "%";
    }

    private boolean batteryStatusInfo() {
        String tmp = Utilities.getFileInfo(STATUS_FILE);
        if (tmp != null) {
            mResultBuffer
                    .append(getString(R.string.battery_status) + tmp);
            return pass(tmp);
        }
        return false;
    }

    private boolean pass(String tmp) {
        return "Charging".equalsIgnoreCase(tmp) || "Full".equalsIgnoreCase(tmp);
    }
}
