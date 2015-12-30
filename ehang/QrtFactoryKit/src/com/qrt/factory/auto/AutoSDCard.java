package com.qrt.factory.auto;

import com.qrt.factory.R;

import android.content.Context;
import android.os.Environment;
import android.os.Message;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:10 Time: 16:45 To
 * change this template use File | Settings | File Templates.
 */
public class AutoSDCard implements AutoTest {

    private static final String TAG = "SDCard Test";

    private Context mContext;

    @Override
    public void initialize(Context context) {

        mContext = context;
    }

    @Override
    public TestResult doingBackground() throws InterruptedException {

        long startTime = System.currentTimeMillis();    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 */
        TestResult testResult = new TestResult();

        try {

            String mSdCardPath = mContext.getString(R.string.default_sd_path,
                    "/storage/sdcard1");

            Process mProcess;
            String paras[] = "mount".split(",");

            mProcess = Runtime.getRuntime().exec(paras);
            mProcess.waitFor();

            InputStream inStream = mProcess.getInputStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader inBuffer = new BufferedReader(inReader);
            String s;
            String data = "";
            while ((s = inBuffer.readLine()) != null) {
                data += s + "\n";
            }
            mProcess.exitValue();
            testResult.setPass(data.contains(mSdCardPath));

        } catch (Exception e) {
            testResult.setPass(false);
        }
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
        long endTime = System.currentTimeMillis();
        String useTime = String.valueOf(Float.parseFloat(String.valueOf(endTime - startTime)) / 1000.0f);
        testResult.setTime(useTime);
        /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 end*/
        return testResult;
    }
}
