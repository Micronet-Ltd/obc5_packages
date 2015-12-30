/*add by tianfangzhou for autotest abort 2013.5.21*/
package com.qrt.factory;
import android.app.Application;
import android.os.SystemProperties;
import java.util.ArrayList;
import java.util.List;
import com.qrt.factory.domain.TestItem;
import com.qrt.factory.util.XmlUtil;
import android.util.Log;
import android.content.Intent;
import android.content.Context;

public class FactoryKit extends Application{

   private static final String TAG = "Control Center";
    public static boolean isAutoTesting = false;
	public static  List<TestItem> mItemList = new ArrayList<TestItem>();
	public static List<TestItem> mAutoTestItemList;
    public static List<TestItem> mUserTestItemList;
	public static int testMode = -1;
	private static Context mContext;

    static {
        Log.d(TAG,"Loading FM-JNI Library");
        System.loadLibrary("qcomfm_jni");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getBaseContext();
    }

	 private static TestItem createAutoTestItem() {
        TestItem testItem = new TestItem();
        testItem.setTitle(mContext.getString(R.string.auto_test));
        return testItem;
    }

    private static TestItem createVersionTestItem() {
        TestItem testItem = new TestItem();
        testItem.setTitle(mContext.getString(R.string.version_info));
        return testItem;
    }
    
    public static void initItemList(Intent intent){
    	if(testMode != -1){
    		return;
    	}
    	
    	List<TestItem>[] testItems = null;
        try {
            int bootMode = 0;
            if (intent.getBooleanExtra("attachment", false)) {
                bootMode = 2;
            } else {
                if ("1".equals(SystemProperties.get("ro.ftmtestmode"))) {
                    bootMode = 1;
                    TestSettings.DEFAULT_FREQ =
                    		mContext.getResources().getInteger(R.integer.default_fm_freq_for_pcba);
				/*qrt added by xuegang for auto sim test 20141120 begin*/	
                } else if ("dsds".equals(SystemProperties.get("persist.radio.multisim.config"))) {
                    bootMode = 3;
                    TestSettings.DEFAULT_FREQ =
                    		mContext.getResources().getInteger(R.integer.default_fm_freq);
                } else {
                    bootMode = 0;
                    TestSettings.DEFAULT_FREQ =
                    		mContext.getResources().getInteger(R.integer.default_fm_freq);
                }
            }
            testMode = bootMode;

            testItems = XmlUtil.loadTestItems(mContext, bootMode);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            Log.e(TAG, "loadTestItems Error Please Check Xml File");
            
        }

        mItemList = new ArrayList<TestItem>();
        if (testItems != null) {
            mAutoTestItemList = testItems[0];
            mItemList.add(createAutoTestItem());
            mItemList.addAll(mAutoTestItemList);
            mUserTestItemList = testItems[1];
            mItemList.addAll(mUserTestItemList);
            mItemList.add(createVersionTestItem());
        }		
    }
} 