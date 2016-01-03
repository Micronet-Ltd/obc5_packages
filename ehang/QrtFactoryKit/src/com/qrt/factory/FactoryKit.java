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
import android.provider.Settings;

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
		int gps_en = 0;
		int bd_en = 0;

	    gps_en=Settings.Secure.getInt(mContext.getContentResolver(),Settings.Secure.TEXT_GPS_ENABLE,0);
	    bd_en=Settings.Secure.getInt(mContext.getContentResolver(),Settings.Secure.TEXT_BD_ENABLE,0);    
		
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
			       if(bd_en!=0&&gps_en!=0)
			       {
                      bootMode = 6;
			       }
				   else if(bd_en!=0)
			       {
                      bootMode = 5;
			       }
			       else
			       {
                      bootMode = 3;
			       } 
					
                   TestSettings.DEFAULT_FREQ =
                    		mContext.getResources().getInteger(R.integer.default_fm_freq);
                } else {
			       if(bd_en!=0&&gps_en!=0)
			       {
                      bootMode = 7;
			       }
			       else if(bd_en!=0)
			       {
                      bootMode = 4;
			       }
			       else
			       {
                      bootMode = 0;
			       }
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