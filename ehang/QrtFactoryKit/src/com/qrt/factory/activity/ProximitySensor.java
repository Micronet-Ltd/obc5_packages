/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ProximitySensor extends AbstractActivity {

    private static final String TAG = "PSensor Test";

    private SensorManager mSensorManager = null;

    private Sensor mPSensor = null;

    private PSensorListener mPSensorListener;

    private TextView mTextView;

    private final static int MIN_COUNT = 3;

    private final static int INIT_VALUE = 10;

    private float value = INIT_VALUE;

    private float pre_value = INIT_VALUE;

    private int count = 0;

    private final static int SENSOR_TYPE = Sensor.TYPE_PROXIMITY;

    private static final String P_SENSOR_FILE_PATH
            = "/sys/devices/virtual/input/input1/id/version";

    private boolean pass = false;

    private TextView mVersionView;

    @Override
    public void finish() {

        try {
            mSensorManager.unregisterListener(mPSensorListener, mPSensor);
        } catch (Exception e) {
            loge(e);
        }
        super.finish();
    }

    void bindView() {

        mTextView = (TextView) findViewById(R.id.psensor_result);
        mVersionView = (TextView) findViewById(R.id.psensor_version);

        Button cancel = (Button) findViewById(R.id.psensor_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });
    }

    void getService() {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            mResultBuffer.append(getString(R.string.service_get_fail));
            fail();
            return;
        }

        mPSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mPSensor == null) {
            mResultBuffer.append(getString(R.string.sensor_get_fail));
            fail();
            return;
        }

        mPSensorListener = new PSensorListener(this);
        if (!mSensorManager.registerListener(mPSensorListener, mPSensor,
                SensorManager.SENSOR_DELAY_FASTEST)) {
            mResultBuffer.append(getString(R.string.sensor_register_fail));
            fail();
            return;
        }
        String pSensorInfo = Utilities
                .getSensorInfoByCode(ProximitySensor.this, Utilities
                        .getFileInfo(P_SENSOR_FILE_PATH));
        mVersionView.setText(
                getString(R.string.light_sensor_verison) + pSensorInfo);
    }

    void updateView(Object s) {
        mTextView.setText(TAG + " : " + s);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.psensor);

        bindView();
        getService();

        init();
        updateView(value);
    }

    private void init() {
        value = pre_value = INIT_VALUE;
        count = 0;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (mSensorManager == null || mPSensorListener == null
                || mPSensor == null) {
            return;
        }
        mSensorManager.unregisterListener(mPSensorListener, mPSensor);
    }

    public class PSensorListener implements SensorEventListener {

        public PSensorListener(Context context) {
            super();
        }

        public void onSensorChanged(SensorEvent event) {

            // PSensor event.value has 3 equal value. Value only can be 1 and 0
            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {

                    value = event.values[0];
                    logd(event.values.length + ":" + event.values[0] + " "
                            + event.values[0] + " "
                            + event.values[0] + " ");
                    updateView(value);
                    // 1(no covered)->0(covered)
                    if (value != INIT_VALUE && value != pre_value) {
                        count++;
                    }
                    if (count >= MIN_COUNT && !pass) {
                        pass = true;
                        pass();
                    }
                    pre_value = value;
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }
}
