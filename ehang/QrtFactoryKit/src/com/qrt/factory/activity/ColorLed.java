/*
 * Copyright (c) 2011-2012, QUALCOMM Technologies Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import java.io.FileOutputStream;
import java.io.IOException;

import com.qrt.factory.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class ColorLed extends AbstractActivity {

    private static final String TAG = "TriColorLed";
    private final int RED = 0;
    private final int GREEN = 1;
    private final int BLUE = 2;
    private final int INIT_COLOR_NUM = 2;
    private int color = RED;
    final byte[] LIGHT_ON = { '2', '5', '5' };
    final byte[] LIGHT_OFF = { '0' };
    final String RED_LED_DEV = "/sys/class/leds/red/brightness";
    final String GREEN_LED_DEV = "/sys/class/leds/green/brightness";
    final String BLUE_LED_DEV = "/sys/class/leds/blue/brightness";
    
    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int colorIndex = color++;
            if (colorIndex >= INIT_COLOR_NUM){
                colorIndex = color = 0;
            }
            setColor(colorIndex);
            mHandler.postDelayed(this, 800);
        }
    };

    private Runnable mShowDialogRunnable = new Runnable() {
        @Override
        public void run() {

            if (!isFinishing()) {
                showPassOrFailDialog(ColorLed.this, getString(R.string.led_confirm),
                        getString(R.string.yes), passListener,
                        getString(R.string.no), failListener);
            }
        }
    };

    @Override
    public void finish() {

        super.finish();
    }

    private void init(Context context) {
        
        setResult(RESULT_CANCELED);
        setContentView(R.layout.tricolor_led);
        TextView textView = (TextView) findViewById(R.id.led_hint);
        if (INIT_COLOR_NUM == 3)
            textView.setText(R.string.led_tri_text);
        else if (INIT_COLOR_NUM == 2)
            textView.setText(R.string.led_dual_text);

        color = -1;
        mHandler.post(mRunnable);
        mHandler.postDelayed(mShowDialogRunnable, 1000);
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        init(getApplicationContext());

    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mRunnable);
        setColor(-1);
        super.onDestroy();
    }

    OnClickListener passListener = new OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {

            pass();
        }
    };

    OnClickListener failListener = new OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {

            fail();
        }
    };

    private void setColor(int color) {

        logd("set:" + color);
        boolean red = false, green = false, blue = false;
        switch (color) {
            case RED:
                red = true;
                break;
            case BLUE:
                blue = true;
                break;
            case GREEN:
                green = true;
                break;
            default:
                break;
        }
        FileOutputStream fRed = null;
        FileOutputStream fGreen = null;
        FileOutputStream fBlue = null;
        try {
            fRed = new FileOutputStream(RED_LED_DEV);
            fRed.write(red ? LIGHT_ON : LIGHT_OFF);
            fRed.close();
            fRed = null;
            fGreen = new FileOutputStream(GREEN_LED_DEV);
            fGreen.write(green ? LIGHT_ON : LIGHT_OFF);
            fGreen.close();
            fBlue = new FileOutputStream(BLUE_LED_DEV);
            fBlue.write(blue ? LIGHT_ON : LIGHT_OFF);
            fBlue.close();

        } catch (Exception e) {
            loge(e);
        } finally {
            if (fRed != null) {
                try {
                    fRed.close();
                } catch (IOException e) {
                    fRed = null;
                }
            }
            if (fGreen != null) {
                try {
                    fGreen.close();
                } catch (IOException e) {
                    fGreen = null;
                }
            }
            if (fBlue != null) {
                try {
                    fBlue.close();
                } catch (IOException e) {
                    fBlue = null;
                }
            }
        }


    }
}
