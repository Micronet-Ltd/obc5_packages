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

public class HightSensor extends AbstractActivity {

    private static final String TAG = "Pressure Sensor";

    private SensorManager mSensorManager = null;

    private Sensor mHSensor = null;

    private HSensorListener mHSensorListener;

    private TextView mTextView;

    private Button cancelButton;

    private final static String INIT_VALUE = "";

    private static String value = INIT_VALUE;

    private static String pre_value = INIT_VALUE;

    private final int MIN_COUNT = 15;

    private final static int SENSOR_TYPE = Sensor.TYPE_PRESSURE;

    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    private boolean pass = false;

    @Override
    public void finish() {

        try {
            mSensorManager.unregisterListener(mHSensorListener, mHSensor);
        } catch (Exception e) {
            Utilities.loge(TAG, e);
        }
        super.finish();
    }

    void bindView() {

        mTextView = (TextView) findViewById(R.id.hsensor_result);
        cancelButton = (Button) findViewById(R.id.hsensor_cancel);
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
			logd("HSensor getService fail!");
            fail();
        }

        mHSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mHSensor == null) {
            mResultBuffer.append(getString(R.string.sensor_get_fail));
			logd("HSensor getHSensorService fail!");
            fail();
        }

        mHSensorListener = new HSensorListener(this);
        if (!mSensorManager
                .registerListener(mHSensorListener, mHSensor, SENSOR_DELAY)) {
            mResultBuffer.append(getString(R.string.sensor_register_fail));
			logd("HSensor getHSensorListenerService fail!");
            fail();
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
        logd("onCreate HSensor View...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hsensor);

        bindView();
        getService();

        updateView(value);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSensorManager == null || mHSensorListener == null
                || mHSensor == null) {
            return;
        }
        mSensorManager.unregisterListener(mHSensorListener, mHSensor);
		pass = false;
    }

    public class HSensorListener implements SensorEventListener {

        private int count = 0;

        public HSensorListener(Context context) {

            super();
        }

        public void onSensorChanged(SensorEvent event) {

            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {
                    logd(event.values.length + ":" + event.values[0] + " ");
                    String value = "(" + event.values[0] + ")";
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

