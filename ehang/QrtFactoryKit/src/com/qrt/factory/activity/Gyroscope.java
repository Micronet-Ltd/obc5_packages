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
import java.text.DecimalFormat;

public class Gyroscope extends AbstractActivity {

    private static final String TAG = "Gyroscope Test";

    private SensorManager GyroscopeManager = null;

    private Sensor mGyroscope = null;

    private GyroscopeListener mGyroscopeListener;

    private TextView mTextView;

    private Button cancelButton;
    private Button passButton;   //baiwuqiang

    private final static String INIT_VALUE = "";

    private static String value = INIT_VALUE;

    private static String pre_value = INIT_VALUE;

    private final int MIN_COUNT = 20;

    private final static int SENSOR_TYPE = Sensor.TYPE_GYROSCOPE;

    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    private boolean pass = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mThreadHandler.removeCallbacks(mRunnable);
                startAdjustment = false;
                mFloats.clear();
                SystemProperties.set("runtime.factory.gyp", "0");
                mAdjustmentButton.setEnabled(true);
                mAdjustmentView.setText(msg.getData().getString("msg"));
                unregisterListener();
                getService();
            } else if (msg.what == 1) {
                mThreadHandler.removeCallbacks(mRunnable);
                startAdjustment = false;
                mFloats.clear();
                SystemProperties.set("runtime.factory.gyp", "0");
                mAdjustmentButton.setEnabled(true);
                mAdjustmentView.setText(R.string.fail);
            }
            super.handleMessage(msg);
        }
    };

    private Handler mThreadHandler;

    private TextView mAdjustmentView;

    private Button mAdjustmentButton;

    private boolean startAdjustment;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            while (mFloats.size() < 100) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}
            }

            int listSize = mFloats.size();
            logd("mFloats.size() = " + listSize);
            if (listSize > 0) {
                float sumX = 0;
                float sumY = 0;
                float sumZ = 0;
                for (int i = listSize - 1; i >= 0; i --) {
                    float[] aFloats = mFloats.get(i);
                    sumX += aFloats[0];
                    sumY += aFloats[1];
                    sumZ += aFloats[2];
                }

                float adjustmentValueX = new BigDecimal(0f - sumX / listSize)
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                float adjustmentValueY = new BigDecimal(0f - sumY / listSize)
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                float adjustmentValueZ = new BigDecimal(0f - sumZ / listSize)
                        .setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();

                logd("adjustmentValueX = " + adjustmentValueX
                        + " adjustmentValueY = " + adjustmentValueY
                        + " adjustmentValueZ = " + adjustmentValueZ);
                String adjustmentValueStr =  adjustmentValueX + "\n"
                        + adjustmentValueY + "\n"
                        + adjustmentValueZ;
                if (Utilities.writeToFile("/persist/gyp_avg", adjustmentValueStr)) {

                    Message msg = new Message();
                    msg.what = 0;
                    Bundle data = new Bundle();
                    data.putString("msg", getString(R.string.adjustment_ok) +"\n" +
                            " offset X : " + adjustmentValueX + "\n" +
                            " offset Y : " + adjustmentValueY + "\n" +
                            " offset Z : " + adjustmentValueZ );
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                } else {
                    logd("writeToFile fail");
                    mHandler.sendEmptyMessage(1);
                }
            } else {
                mHandler.sendEmptyMessage(1);
            }
        }
    };

    private List<float[]> mFloats = new ArrayList<float[]>();

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
        passButton = (Button) findViewById(R.id.gyroscope_pass);     //baiwuqiang
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });
        passButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                    pass = true;
                    pass();
            }
        });
        mAdjustmentView = (TextView) findViewById(R.id.adjustment_value);
        mAdjustmentButton = (Button) findViewById(R.id.adjustment_btn);
        mAdjustmentButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mAdjustmentView.setText(R.string.adjustment_start);
                mAdjustmentButton.setEnabled(false);
                SystemProperties.set("runtime.factory.gyp", "1");
                mFloats.clear();
                startAdjustment = true;
                mThreadHandler.post(mRunnable);
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
        setContentView(R.layout.gyroscope);

        HandlerThread handlerThread = new HandlerThread("gyroscopeHandlerThread");
        handlerThread.start();
        mThreadHandler = new Handler(handlerThread.getLooper());
        bindView();
        getService();

        updateView(value);
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
                    String value0Str;
					String value1Str;
					String value2Str;

					DecimalFormat decimalFormat=new DecimalFormat("#0.00");
					value0Str=decimalFormat.format(value0);
					value1Str=decimalFormat.format(value1);
					value2Str=decimalFormat.format(value2);

					logd(event.values.length + ":" + value0Str + " "
                            + value1Str + " "
                            + value2Str + " ");
				
                    String value = "\n X = " + value0Str + ", \n Y = "
                            + value1Str + ", \n Z = "
                            + value2Str;
                    /*updateView(value);
                    if (value != pre_value) {
                        count++;
                    }
                    if (count >= MIN_COUNT && !pass) {
                        pass = true;
                 //       pass();    // baiwuqiang 
                    }*/
                    if (startAdjustment && mFloats.size() < 100) {
                        logd("add " + value);
                        mFloats.add(new float[]{value0, value1, value2});
                    } else {
                        updateView(value);
                    }

                    pre_value = value;
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }
}
