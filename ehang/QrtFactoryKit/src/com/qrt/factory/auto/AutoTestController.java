package com.qrt.factory.auto;

import com.qrt.factory.R;
import com.qrt.factory.domain.TestItem;
import com.qrt.factory.util.Utilities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: wangwenlong Date: 12-1-9 Time: 上午9:49 To
 * change this template use File | Settings | File Templates.
 */
public class AutoTestController {

    private final Context mContext;

    private final Handler mHandler;

    private List<TestItem> mAutoTestItems;

    private Thread thread = null;

    private boolean testFinish;

    public AutoTestController(Context context, Handler handler,
            List<TestItem> autoTestItems) {

        mContext = context;
        mHandler = handler;
        mAutoTestItems = autoTestItems;
    }

    public void setTestFinish(boolean testFinish) {
        this.testFinish = testFinish;
    }

    public void startTestInBackground() {
        if (thread != null && thread.isAlive()) {
            try {
                thread.interrupt();
            } catch (Exception e) {

            }
            thread = null;
        }
        thread = new DoThread();
        thread.start();
    }

    private class DoThread extends Thread {

        public DoThread() {
            super();
            testFinish = false;
        }

        @Override
        public void run() {
            Map<String, AutoTest> map = new HashMap();
            map.put("Sim1", new AutoSIM1());
            map.put("Sim2", new AutoSIM2());
            map.put("SdCard", new AutoSDCard());
            map.put("Battery", new AutoBattery());
            map.put("GravitySensorAuto", new AutoGSensor());	
            map.put("GyroscopeAuto", new AutoGypSensor());				
            map.put("MagneticSensor", new AutoMSensor());
			map.put("HightSensor", new AutoHSensor());	
            map.put("Bluetooth", new AutoBluetooth());
            map.put("WiFi", new AutoWifi());
            map.put("Gps", new AutoGPS());

            mContext.getMainLooper().prepare();
            for (TestItem autoTestItem : mAutoTestItems) {
                Utilities.loge("AUTO_TEST", autoTestItem.getName());
               // if (testFinish) {
                 //   mHandler.sendEmptyMessage(3);
                 //   continue;
                //}
                Utilities.loge("AUTO_TEST", "testFinish = " + testFinish);
                AutoTest autoTest = map.get(autoTestItem.getName());
                if (autoTest != null) {
                    autoTest.initialize(mContext);
                    TestResult testResult = null;
                    try {
                        testResult = autoTest.doingBackground();
                    } catch (InterruptedException ignored) {

                    }
                    if (null != testResult) {
                        Log.d("Auto Test result",
                                testResult.isPass() + " : " + testResult
                                        .getResult());
                        Message message = new Message();
                        message.what = 1;
                        Bundle data = new Bundle();
                        data.putBoolean("pass", testResult.isPass());
                        data.putString("title", autoTestItem.getTitle());
                        data.putString("result", testResult.getResult());
                        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
                        if(testResult.getTime()!=null){
                            data.putString("time", testResult.getTime());
                        }
                        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 end*/
                        message.setData(data);
                        mHandler.sendMessage(message);
                    }
                }
            }

            mHandler.sendEmptyMessage(2);
        }
    }
}