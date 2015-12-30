package com.qrt.factory.auto;

import android.app.Service;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:10 Time: 15:37 To
 * change this template use File | Settings | File Templates.
 */
public class AutoSIM1 implements AutoTest {

    private static final String TAG = "SIM1 Test";

    private Context mContext;

    @Override
    public void initialize(Context context) {

        mContext = context;
    }

    @Override
    public TestResult doingBackground() {

        long startTime = System.currentTimeMillis();    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 */
        TestResult testResult = new TestResult();

        String IMSI = "";
        int simState = TelephonyManager.SIM_STATE_UNKNOWN;

            TelephonyManager mTelephonyManager =
                    (TelephonyManager) mContext.getSystemService(
                    Service.TELEPHONY_SERVICE);
            IMSI = mTelephonyManager.getSubscriberId(0);
            simState = mTelephonyManager.getSimState(0);

        if (!TextUtils.isEmpty(IMSI)) {
            testResult.appendResult("IMSI: " + IMSI + "\n");
            testResult.setPass(true);
        } else {
            if (simState == TelephonyManager.SIM_STATE_READY) {
                testResult.appendResult("SIM1 State: Ready" + "\n");
                testResult.setPass(true);
            } else {
                testResult.setPass(false);
            }
        }
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
        long endTime = System.currentTimeMillis();
        String useTime = String.valueOf(Float.parseFloat(String.valueOf(endTime - startTime)) / 1000.0f);
        testResult.setTime(useTime);
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 end*/
        return testResult;
    }
}
