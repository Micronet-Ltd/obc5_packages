package com.qrt.factory.auto;

import com.qrt.factory.R;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:10 Time: 16:34 To
 * change this template use File | Settings | File Templates.
 */
public class AutoMSensor implements AutoTest {

    private static final String TAG = "MSensor Test";

    private final static int SENSOR_TYPE = Sensor.TYPE_MAGNETIC_FIELD;

    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    private final int MIN_COUNT = 10;

    private Context mContext;

    private SensorManager mSensorManager = null;

    private Sensor mMSensor = null;

    private MSensorListener mMSensorListener;

    private String pre_value = "";

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

        if (mSensorManager != null && mMSensorListener != null
                && mMSensor != null) {
            mSensorManager.unregisterListener(mMSensorListener, mMSensor);
            mSensorManager = null;
            mMSensorListener = null;
            mMSensor = null;
        }

        return mTestResult;
    }

    void getService() {

        mSensorManager = (SensorManager) mContext
                .getSystemService(mContext.SENSOR_SERVICE);
        if (mSensorManager == null) {
            mTestResult.appendResult(
                    mContext.getString(R.string.service_get_fail));
            mTestResult.setPass(false);
            return;
        }

        mMSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mMSensor == null) {
            mTestResult
                    .appendResult(mContext.getString(R.string.sensor_get_fail));
            mTestResult.setPass(false);
            return;
        }

        mMSensorListener = new MSensorListener();
        if (!mSensorManager
                .registerListener(mMSensorListener, mMSensor, SENSOR_DELAY)) {
            mTestResult.appendResult(
                    mContext.getString(R.string.sensor_register_fail));
            mTestResult.setPass(false);
        }
    }

    public class MSensorListener implements SensorEventListener {

        private int count = 0;

        public void onSensorChanged(SensorEvent event) {

            // MSensor event.value has 3 equal value.
            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {
                    Log.d("Auto MSensor",
                            event.values.length + ":" + event.values[0] + " "
                                    + event.values[0] + " "
                                    + event.values[0] + " ");
                    String value = "(" + event.values[0] + ", "
                            + event.values[1] + ", "
                            + event.values[2] + ")";
                    if (value != pre_value) {
                        count++;
                    }
                    if (count >= MIN_COUNT && !pass) {
                        pass = true;
                        mTestResult.setPass(true);
                    }
                    pre_value = value;
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }
}
