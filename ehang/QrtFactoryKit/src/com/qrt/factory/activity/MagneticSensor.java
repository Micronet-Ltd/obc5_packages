/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MagneticSensor extends AbstractActivity {

    private static final String TAG = "MSensor Test";

    private SensorManager mSensorManager = null;

    private Sensor mMSensor = null;

    private MSensorListener mMSensorListener;

    private TextView mTextView;

    private Button cancelButton;

    private final static String INIT_VALUE = "";

    private static String value = INIT_VALUE;

    private static String pre_value = INIT_VALUE;

    private final int MIN_COUNT = 10;

    private final static int SENSOR_TYPE = Sensor.TYPE_MAGNETIC_FIELD;

    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    private boolean pass = false;

    @Override
    public void finish() {

        try {
            mSensorManager.unregisterListener(mMSensorListener, mMSensor);
        } catch (Exception e) {
            loge(e);
        }
        super.finish();
    }

    void bindView() {

        mTextView = (TextView) findViewById(R.id.msensor_result);
        cancelButton = (Button) findViewById(R.id.msensor_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

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

        mMSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mMSensor == null) {
            mResultBuffer.append(getString(R.string.sensor_get_fail));
            fail();
            return;
        }

        mMSensorListener = new MSensorListener();
        if (!mSensorManager
                .registerListener(mMSensorListener, mMSensor, SENSOR_DELAY)) {
            mResultBuffer.append(getString(R.string.sensor_register_fail));
            fail();
            return;
        }
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
        setContentView(R.layout.msensor);

        bindView();
        getService();

        updateView(value);
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mSensorManager == null || mMSensorListener == null
                || mMSensor == null) {
            return;
        }
        mSensorManager.unregisterListener(mMSensorListener, mMSensor);
    }

    public class MSensorListener implements SensorEventListener {

        private int count = 0;

        public void onSensorChanged(SensorEvent event) {

            // MSensor event.value has 3 equal value.
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
