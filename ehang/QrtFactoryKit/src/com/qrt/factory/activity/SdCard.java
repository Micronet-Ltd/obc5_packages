/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SdCard extends AbstractActivity {

    private static final String TAG = "SDCard Test";

    private String mSdCardPath;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdcard);

        TextView textView = (TextView) findViewById(R.id.sdcard_hint);
        textView.setText(getString(R.string.sdcard_wait));

        mSdCardPath = getString(R.string.default_sd_path, "/storage/sdcard1");

        try {
            exec("mount");
        } catch (Exception e){
            fail();
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void dispatchMessage(android.os.Message msg) {
            boolean res = (Boolean) msg.obj;
            if (res) {
                pass();
            } else {
                fail();
            }
        }
    };

    void exec(final String para) {

        new Thread() {

            public void run() {
                try {
                    logd(para);

                    Process mProcess;
                    String paras[] = para.split(",");
                    for (int i = 0; i < paras.length; i++)
                        logd(i + ":" + paras[i]);
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
                    logd(data);
                    int result = mProcess.exitValue();
                    logd("ExitValue=" + result);
                    Message message = new Message();
                    message.obj = data.contains(mSdCardPath);
                    message.setTarget(mHandler);
                    message.sendToTarget();

                } catch (Exception e) {
                    logd(e);
                    Message message = new Message();
                    message.obj = false;
                    message.setTarget(mHandler);
                    message.sendToTarget();
                }
            }
        }.start();
    }
}
