/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LightSensor extends AbstractActivity {

    private static final String TAG = "LightSensor Test";

    private SensorManager LightSensorManager = null;

    private Sensor mLightSensor = null;

    private LightSensorListener mLightSensorListener;

    private TextView mTextView;

    private Button cancelButton;

    private final static String INIT_VALUE = "";

    private static String value = INIT_VALUE;

    private static String pre_value = INIT_VALUE;

    private int min_count = 4;

    private final static int SENSOR_TYPE = Sensor.TYPE_LIGHT;

    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    private static final String LIGHT_SENSOR_FILE_PATH
            = "/sys/devices/virtual/input/input1/id/version";

    private boolean pass = false;

    private TextView mVersionTextView;

    @Override
    public void finish() {
        try {
            LightSensorManager
                    .unregisterListener(mLightSensorListener, mLightSensor);
        } catch (Exception e) {
            loge(e);
        }
        super.finish();
    }

    void bindView() {
        mTextView = (TextView) findViewById(R.id.lightsensor_result);
        mVersionTextView = (TextView) findViewById(R.id.lightsensor_version);
        cancelButton = (Button) findViewById(R.id.lightsensor_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });
    }

    void getService() {

        LightSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (LightSensorManager == null) {
            mResultBuffer.append(getString(R.string.service_get_fail));
            fail();
            return;
        }

        mLightSensor = LightSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mLightSensor == null) {
            mResultBuffer.append(getString(R.string.sensor_get_fail));
            fail();
            return;
        }

        mLightSensorListener = new LightSensorListener();
        if (!LightSensorManager
                .registerListener(mLightSensorListener, mLightSensor,
                        SENSOR_DELAY)) {
            mResultBuffer.append(getString(R.string.sensor_register_fail));
            fail();
            return;
        }
        String lightSensorInfo = Utilities
                .getSensorInfoByCode(LightSensor.this, Utilities
                        .getFileInfo(LIGHT_SENSOR_FILE_PATH));
        mVersionTextView.setText(
                getString(R.string.light_sensor_verison) + lightSensorInfo);
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
        setContentView(R.layout.lightsensor);

        min_count = "1".equals(SystemProperties.get("ro.ftmtestmode")) ?
                1 : min_count;

        bindView();
        getService();
        init();
        updateView(value);
    }

    private void init() {
        value = INIT_VALUE;
        pre_value = INIT_VALUE;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (LightSensorManager == null || mLightSensorListener == null
                || mLightSensor == null) {
            return;
        }
        LightSensorManager
                .unregisterListener(mLightSensorListener, mLightSensor);
    }

    public class LightSensorListener implements SensorEventListener {

        private int count = 0;

        public void onSensorChanged(SensorEvent event) {

            // LightSensor event.value has 3 equal value.
            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {
                    logd(event.values.length + ":" + event.values[0] + " "
                            + event.values[0] + " "
                            + event.values[0] + " ");
                    String value = "(" + event.values[0] + ", "
                            + event.values[1] + ", "
                            + event.values[2] + ")";
                    updateView(value);
                    if (value != pre_value) {
                        count++;
                    }
                    if (count >= min_count && !pass) {
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
