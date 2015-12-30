package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by IntelliJ IDEA. User: wangwenlong Date: 12-2-22 Time: 上午10:44 To
 * change this template use File | Settings | File Templates.
 */
public class HeadsetKey extends AbstractActivity {

    private static final String TAG = "Headset key Test";

    private static final String mPath = "/sys/class/switch/h2w/state";

    private boolean isPass = false;

    private Button mFailButton;

    private TextView mHeadsetKeyInsertHeadsetView = null;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.headset_key);
        isPass = false;

//        Button pass = (Button) findViewById(R.id.pass);
//        pass.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                if(isPass) {
//                    pass();
//                } else {
//                    if (!isFinishing()) {
//                        showWarningDialog(HeadsetKey.this, getString(R.string.error_pass));
//                    }
//                }
//            }
//        });

        mFailButton = (Button) findViewById(R.id.fail);
        mFailButton.setClickable(true);
        mFailButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                fail();
            }
        });

        mHeadsetKeyInsertHeadsetView = (TextView) findViewById(
                R.id.headset_key_insert_headset);
        mHeadsetKeyInsertHeadsetView.setVisibility(View.GONE);
        String headsetState = Utilities.getFileInfo(mPath);
        if (!"1".equals(headsetState)) {
            if (!isFinishing()) {
                mHeadsetKeyInsertHeadsetView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        registerReceiver(headsetRecevier,
                new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(headsetRecevier);
        super.onPause();
    }

    private final BroadcastReceiver headsetRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                int state = intent.getIntExtra("state", 0);

                if (state == 1) { // 插入耳机
                    mHeadsetKeyInsertHeadsetView.setVisibility(View.GONE);
                } else { // 拔出耳机
                    mHeadsetKeyInsertHeadsetView.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!isFinishing() && isPass) {
                pass();
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            TextView keyText = (TextView) findViewById(R.id.headsetkeyText);
            isPass = true;
            mFailButton.setClickable(false);
            mHandler.sendEmptyMessageDelayed(0, 1000);
            keyText.setBackgroundResource(R.color.green);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
