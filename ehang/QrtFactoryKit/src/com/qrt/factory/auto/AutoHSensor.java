package com.qrt.factory.auto;

import com.qrt.factory.R;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:10 Time: 15:44 To
 * change this template use File | Settings | File Templates.
 */
public class AutoHSensor implements AutoTest {

    private static final String TAG = "Press Test";

    private final static int SENSOR_TYPE = Sensor.TYPE_PRESSURE;

    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    private Context mContext;

    private SensorManager mSensorManager = null;

    private Sensor mHSensor = null;

    private HSensorListener mHSensorListener;

    private String pre_value = "";

    private final int MIN_COUNT = 15;

    private boolean pass = false;

    TestResult mTestResult = new TestResult();

    @Override
    public void initialize(Context context) {

        mContext = context;
    }

    @Override
    public TestResult doingBackground() throws InterruptedException {
        getService();

        int i = 0;
        while (!pass && i < 15) {
            Thread.sleep(1000);
        }

        if (mSensorManager != null && mHSensorListener != null
                && mHSensor != null) {
            mSensorManager.unregisterListener(mHSensorListener, mHSensor);
            mSensorManager = null;
            mHSensorListener = null;
            mHSensor = null;
        }

        return mTestResult;
    }

    private void getService() {

        mSensorManager = (SensorManager) mContext.getSystemService(
                mContext.SENSOR_SERVICE);
        if (mSensorManager == null) {
            mTestResult.appendResult(
                    mContext.getString(R.string.service_get_fail));
            mTestResult.setPass(false);
        }

        mHSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mHSensor == null) {
            mTestResult.appendResult(
                    mContext.getString(R.string.sensor_get_fail));
            mTestResult.setPass(false);
        }

        mHSensorListener = new HSensorListener();
        if (!mSensorManager
                .registerListener(mHSensorListener, mHSensor, SENSOR_DELAY)) {
            mTestResult.appendResult(
                    mContext.getString(R.string.sensor_register_fail));
            mTestResult.setPass(false);
        }
    }

    public class HSensorListener implements SensorEventListener {

        private int count = 0;

        public void onSensorChanged(SensorEvent event) {

            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {
                    Log.d("Auto HSensor",
                            event.values.length + ":" + event.values[0] + " ");
                    String value = "(" + event.values[0] + ")";
                    if (value != pre_value) {
                        count++;
                    }
                    if (count >= MIN_COUNT && !pass) {
                        mTestResult.setPass(true);
                        pass = true;
                    }
                    pre_value = value;
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }
}
