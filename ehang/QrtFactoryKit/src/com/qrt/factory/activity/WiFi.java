/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import java.util.List;

public class WiFi extends AbstractActivity {

    private static final String TAG = "WiFi Test";

    private WifiLock mWifiLock;

    private WifiManager mWifiManager;

    private List<ScanResult> wifiScanResult;

    private TextView mTextView;

    private final int SCAN_INTERVAL = 3000;

    private final int OUT_TIME = 30000;

    private IntentFilter mFilter = new IntentFilter();

    private boolean isOver = false;

    private String wifiInfos = "";

    private boolean stop = false;

    @Override
    public void finish() {
//        // User may press back key while showing the AP list.
//        if (wifiScanResult != null && wifiScanResult.size() > 0)
//        {
//            mResultBuffer.append(wifiInfos);
//            pass();
//        }

        stop = true;
//        enableWifi(false);
        try {
//            mCountDownTimer.cancel();
            if (true == mWifiLock.isHeld()) {
                mWifiLock.release();
            }
        } catch (Exception e) {
            loge(e);
        }
        super.finish();
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi);
        isOver = false;

        bindView();
        getService();

        /** Keep Wi-Fi awake */
        mWifiLock = mWifiManager
                .createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "WiFi");
        if (false == mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }

        mOpenThread.start();

        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }

    @Override
    protected void onResume() {
        registerReceiver(mReceiver, mFilter);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    private void enableWifi(boolean enable) {
        if (mWifiManager != null) {
            mWifiManager.setWifiEnabled(enable);
        }
    }

    private void bindView() {
        mTextView = (TextView) findViewById(R.id.wifi_hint);
        mTextView.setText(getString(R.string.wifi_text));
    }

    private void getService() {
        mWifiManager = (WifiManager) this
                .getSystemService(Context.WIFI_SERVICE);
    }

    private Thread mScanThread = new Thread(new Runnable() {
        @Override
        public void run() {
            logd("WiFi Scan Runnable Start");
            // At least conduct startScan() 3 times to ensure wifi's scan
            int i = 0;
            while (i <= 10 && !isOver) {
                if (mWifiManager.getWifiState()
                        == WifiManager.WIFI_STATE_ENABLED) {
                    mWifiManager.startScan();
                }
                i++;
                try {
                    Thread.sleep(SCAN_INTERVAL);
                } catch (InterruptedException e) {

                }
            }

            if (!isOver) {
                mHandler.sendEmptyMessage(0);
            }
        }
    });

    private Thread mOpenThread = new Thread(new Runnable() {
        @Override
        public void run() {
            logd("WiFi Open Runnable Start");
            // At least conduct startScan() 3 times to ensure wifi's scan
            int i = 0;
            while (i <= 30) {

                if (stop == true) {

                    return;
                }

                logd("wifi state:" + mWifiManager.getWifiState());
                if (mWifiManager.getWifiState()
                        == WifiManager.WIFI_STATE_DISABLED) {
                    enableWifi(true);
                    //mHandler.sendEmptyMessage(1);
                    return;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }

                i++;
            }

            if (!isOver) {
                switch (mWifiManager.getWifiState()) {
                    case WifiManager.WIFI_STATE_DISABLING:
                        mResultBuffer
                                .append(getString(R.string.wifi_is_closing));
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        mResultBuffer.append(getString(
                                R.string.wifi_state_unknown1));
                        break;
                }
                mHandler.sendEmptyMessage(2);
            }

        }
    });

//    private CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME,
//            SCAN_INTERVAL) {
//
//        @Override
//        public void onFinish() {
//            logd("Timer Finish");
//            if (wifiScanResult == null || wifiScanResult.size() == 0) {
//                mResultBuffer.append(getString(R.string.wifi_scan_null));
//                fail();
//            }
//        }
//
//        @Override
//        public void onTick(long arg0) {
//            logd("Timer Tick");
//            // At least conduct startScan() 3 times to ensure wifi's scan
//            if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
//                mWifiManager.startScanActive();
//            }
//        }
//    };

    /* private CountDownTimer mDelayTimer = new CountDownTimer(2000, 1000) {
        @Override
        public void onFinish() {
//            mResultBuffer.append(wifiInfos);
            isOver = true;
            pass();
        }

        @Override
        public void onTick(long arg0) {

        }
    };*/

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0:
                    getScanResult();
                    break;
                case 1:
                    if (mScanThread != null && !mScanThread.isAlive()) {
                        mScanThread.start();
                    }
                    break;
                case 2:
                    isOver = true;
                    fail();
                    break;
            }
        }

        private void getScanResult() {
            String s = getString(R.string.wifi_text) + "\n\n" + "AP List:\n";
            wifiInfos = "";
            if (wifiScanResult != null && wifiScanResult.size() > 0) {

                for (int i = 0; i < wifiScanResult.size(); i++) {
                    logd(wifiScanResult.get(i));
                    s += " " + i + ": " + wifiScanResult.get(i).SSID + "\n\n";
                    wifiInfos += " " + i + ": " + wifiScanResult.get(i)
                            .toString() + "\n\n";
                    mTextView.setText(s);
                }

                isOver = true;
                pass();
            } else {
                mResultBuffer.append(getString(R.string.wifi_scan_null));
                isOver = true;
                fail();
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context c, Intent intent) {

            logd("recevie action:" + intent.getAction() + "  " + mWifiManager
                    .getWifiState());
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
                    .equals(intent.getAction())) {
                wifiScanResult = mWifiManager.getScanResults();
                mHandler.sendEmptyMessage(0);
            }
            if (WifiManager.WIFI_STATE_CHANGED_ACTION
                    .equals(intent.getAction())) {
                if (mWifiManager.getWifiState()
                        == WifiManager.WIFI_STATE_ENABLED) {
                    mHandler.sendEmptyMessageDelayed(1, 1000);
                }
            }
        }
    };
}