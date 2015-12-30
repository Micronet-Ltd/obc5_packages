package com.qrt.factory.auto;

import android.content.Context;
import static com.qrt.factory.TestSettings.GPS_RESULT;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:14 Time: 15:09 To
 * change this template use File | Settings | File Templates.
 */
public class AutoGPS implements AutoTest {

    private static final String TAG = "GPS Test";

    private Context mContext;

    private TestResult mTestResult;
    String endTime;    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 */

    @Override
    public void initialize(Context context) {

        mContext = context;
        mTestResult = new TestResult();
    }

    @Override
    public TestResult doingBackground() throws InterruptedException {

        mTestResult.setPass(GPS_RESULT);
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
        if(!GPS_RESULT){
             endTime = String.valueOf(System.currentTimeMillis());
        }
        mTestResult.setTime(endTime);
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 end*/
        return mTestResult;
    }

}
