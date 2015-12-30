/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;


import com.qrt.factory.R;
import com.qrt.factory.domain.PadTestKey;
import com.qrt.factory.util.Utilities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class KeypadAndVibrate extends AbstractActivity {

    private static final String TAG = "Keypad Test";

    private Button mFailButton;

    private List<PadTestKey> mTestKeys;

    private TextView mTextView;

    private final long VIBRATOR_ON_TIME = 1000;

    private final long VIBRATOR_OFF_TIME = 500;

    private Vibrator mVibrator = null;

    private long[] pattern = {
            VIBRATOR_OFF_TIME, VIBRATOR_ON_TIME
    };

    private boolean keyTestPass;

    @Override
    protected String getTag() {
        return TAG;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (mTestKeys != null && mTestKeys.size() > 0) {
                    SpannableString ss = new SpannableString(
                            mTextView.getText());

                    String text = mTextView.getText().toString();
                    for (PadTestKey testKey : mTestKeys) {

                        String str = testKey.getText();
                        int start = text.indexOf(str);

                        int color = testKey.isPass() ? Color.GREEN
                                : Color.WHITE;
                        ss.setSpan(new ForegroundColorSpan(color),
                                start, start + str.length() + "\n".length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    mTextView.setText(ss);
                }
            } else if (msg.what == 2) {
                keyTestFinish(true);
            } else if (msg.what == 3) {
                keyTestFinish(false);
            }
        }
    };

    private void keyTestFinish(boolean isPass) {
        mResultBuffer.append("Keypad Test ").append(isPass ? "pass" : "fail");
        keyTestPass = isPass;

        if (!isFinishing()) {
            AlertDialog alertDialog = createConfirmDialog(KeypadAndVibrate.this,
                    getString(R.string.vibrate_confirm),
                    getString(R.string.pass),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            vibrateTestFinish(true);
                        }
                    }, getString(R.string.fail),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            vibrateTestFinish(false);
                        }
                    }
            );
            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i,
                        KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        int keyCode = keyEvent.getKeyCode();
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            dialogInterface.dismiss();
                            vibrateTestFinish(false);
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            dialogInterface.dismiss();
                            vibrateTestFinish(true);
                            return true;
                        }
                    }
                    return false;
                }
            });
            alertDialog.show();
        }
    }

    private void vibrateTestFinish(boolean isPass) {
        mResultBuffer.append("\nVibrate Test ").append(isPass ? "pass" : "fail");

        if (isPass && keyTestPass) {
            pass();
        } else {
            fail();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.keypad);

        mTestKeys = null;
        try {
            mTestKeys = Utilities
                    .loadXmlForClass(KeypadAndVibrate.this,
                            R.xml.key_pad_config,
                            PadTestKey.class);
        } catch (Exception e) {
            loge("Exception :" + e);
        }

        if (mTestKeys == null || mTestKeys.size() == 0) {
            mResultBuffer.append("load keys xml fail");
            mHandler.sendEmptyMessage(3);
        }

        StringBuilder buffer = new StringBuilder();

        if (mTestKeys != null) {
            for (PadTestKey testKey : mTestKeys) {

                String s = getString(getResources()
                        .getIdentifier(testKey.getText(), "string",
                                getPackageName()));
                testKey.setText(s);
                buffer.append(s).append("\n");
            }
        }

        mTextView = (TextView) findViewById(R.id.keypad_text);
        mTextView.setText(buffer.toString());

        mFailButton = (Button) findViewById(R.id.fail);
        mFailButton.setClickable(true);
        mFailButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mHandler.sendEmptyMessage(3);
            }
        });
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onResume() {
        mHandler.postDelayed(mRunnable, 0);
        super.onResume();
    }

    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        mVibrator.cancel();
        super.onPause();
    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            mHandler.removeCallbacks(mRunnable);
            mVibrator.vibrate(pattern, 0);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean isPass = true;

        for (PadTestKey testKey : mTestKeys) {
            if (testKey.getKeyCode() == keyCode) {
                testKey.setPass(true);
                mHandler.sendEmptyMessage(1);
            }
            if (!testKey.isPass()) {
                isPass = false;
            }
        }

        if (isPass) {
            mFailButton.setClickable(false);
            mHandler.sendEmptyMessageDelayed(2, 1000);
        }
//
//            case KeyEvent.KEYCODE_FOCUS:
//                keyText = (TextView) findViewById(R.id.focus);
//                break;
//
//            case KeyEvent.KEYCODE_CAMERA:
//                keyText = (TextView) findViewById(R.id.camera);
//                break;
        return true;
    }
}
