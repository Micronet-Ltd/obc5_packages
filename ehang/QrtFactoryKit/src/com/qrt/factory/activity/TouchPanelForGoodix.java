package com.qrt.factory.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by wangwenlong on 13-11-15.
 */
public class TouchPanelForGoodix extends AbstractActivity {
    private static final String TAG = "TouchPanelForGoodix";

    private static final int CHANNEL_PASS = 0;

    private BroadcastReceiver receiver = null;

    @Override
    protected String getTag() {
        return TAG;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.goodix.rawdata",
                "com.goodix.rawdata.RawDataTest"));
        intent.putExtra("command", 1);
        intent.putExtra("frequences", 1);
        intent.putExtra("autofinish", true);
		intent.putExtra("successfinish", true);
        /*Modify by zhangkaikai for ActivityNotFoundException 2014-10-16 */
        try {
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            loge(e);
            finish();
        }
        /*Modify by zhangkaikai for ActivityNotFoundException 2014-10-16 */
        registerReceiver();
    }

    @Override
    public void finish() {
    	/*delete by bwq for unregisterReceiver 20140913 begin*/
    	/*if (receiver != null) {
            unregisterReceiver(receiver);        
        }*/
        /*delete by bwq for unregisterReceiver 20140913 end*/
        super.finish();
    }

    private void registerReceiver() {
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null || intent.getAction() == null) return;

                    Log.d(TAG, "Intent Action : " + intent.getAction());

                    Bundle bundle = intent.getExtras();
                    Log.d(TAG, "intent.getExtras : " + bundle);
                    if (bundle != null) {
                        for (String s : bundle.keySet()) {
                            Log.d(TAG, "Intent Extras key : " + s + " ,value : " + bundle.get(s));
                        }
                    }
                    int result = intent.getIntExtra("testResult", -1);
                    if (result == CHANNEL_PASS) {
                        pass();
                    } else {
                        /*BEYOND_MAX_LIMIT = 0X0001; //最大值超过设定值
                        BEYOND_MIN_LIMIT = 0x0002; //最小值超过设定值
                        BEYOND_ACCORD_LIMIT = 0x0004; //最大相邻数据偏差比值超过设定值
                        BEYOND_OFFSET_LIMIT = 0x0008; //整屏数据最大偏差比值超过设定值
                        BEYOND_JITTER_LIMIT = 0x0010; //整屏数据的最大抖动查过设定值
                        KEY_BEYOND_MAX_LIMIT = 0x1000; //按键超过最大值
                        KEY_BEYOND_MIN_LIMIT = 0x2000; //按键小于最小值
                        MODULE_TYPE_ERR = 0x00080000; //模组类型错误
                        VERSION_ERR = 0x10000; //版本号不匹配
                        GT_SHORT = 0x00400000; //Guitar短路*/
                        mResultBuffer.append("\nError code : " + result);
                        fail();
                    }
                    unregisterReceiver(receiver);                          //add by bwq for unregisterReceiver 20140913
                }
            };
        }
        Log.d(TAG, "registerReceiver android.intent.action.goodix");
        registerReceiver(receiver, new IntentFilter("android.intent.action.goodix"));
    }

}