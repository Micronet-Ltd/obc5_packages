/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;

import java.util.ArrayList;
import java.util.List;

public class Headset extends AbstractActivity {

    private static final String TAG = "Headset Test";

    private Button mPassButton;

    private Button mFailButton;

    private TextView mInsertHeadsetView = null;

    private Button mRetestButton;

    private static final List<String> OPEN_CMD_LIST = initOpenCmdList();
    private static final List<String> CLOSE_CMD_LIST = initCloseCmdList();

    private static List<String> initOpenCmdList(){
        List<String> openCmdList = new ArrayList<String>();
		/*Add By baiwuqiang to change cmd at android 4.4 HQ00000000 2014-6-24 begin*/
		openCmdList.add("tinymix 'LOOPBACK Mode' 'ENABLE'");
        openCmdList.add("tinymix 'MICBIAS CAPLESS Switch' '1'");
        openCmdList.add("tinymix 'DEC1 MUX' 'ADC2'");
        openCmdList.add("tinymix 'ADC2 MUX' 'INP2'");
        openCmdList.add("tinymix 'DEC1 Volume' '84'");
        openCmdList.add("tinymix 'ADC2 Volume' '6'");
        openCmdList.add("tinymix 'IIR1 INP1 MUX' 'DEC1'");
        openCmdList.add("tinymix 'IIR1 INP1 Volume' '100'");
        openCmdList.add("tinymix 'RX1 MIX1 INP1' 'IIR1'");
        openCmdList.add("tinymix 'RX2 MIX1 INP1' 'IIR1'");
        openCmdList.add("tinymix 'RDAC2 MUX' 'RX2'");
        openCmdList.add("tinymix 'HPHL' 'Switch'");
        openCmdList.add("tinymix 'HPHR' 'Switch'");
        openCmdList.add("tinymix 'RX1 Digital Volume' '84'");
        openCmdList.add("tinymix 'RX2 Digital Volume' '84'");
        openCmdList.add("tinymix 'Loopback MCLK' 'ENABLE'");
/*		
        openCmdList.add("tinymix 'DEC1 MUX' 'ADC2'");
        openCmdList.add("tinymix 'DEC1 Volume' '60%'");
        openCmdList.add("tinymix 'ADC2 Volume' '60%'");
        openCmdList.add("tinymix 'IIR1 INP1 MUX' 'DEC1'");
        openCmdList.add("tinymix 'RX1 MIX1 INP1' 'IIR1'");
        openCmdList.add("tinymix 'RX2 MIX1 INP1' 'IIR1'");
        openCmdList.add("tinymix 'HPHL Volume' '100%'");
        openCmdList.add("tinymix 'HPHR Volume' '100%'");
        //Add By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-10-25
        openCmdList.add("tinymix 'IIR1 INP1 Volume' '68%'");
        openCmdList.add("tinymix 'HPHL DAC Switch' 1");
        openCmdList.add("tinymix 'CLASS_H_DSM MUX' 'RX_HPHL'");
        openCmdList.add("tinymix 'RX1 Digital Volume' '68%'");
        openCmdList.add("tinymix 'RX2 Digital Volume' '68%'");
		openCmdList.add("tinymix 'Loopback MCLK' 'ENABLE'");*/
		/*Add By baiwuqiang to change cmd at android 4.4 HQ00000000 2014-6-24 end*/
        return openCmdList;
    }

    private static List<String> initCloseCmdList(){
        List<String> closeCmdList = new ArrayList<String>();
		/*Add By baiwuqiang to change cmd at android 4.4 HQ00000000 2014-6-24 begin*/
		closeCmdList.add("tinymix 'MICBIAS CAPLESS Switch' '0'");
        closeCmdList.add("tinymix 'DEC1 MUX' 'ZERO'");
        closeCmdList.add("tinymix 'ADC2 MUX' 'ZERO'");
        closeCmdList.add("tinymix 'DEC1 Volume' '84'");
        closeCmdList.add("tinymix 'ADC2 Volume' '6'");
        closeCmdList.add("tinymix 'IIR1 INP1 MUX' 'ZERO'");
        closeCmdList.add("tinymix 'IIR1 INP1 Volume' '84'");
        closeCmdList.add("tinymix 'RX1 MIX1 INP1' 'ZERO'");
        closeCmdList.add("tinymix 'RX2 MIX1 INP1' 'ZERO'");
        closeCmdList.add("tinymix 'RDAC2 MUX' 'ZERO'");
        closeCmdList.add("tinymix 'HPHL' 'ZERO'");
        closeCmdList.add("tinymix 'HPHR' 'ZERO'");
        closeCmdList.add("tinymix 'RX1 Digital Volume' '84'");
        closeCmdList.add("tinymix 'RX2 Digital Volume' '84'");
        closeCmdList.add("tinymix 'Loopback MCLK' 'DISABLE'");
		closeCmdList.add("tinymix 'LOOPBACK Mode' 'DISABLE'");
        //Del By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-10-22 Begin
        /*cmdList.add("tinymix 'DEC1 Volume' '0%'");
        cmdList.add("tinymix 'ADC2 Volume' '0%'");
        cmdList.add("tinymix 'HPHL DAC Switch' 0");
        cmdList.add("tinymix 'RX1 Digital Volume' '0%'");
        cmdList.add("tinymix 'RX2 Digital Volume' '0%'");*/
        //Del By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-10-22 End
        //Add By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-10-22 Begin
 /*       closeCmdList.add("tinymix 'DEC1 MUX' '0'");
        closeCmdList.add("tinymix 'DEC1 Volume' '68%'");
        closeCmdList.add("tinymix 'ADC2 Volume' '68%'");
        closeCmdList.add("tinymix 'IIR1 INP1 MUX' '0'");
        closeCmdList.add("tinymix 'RX1 MIX1 INP1' '0'");
        closeCmdList.add("tinymix 'RX2 MIX1 INP1' '0'");
        closeCmdList.add("tinymix 'HPHL Volume' '68%'");
        closeCmdList.add("tinymix 'HPHR Volume' '68%'");
        //Add By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-10-25
        closeCmdList.add("tinymix 'IIR1 INP1 Volume' '68%'");
        closeCmdList.add("tinymix 'HPHL DAC Switch' 0");
        closeCmdList.add("tinymix 'CLASS_H_DSM MUX' '0'");
        closeCmdList.add("tinymix 'RX1 Digital Volume' '68%'");
        closeCmdList.add("tinymix 'RX2 Digital Volume' '68%'");*/
        //Add By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-10-22 End
		/*Add By baiwuqiang to change cmd at android 4.4 HQ00000000 2014-6-24 end*/
        return closeCmdList;
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mic_receiver);
        TextView messageTextView = (TextView) findViewById(R.id.audio_message);
        messageTextView.setText(getString(R.string.audio_message_headset));
        mRetestButton = (Button) findViewById(R.id.audio_retest);
        mRetestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retest();
            }
        });
        mPassButton = (Button) findViewById(R.id.audio_pass);
        mPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pass();
            }
        });
        mFailButton = (Button) findViewById(R.id.audio_fail);
        mFailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fail();
            }
        });
        mPassButton.setClickable(false);

        mInsertHeadsetView = (TextView) findViewById(
                R.id.mic_receiver_headset);
        mInsertHeadsetView.setText(R.string.insert_headset);

        //Del By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-11-28
        //Utilities.exec(OPEN_CMD_LIST);
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
                    mInsertHeadsetView.setVisibility(View.INVISIBLE);
                    mPassButton.setClickable(true);
                    //Add By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-11-28
                    Utilities.exec(OPEN_CMD_LIST);
                } else { // 拔出耳机
                    mInsertHeadsetView.setVisibility(View.VISIBLE);
                    mPassButton.setClickable(false);
                    //Add By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-11-28
                    Utilities.exec(CLOSE_CMD_LIST);
                }
            }
        }
    };

    @Override
    public void finish() {
        Utilities.exec(CLOSE_CMD_LIST);
        super.finish();
    }
}
