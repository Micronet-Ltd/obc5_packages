/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.TestSettings;
import com.qrt.factory.util.FmManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class FmRadio extends AbstractActivity {

    private static final String TAG = "FM Test";

    private Button searchButton, passButton, failButton;

    private TextView mTextView;

    private AudioManager mAudioManager = null;

    private FmManager mFmManager = null;

    private int mDefaultMode;

    private ProgressDialog progressDialog = null;

    private ProgressDialog loadingProgressDialog = null;

    private Map<Integer, Integer> mDefaultAudioVolume
            = new HashMap<Integer, Integer>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                progressDialog = ProgressDialog
                        .show(FmRadio.this, "", getString(R.string.fm_search_on),
                                true);

            } else if (msg.what == 1) {
                searchButton.setEnabled(true);
                cancelDialog(FmRadio.this.progressDialog);
                progressDialog = null;
                mTextView.setText(msg.getData().getString("freq"));
            } else if (msg.what == 2) {
                loadingProgressDialog = ProgressDialog
                        .show(FmRadio.this, "", getString(R.string.fm_open_on),
                                true);
                //Add By Wangwenlong to no headset disable (8x26) HQ00000000 2013-10-28
                passButton.setEnabled(true);
            } else if (msg.what == 3) {
                searchButton.setEnabled(true);
                cancelDialog(FmRadio.this.loadingProgressDialog);
                loadingProgressDialog = null;
                if (mFmManager.isFmOn()) {
                    for (int i = 0; i < 3; i++) {
                        mFmManager.setFreq(TestSettings.DEFAULT_FREQ);
                    }
                }
            } else if (msg.what == 4) {
                if (!isFinishing()) {
                    searchButton.setEnabled(true);
                    cancelDialog(FmRadio.this.loadingProgressDialog);
                    loadingProgressDialog = null;
                    showToast(getString(R.string.fm_open_error));
                    fail();
                }
            } else if (msg.what == 5) {
                mTextView.setText(
                        new Float(mFmManager.getFreq() / 1000f).toString()
                                + "MHZ");
                searchButton.setEnabled(true);
                cancelDialog(FmRadio.this.progressDialog);
                progressDialog = null;
            }
        }
    };

    private TextView mFmInsertHeadsetView = null;

    private void cancelDialog(ProgressDialog dialog) {
        if (!isFinishing() && dialog != null && dialog.isShowing()) {
            dialog.cancel();
        }
    }


    private Runnable progressRunnable = new Runnable() {

        @Override
        public void run() {
            int frequency = TestSettings.DEFAULT_FREQ;
            frequency = getFreq();
            String freq = Float.toString(frequency / 1000f) + "MHZ";
            Message message = new Message();
//            message.what = 1;
//            Bundle bundle = new Bundle();
//            bundle.putString("freq", freq);
//            message.setData(bundle);
//            mHandler.sendMessage(message);
        }
    };

    private int getFreq() {
//        if (mFmManager.searchUP()) {
//            int frequency = mFmManager.getFreq();
//            mFmManager.setFreq(frequency);
//            return frequency;
//        } else {
//            return getFreq();
//        }

        mFmManager.searchUP();
        int frequency = mFmManager.getFreq();
//        mFmManager.setFreq(frequency);
        return frequency;

    }

    void getService() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mFmManager = new FmManager(this, mHandler);
    }

    void bindView() {

        searchButton = (Button) findViewById(R.id.fm_search);
        searchButton.setEnabled(false);
        passButton = (Button) findViewById(R.id.fm_pass);
        //Add By Wangwenlong to no headset disable (8x26) HQ00000000 2013-10-28
        passButton.setEnabled(false);
        failButton = (Button) findViewById(R.id.fm_fail);
        mTextView = (TextView) findViewById(R.id.fm_frequency);

        searchButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                if (mAudioManager.isWiredHeadsetOn()) {
                    searchButton.setEnabled(false);
                    mHandler.sendEmptyMessage(0);
                    new Thread(progressRunnable).start();
                }
            }
        });

        passButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                pass();
            }
        });

        failButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                fail();
            }
        });

    }

    @Override
    public void finish() {
        mFmManager.fmOff();
        setDefaultAudioVolume();
        super.finish();
    }

    private void setDefaultAudioVolume() {
        mAudioManager.setMode(mDefaultMode);
        for (Map.Entry<Integer, Integer> entry : mDefaultAudioVolume
                .entrySet()) {
            mAudioManager.setStreamVolume(entry.getKey(), entry.getValue(), 0);
        }
    }

    private void getDefaultAudioVolume() {

        mDefaultMode = mAudioManager.getMode();

        mDefaultAudioVolume.clear();
        mDefaultAudioVolume.put(AudioManager.STREAM_ALARM, mAudioManager
                .getStreamVolume(AudioManager.STREAM_ALARM));
        mDefaultAudioVolume.put(AudioManager.STREAM_MUSIC, mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));
        mDefaultAudioVolume.put(AudioManager.STREAM_VOICE_CALL, mAudioManager
                .getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        mDefaultAudioVolume.put(AudioManager.STREAM_DTMF, mAudioManager
                .getStreamVolume(AudioManager.STREAM_DTMF));
        mDefaultAudioVolume.put(AudioManager.STREAM_NOTIFICATION, mAudioManager
                .getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        mDefaultAudioVolume.put(AudioManager.STREAM_RING, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_RING));
        mDefaultAudioVolume.put(AudioManager.STREAM_SYSTEM, mAudioManager
                .getStreamVolume(AudioManager.STREAM_SYSTEM));
    }

    public void setAudio() {
/*del by baiwuqiang for after fmtest receiver no voice SW00065030 20140716 begin*/
//        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
//        mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        float ratio = 0.8f;

        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)), 0);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.fm);
        getService();
        getDefaultAudioVolume();
        setAudio();
        bindView();

        mFmInsertHeadsetView = (TextView) findViewById(
                R.id.fm_insert_headset);
        if (!mAudioManager.isWiredHeadsetOn()) {
            mFmInsertHeadsetView.setVisibility(View.VISIBLE);
        } else {
            mFmInsertHeadsetView.setVisibility(View.GONE);
            //Add By Wangwenlong to no headset disable (8x26) HQ00000000 2013-10-28
            passButton.setEnabled(true);
        }

//        mHandler.sendEmptyMessage(2);
//
//        new Thread(openDeviceRunnable).start();
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

                Log.i(TAG, "state: " + state);
                Log.i(TAG, "fmOn: " + mFmManager.isFmOn());

                if (state == 1) { // 插入耳机
                    mFmInsertHeadsetView.setVisibility(View.GONE);
                    if (!mFmManager.isFmOn()) {
                        mHandler.sendEmptyMessage(2);
                        new Thread(openDeviceRunnable).start();
                    }
                } else { // 拔出耳机
                    mFmInsertHeadsetView.setVisibility(View.VISIBLE);
                    if (mFmManager.isFmOn()) {
                        mFmManager.fmOff();
                    }
                }
            }
        }
    };

    private Runnable openDeviceRunnable = new Runnable() {
        @Override
        public void run() {
            int i = 0;
            while (!mFmManager.isFmOn()) {
                mFmManager.fmOn();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                i++;
                if (i > 10) {
                    mHandler.sendEmptyMessage(4);
                    break;
                }
            }
            if (i <= 10) {
                mHandler.sendEmptyMessage(3);
            }
        }
    };
}
