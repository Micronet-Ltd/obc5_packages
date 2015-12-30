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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdjustProximitySensor extends AbstractActivity {// modify by zhangkaikai to change name for adjust HQ00000000 2014-08-27

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
    // add by zhangkaikai to get P file 20140827
    private static final String P_SENSOR_FILE_VALUE_PATH = "/sys/class/input";
    
    private boolean pass = false;

    private TextView mVersionView;

    private List<Float> mFloats = new ArrayList<Float>();

    private Button passButton,mAdjustmentButton;

    private TextView mAdjustmentView,mTestResultView;//Modify by zhangkaikai for pass tip QL810 SW00085303 20141011

    private boolean startAdjustment = false , showpass = false;//Modify by zhangkaikai for pass tip QL810 SW00085303 20141011
 // add by zhangkaikai to get P file 20140827
    private static String PORX_SENSOR_FILE;
    private String mProxSensorInfo = null;
    private Float mProxSensorValue = 0f;
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mThreadHandler.removeCallbacks(mRunnable);
                mFloats.clear();
                startAdjustment = false;
                SystemProperties.set("runtime.factory.prox", "0");
                mAdjustmentButton.setEnabled(true);
                //passButton.setEnabled(true);//Modify by zhangkaikai for pass tip QL810 SW00085303 20141011
                mAdjustmentView.setText(msg.getData().getString("msg"));
                unregisterListener();
                getService();
            } else if (msg.what == 1) {
                mThreadHandler.removeCallbacks(mRunnable);
                mFloats.clear();
                startAdjustment = false;
                SystemProperties.set("runtime.factory.prox", "0");
                mAdjustmentButton.setEnabled(true);
                mAdjustmentView.setText(R.string.fail);
            }
            super.handleMessage(msg);
        }
    };

    private Handler mThreadHandler;

    private HandlerThread mHandlerThread;

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
        mAdjustmentView = (TextView) findViewById(R.id.adjustment_value);

     // modify by zhangkaikai to disable passButton before Adjust HQ00000000 2014-08-27
        passButton = (Button) findViewById(R.id.psensor_pass);
        passButton.setEnabled(false);
        passButton.setOnClickListener(new View.OnClickListener() {
            // modify by zhangkaikai to disable passButton before Adjust HQ00000000 2014-08-27

            public void onClick(View v) {
                pass();
            }
        });

        Button cancel = (Button) findViewById(R.id.psensor_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });

        mAdjustmentButton = (Button) findViewById(R.id.adjustment_btn);
        mAdjustmentButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                showpass = true;//Modify by zhangkaikai for pass tip QL810 SW00085303 20141011
                mAdjustmentView.setText(R.string.adjustment_start);
                mAdjustmentButton.setEnabled(false);
                mTestResultView.setVisibility(View.GONE);//Modify by zhangkaikai for pass tip QL810 SW00085303 20141011
                passButton.setEnabled(false);// modify by zhangkaikai to disable passButton before Adjust HQ00000000 2014-08-27
                // add by zhangkaikai to get P file 20140827 begin
                File f = new File(P_SENSOR_FILE_VALUE_PATH);
                PORX_SENSOR_FILE = FindFile(f, "ps_cal");
                Log.d("adjustprox", "ps_file == " + PORX_SENSOR_FILE);
                mProxSensorInfo = Utilities
                        .getFileInfo(PORX_SENSOR_FILE);
                if (mProxSensorInfo != null) {
                    mProxSensorValue = Float.parseFloat(mProxSensorInfo);
                    Log.d("adjustprox", "mProxSensorValue == " + mProxSensorValue);
                }
                // SystemProperties.set("runtime.factory.prox", "1");
                // mFloats.clear();
                // add by zhangkaikai to get P file 20140827 end
                startAdjustment = true;
                mThreadHandler.post(mRunnable);
            }
        });
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            int count = 0;
            while (count < 5) {
                try {
                    Thread.sleep(1000);
                    count++;
                } catch (InterruptedException ignored) {}
            }

            logd("mFloats.size() = " + mFloats.size());
            // modify by zhangkaikai to add adjust HQ00000000 2014-08-27 begin
            // if (mFloats.size() > 0) {
            if (mProxSensorValue > 0) {
                /*
                 * float f = 0; for (Float aFloat : mFloats) { f += aFloat; }
                 * int adjustmentValue = Math.round(f / mFloats.size());
                 */
                int adjustmentValue = Math.round(mProxSensorValue);
                logd("adjustmentValue = " + adjustmentValue);
                Log.d("adjustprox", "adjustmentValue == " + adjustmentValue);
                // modify by zhangkaikai to add adjust HQ00000000 2014-08-27 end
                if (Utilities.writeToFile("/persist/prox_avg",
                        String.valueOf(adjustmentValue))) {

                    if (adjustmentValue > 65535) {
                        mHandler.sendEmptyMessage(1);
                        return;
                    }
                    Message msg = new Message();
                    msg.what = 0;
                    Bundle data = new Bundle();
                    data.putString("msg", getString(R.string.adjustment_ok) +"\n" +
                            " Crosstalk : " + adjustmentValue + "\n" +
                            // modify by zhangkaikai to change adjustmentValue HQ00000000 2014-08-08 begin
                            " Threshold High : " + (adjustmentValue + 390) + "\n" +
                            " Threshold Low : " + (adjustmentValue + 290));
                    // modify by zhangkaikai to change adjustmentValue HQ00000000 2014-08-08 end
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
                .getSensorInfoByCode(AdjustProximitySensor.this, Utilities// modify by zhangkaikai to change name for adjust HQ00000000 2014-08-27
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
        setContentView(R.layout.psensor_adjust);// modify by zhangkaikai to change name for adjust HQ00000000 2014-08-27
        mTestResultView = (TextView) findViewById(R.id.result_success);//Modify by zhangkaikai for pass tip QL810 SW00085303 20141011

        mHandlerThread = new HandlerThread("sensorHandlerThread");
        mHandlerThread.start();
        mThreadHandler = new Handler(mHandlerThread.getLooper());
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
        mThreadHandler.removeCallbacks(mRunnable);
        mHandlerThread.quit();
        unregisterListener();
    }

    private void unregisterListener() {
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
                            + event.values[1] + " "
                            + event.values[2] + " ");

                    if (startAdjustment) {
                        logd("add " + value);
                        mFloats.add(value);
                    } else {
                        updateView(value);
                      /*Modify by zhangkaikai for pass tip QL810 SW00085303 20141011 begin*/
                        if(value<5.0f && showpass){
                            mTestResultView.setVisibility(View.VISIBLE);
                            mTestResultView.setText(R.string.test_pass);
                            mTestResultView.setTextColor(android.graphics.Color.GREEN);
                            passButton.setEnabled(true);//add by zhangkaikai  to disable passButton before Adjust (QL1700)HQ00000000 2014-08-07
                        }
                            /*Modify by zhangkaikai for pass tip QL810 SW00085303 20141011 end*/
                    }

                    /*// 1(no covered)->0(covered)
                    if (value != INIT_VALUE && value != pre_value) {
                        count++;
                    }
                    if (count >= MIN_COUNT && !pass) {
                        pass = true;
                        pass();
                    }
                    pre_value = value;*/
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }
    
    // add by zhangkaikai to get P file 20140808 begin
    private static String FindFile(File file, String key_search)
    {
        if ((file == null) || (file.getName() == "")) {
            Log.d("HallActivity", "paramter invalid");
            return null;
        }

        if (file.getAbsolutePath().length() > 45) {
            return null;
        }
        if (file.isDirectory()) {
            Log.d("proxActivity", "file.isDirectory()== " + file.getName());
            File[] all_file = file.listFiles();
            // Log.d("proxActivity", "all_file count: "+all_file.size);
            if (all_file != null) {
                for (File tempf : all_file) {
                    if (tempf.isDirectory()) {
                        Log.d("proxActivity", "check file dir: == " + tempf.getName());
                        String result = "";
                        if (tempf.getAbsolutePath().startsWith("/sys/class/input/input")) {
                            result = FindFile(tempf, key_search);
                            if (result != null && !result.equals(""))
                                return result;
                        }
                    }
                    else
                    {
                        Log.d("proxActivity", "check file:" + tempf.getAbsolutePath());
                        if (tempf.getName().equals(key_search))
                        {
                            Log.d("proxActivity", "find path:" + tempf.getAbsolutePath());
                            return tempf.getAbsolutePath();
                        }
                    }
                }
            }

        } else if (file.getName().equals(key_search))
        {
            Log.d("proxActivity", " check file second:" + file.getAbsolutePath());
            return file.getAbsolutePath();
        }
        return null;
    }
    // add by zhangkaikai to get P file 20140808 end
}
