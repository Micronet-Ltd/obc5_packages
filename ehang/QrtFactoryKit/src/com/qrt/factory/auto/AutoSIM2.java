package com.qrt.factory.auto;

import android.app.Service;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:10 Time: 15:38 To
 * change this template use File | Settings | File Templates.
 */
public class AutoSIM2 implements AutoTest {

    private static final String TAG = "SIM2 Test";

    private Context mContext;

    @Override
    public void initialize(Context context) {

        mContext = context;
    }

    @Override
    public TestResult doingBackground() {

        long startTime = System.currentTimeMillis();    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 */
        TestResult testResult = new TestResult();
		TelephonyManager mTelephonyManager =
				(TelephonyManager) mContext.getSystemService(
				Service.TELEPHONY_SERVICE);
		String IMSI = mTelephonyManager.getSubscriberId(1);

        if (!TextUtils.isEmpty(IMSI)) {
            testResult.appendResult("IMSI: " + IMSI);
            testResult.setPass(true);
        } else if (mTelephonyManager.getSimState(1) == TelephonyManager.SIM_STATE_READY) {
            testResult.appendResult("SIM1 State: Ready");
            testResult.setPass(true);
        } else {
            testResult.setPass(false);
        }
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
        long endTime = System.currentTimeMillis();
        String useTime = String.valueOf(Float.parseFloat(String.valueOf(endTime - startTime)) / 1000.0f);
        testResult.setTime(useTime);
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
        return testResult;
    }
}
