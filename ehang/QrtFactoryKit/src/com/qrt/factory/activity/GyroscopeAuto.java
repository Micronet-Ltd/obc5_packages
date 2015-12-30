/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.android.internal.util.ArrayUtils;
import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class GyroscopeAuto extends AbstractActivity {

    private static final String TAG = "Gyroscope Test";

    private SensorManager GyroscopeManager = null;

    private Sensor mGyroscope = null;

    private GyroscopeListener mGyroscopeListener;

    private TextView mTextView;

    private Button cancelButton;

    private final static String INIT_VALUE = "";

    private static String value = INIT_VALUE;

    private static String pre_value = INIT_VALUE;
	
    private int count = 0;

    private final int MIN_COUNT = 20;

    private final static int SENSOR_TYPE = Sensor.TYPE_GYROSCOPE;

    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    private boolean pass = false;

    @Override
    public void finish() {

        try {
            GyroscopeManager.unregisterListener(mGyroscopeListener, mGyroscope);
        } catch (Exception e) {
            loge(e);
        }
        super.finish();
    }

    void bindView() {

        mTextView = (TextView) findViewById(R.id.gyroscope_result);
        cancelButton = (Button) findViewById(R.id.gyroscope_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });
    }

    void getService() {

        GyroscopeManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (GyroscopeManager == null) {
            mResultBuffer.append(getString(R.string.service_get_fail));
            fail();
            return;
        }

        mGyroscope = GyroscopeManager.getDefaultSensor(SENSOR_TYPE);
        if (mGyroscope == null) {
            mResultBuffer.append(getString(R.string.sensor_get_fail));
            fail();
            return;
        }

        mGyroscopeListener = new GyroscopeListener(this);
        if (!GyroscopeManager.registerListener(mGyroscopeListener, mGyroscope,
                SENSOR_DELAY)) {
            mResultBuffer.append(getString(R.string.sensor_register_fail));
            fail();
            return;
        }
    }

    void updateView(Object s) {
    	 mTextView.setTextSize(22);   //baiwuqiang 
        mTextView.setText(TAG + " : " + s);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gyroscope_auto);

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

        unregisterListener();
    }

    private void unregisterListener() {
        if (GyroscopeManager == null || mGyroscopeListener == null
                || mGyroscope == null) {
            return;
        }
        GyroscopeManager.unregisterListener(mGyroscopeListener, mGyroscope);
		pass = false;
    }

    public class GyroscopeListener implements SensorEventListener {

        private int count = 0;

        public GyroscopeListener(Context context) {

            super();
        }

        public void onSensorChanged(SensorEvent event) {

            // Gyroscope event.value has 3 equal value.
            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {
                    float value0 = event.values[0];
                    float value1 = event.values[1];
                    float value2 = event.values[2];
                    logd(event.values.length + ":" + value0 + " "
                            + value1 + " "
                            + value2 + " ");
                    String value = "\n X = " + value0 + ", \n Y = "
                            + value1 + ", \n Z = "
                            + value2;
                    updateView(value);
                    if (value != pre_value) {
                        count++;
                    }
                    if (count >= MIN_COUNT && !pass) {
                        pass = true;
                        pass();    // baiwuqiang 
                    }
                    pre_value = value;
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }
}
