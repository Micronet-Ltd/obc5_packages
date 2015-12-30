package com.qrt.factory.auto;

import android.content.Context;
import static com.qrt.factory.TestSettings.WIFI_RESULT;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:14 Time: 11:07 To
 * change this template use File | Settings | File Templates.
 */
public class AutoWifi implements AutoTest {

    private static final String TAG = "WiFi Test";

    private TestResult mTestResult;
    String endTime;    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 */

    @Override
    public void initialize(Context context) {
        mTestResult = new TestResult();
    }

    @Override
    public TestResult doingBackground() throws InterruptedException {

        mTestResult.setPass(WIFI_RESULT);
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
        if(!WIFI_RESULT){
            endTime = String.valueOf(System.currentTimeMillis());
       }
       mTestResult.setTime(endTime);
       /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 end*/
        return mTestResult;
    }
}
