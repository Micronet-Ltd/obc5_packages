/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.domain.TestKey;
import com.qrt.factory.util.Utilities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import android.util.Log;

public class TouchPanel extends AbstractActivity {

    private static final String TAG = "TouchPanelKey And Vibrate Test";

    private static final String APP_SWITCH_PRESS_ACTION
            = "com.qrt.factory.app_switch_key_pressed";

    int key = 0;

    int searchKeyDownCount = 0;

    private List<TestKey> mTestKeys = null;

    private TextView mTextView;

    private AlertDialog mDialog;

    private boolean keyTestPass;

	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; 

    @Override
    protected String getTag() {
        return TAG;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                keyTestFinish(true);
            } else if (msg.what == 1) {
                keyTestFinish(false);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

		Log.d(TAG, "TouchPanel!!!");

        searchKeyDownCount = 0;
        key = 0;
        mTestKeys = null;
        try {
            int panelKeyComfigRes = R.xml.panel_key_config;
            if ("1".equals(SystemProperties.get("ro.ftmtestmode"))) {
                panelKeyComfigRes = R.xml.panel_key_config_for_pcba;
            }

            mTestKeys = Utilities
                    .loadXmlForClass(TouchPanel.this, panelKeyComfigRes,
                            TestKey.class);
        } catch (Exception e) {
            loge("Exception :" + e);
        }

        if (mTestKeys == null || mTestKeys.size() == 0) {
            mResultBuffer.append("load keys xml fail");
            mHandler.sendEmptyMessage(0);
        }
        setContentView(R.layout.touch_panel_key);
        mTextView = (TextView) findViewById(R.id.key_text);
        mTextView.setText(getKeyName());

        mDialog = new AlertDialog.Builder(this)
                .setTitle(null)
                .create();
        mDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_KEYGUARD);
        mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
  
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (KeyEvent.ACTION_UP == event.getAction()) {
                    onKeyUp(keyCode, event);
                }
                return true;
            }
        });
        mDialog.setCancelable(false);
        if (!isFinishing()) {
            mDialog.show();
        }  
    }

    private String getKeyName() {
        if (key < mTestKeys.size()) {
            return getString(R.string.touch_panel_key_message,
                    mTestKeys.get(key).getName());
        }
        return "";
    }

    public boolean onKeyUp(int keyCode, KeyEvent msg) {
		Log.d(TAG, "onKeyUp onKeyDown : " + keyCode);

        keyPressed(keyCode);

        return true;
    }

	@Override
	public boolean onKeyDown( int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown onKeyDown : " + keyCode);
		
		if (keyCode == event.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

    private void keyPressed(int keyCode) {
        if (key >= mTestKeys.size()) {
            return;
        }

        if (mTestKeys.get(key).getKeyCode() == keyCode) {
            searchKeyDownCount = 0;
            key++;
            if (key >= mTestKeys.size()) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                mHandler.sendEmptyMessage(0);
//                pass();
            }
        } else {
            searchKeyDownCount++;
            if (searchKeyDownCount > 3) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                mHandler.sendEmptyMessage(1);
//                fail();
            }
        }

        mTextView.setText(getKeyName());
    }

    private BroadcastReceiver appSwitchRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (APP_SWITCH_PRESS_ACTION.equals(action)) {
                keyPressed(KeyEvent.KEYCODE_APP_SWITCH);
            }
        }
    };

    @Override
    protected void onResume() {
        /*qrt added by xuegang for Factory Test Key test bug 20150119 begin*/
        if (!isFinishing()) {
            mDialog.show();
        }
		/*qrt added by xuegang for Factory Test Key test bug 20150119 end*/
		
        registerReceiver(appSwitchRecevier,
                new IntentFilter(APP_SWITCH_PRESS_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        /*qrt added by xuegang for Factory Test Key test bug 20150119 begin*/
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }  
		/*qrt added by xuegang for Factory Test Key test bug 20150119 end*/
		
        unregisterReceiver(appSwitchRecevier);
        super.onPause();
    }

    private void keyTestFinish(boolean isPass) {
        mResultBuffer.append("TouchPanelKey Test " + (isPass ? "pass" : "fail"));
        keyTestPass = isPass;
        
        if (keyTestPass) {
            pass();
        } else {
            fail();
        }

    }

}
