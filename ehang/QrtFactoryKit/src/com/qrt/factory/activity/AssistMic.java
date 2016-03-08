package com.qrt.factory.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;
import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;
import android.app.Dialog;
import android.app.ProgressDialog;
import java.util.ArrayList;
import java.util.List;

public class AssistMic extends AbstractActivity {

    private static final String TAG = "AssistMicTest";

    private Button mPassButton;

    private Button mFailButton;

    private TextView mInsertHeadsetView;

    private Button mRetestButton;
    
    private Boolean mRetest = null;
    private Boolean mBoolean = null;   //add by baiwuqiang for SW00049591 anr 2014040513
    private static final List<String> OPEN_CMD_LIST = initOpenCmdList();
    private static final List<String> CLOSE_CMD_LIST = initCloseCmdList();

    private static List<String> initOpenCmdList(){
        List<String> openCmdList = new ArrayList<String>();
/*Add By baiwuqiang to change cmd at android 4.4 HQ00000000 2014-6-24 begin*/
        openCmdList.add("tinymix 'LOOPBACK Mode' 'ENABLE'");
		openCmdList.add("tinymix 'MICBIAS CAPLESS Switch' '1'");
        openCmdList.add("tinymix 'DEC1 MUX' 'ADC2'");
        openCmdList.add("tinymix 'ADC2 MUX' 'INP3'");
        openCmdList.add("tinymix 'DEC1 Volume' '84'");
        openCmdList.add("tinymix 'ADC2 Volume' '6'");
        openCmdList.add("tinymix 'IIR1 INP1 MUX' 'DEC1'");
        openCmdList.add("tinymix 'IIR1 INP1 Volume' '84'");
        openCmdList.add("tinymix 'RX1 MIX1 INP1' 'IIR1'");
        openCmdList.add("tinymix 'RX2 MIX1 INP1' 'IIR1'");
        openCmdList.add("tinymix 'RDAC2 MUX' 'RX2'");
        openCmdList.add("tinymix 'HPHL' 'Switch'");
        openCmdList.add("tinymix 'HPHR' 'Switch'");
        openCmdList.add("tinymix 'RX1 Digital Volume' '77'");
        openCmdList.add("tinymix 'RX2 Digital Volume' '77'");
        openCmdList.add("tinymix 'Loopback MCLK' 'ENABLE'");
		
/*	    openCmdList.add("tinymix 'DEC1 MUX' 'ADC3'");
        openCmdList.add("tinymix 'DEC1 Volume' '70%'");
        openCmdList.add("tinymix 'ADC3 Volume' '70%'");
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
        openCmdList.add("tinymix 'RX2 Digital Volume' '68%'");*/
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
        cmdList.add("tinymix 'ADC3 Volume' '0%'");
        cmdList.add("tinymix 'HPHL DAC Switch' 0");
        cmdList.add("tinymix 'RX1 Digital Volume' '0%'");
        cmdList.add("tinymix 'RX2 Digital Volume' '0%'");*/
        //Del By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-10-22 End
        //Add By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-10-22 Begin
/*        closeCmdList.add("tinymix 'DEC1 MUX' '0'");
        closeCmdList.add("tinymix 'DEC1 Volume' '68%'");
        closeCmdList.add("tinymix 'ADC3 Volume' '68%'");
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
        closeCmdList.add("tinymix 'RX2 Digital Volume' '68%'");*/;
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
        messageTextView.setText(getString(R.string.audio_message_aux));
        mRetestButton = (Button) findViewById(R.id.audio_retest);
        mRetestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	//start add by baiwuqiang for SW00049591 anr 2014040513
              //  retest();
                mBoolean = false;
                mRetest = true;
                new Thread(mCloseRunnable).start();
              //end add by baiwuqiang for SW00049591 anr 2014040513
            }
        });
        mPassButton = (Button) findViewById(R.id.audio_pass);
        mPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	//start add by baiwuqiang for SW00049591 anr 2014040513
             //   pass();
            	mRetest = false;
                mBoolean = true;
                new Thread(mCloseRunnable).start();
              //end add by baiwuqiang for SW00049591 anr 2014040513
            }
        });
        mFailButton = (Button) findViewById(R.id.audio_fail);
        mFailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	//start add by baiwuqiang for SW00049591 anr 2014040513
           //     fail();
                mBoolean = false;
                mRetest = false;
                new Thread(mCloseRunnable).start();
                //end add by baiwuqiang for SW00049591 anr 2014040513
            }
        });
        mPassButton.setClickable(false);
      //add by baiwuqiang for SW00049591 anr 2014040513
        mBoolean = null;
        mRetest = null;
        
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
                  //start add by baiwuqiang for SW00049591 anr 2014040513
                   // Utilities.exec(OPEN_CMD_LIST);
                    new Thread(mOpenRunnable).start();
                  //end add by baiwuqiang for SW00049591 anr 2014040513
                } else { // 拔出耳机
                    mInsertHeadsetView.setVisibility(View.VISIBLE);
                    mPassButton.setClickable(false);
                    //Add By Wangwenlong to avoid bug issue (8x26) HQ00000000 2013-11-28
                    //start add by baiwuqiang for SW00049591 anr 2014040513
                 //   Utilities.exec(CLOSE_CMD_LIST);
                    new Thread(mCloseRunnable).start();  //add by bwq for close mic test cmd 201400905
                    //end add by baiwuqiang for SW00049591 anr 2014040513
                }
            }
        }
    };

    @Override
    public void finish() {
       // Utilities.exec(CLOSE_CMD_LIST);   //add by baiwuqiang for SW00049591 anr 2014040513
        super.finish();
    }
    
    private Handler mHandler = new Handler() {
       @Override
       public void handleMessage(Message msg) {
            if (msg.what == 0) {
                showDialog(1);
                return;
            } else if (msg.what == 1) {
                removeDialog(1);
                if (mBoolean != null) {
                    if (mBoolean) {
                        pass();
                    } else {
                    	if(mRetest)
                    	{
                    		retest();
                    	}else{
                            fail();
                    	}

                    }
                }
                return;
            }
            super.handleMessage(msg);
        }
    };
    private Runnable mCloseRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(0);
            Utilities.exec(CLOSE_CMD_LIST);
            mHandler.sendEmptyMessage(1);
        }
    };
    private Runnable mOpenRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.sendEmptyMessage(0);
            Utilities.exec(OPEN_CMD_LIST);
            mHandler.sendEmptyMessage(1);
        }
    };
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            ProgressDialog dialog = new ProgressDialog(AssistMic.this);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
        return super.onCreateDialog(id);
    }
}