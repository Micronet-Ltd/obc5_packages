package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: wangwenlong Date: 12-1-10 Time: 上午11:26 To
 * change this template use File | Settings | File Templates.
 */
public class PanelKeyLight extends AbstractActivity {

    private static final String TAG = "PanelKeyLight Test";

    private static final String BRIGHTNESS
            = "/sys/class/leds/button-backlight/brightness";

    private static final byte onValue = '1';

    private static final byte offValue = '0';

    private byte value = '0';

    private Handler mHandler = new Handler();

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.panelkey_led);

        mHandler.post(mRunnable);
        if (!isFinishing()) {
            showPassOrFailDialog(PanelKeyLight.this, getString(R.string.keyLight_confirm),
                    getString(R.string.ok), passListener,
                    getString(R.string.fail), failListener);
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            value = value == onValue ? offValue : onValue;

            changeFileValue(value);

            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    private void changeFileValue(byte value) {

        FileOutputStream file = null;

        try {
            file = new FileOutputStream(BRIGHTNESS);
            file.write(value);
            file.close();
            file = null;
        } catch (FileNotFoundException e) {
            loge(e);
        } catch (IOException e) {
            loge(e);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e1) {
                    file = null;
                }
            }
        }

    }

    @Override
    public void finish() {
        changeFileValue((byte) '0');
        super.finish();
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        super.onPause();
    }

    DialogInterface.OnClickListener passListener
            = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            pass();
        }
    };

    DialogInterface.OnClickListener failListener
            = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            fail();
        }
    };
}