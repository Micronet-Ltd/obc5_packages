package com.qrt.factory.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.qrt.factory.R;
import com.qrt.factory.serial.Rs232Tester;

/**
 * Created by aitam on 26/06/2017.
 */

public class Rs232 extends AbstractActivity {

    static {
        System.loadLibrary("Serial_port");
    }
    private final Handler mCountdownHandler = new Handler();
    private static int COUNTDOWN=30;
    private static int STAGE =0;
    private static final String TAG = Rs232.class.getSimpleName();
    private final int BYTES_TO_READ = 128;
    private final byte[] data=new byte[]{
            48,49,50,51,52,53,54,55,56,57,
            48,49,50,51,52,53,54,55,56,57,
            48,49,50,51,52,53,54,55,56,57,
            48,49,32,33,34,35,36,37,58,39,
            40,41,42,43,44,45,46,47,48,49,
            50,51,52,53,54,55,56,57,58,59,
            60,61,62,63,64,65,66,67,68,69,
            70,71,72,73,74,75,76,77,78,79,
            80,81,82,83,84,85,86,87,88,89,
            90,91,92,93,94,95,96,97,98,99,
            100,101,102,103,104,105,106,107,108,109,
            110,111,112,113,114,115,116,117,118,119,
            120,121,122,123,124,125,126,127};
    Rs232Tester mRs232Tester;
    Rs232Tester mRs232TesterOther;
    boolean mFailOnce = false;
    TextView mRs232Msg;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (COUNTDOWN >0)  {
                mRs232Msg.setText(String.format(getString(R.string.rs232_connect_countdown),COUNTDOWN));
                COUNTDOWN--;
                if (readOnlyTest()){
                    resetCounter();
                    pass();
                } else {
                    mCountdownHandler.postDelayed(this,700);
                }
            } else {
                mRs232Msg.setText("Time Left: 0");
                COUNTDOWN =30;
                mCountdownHandler.removeCallbacks(this);
                if (readOnlyTest()){
                    resetCounter();
                    pass();
                } else {
                    mRs232Msg.setText(getString(R.string.rs232_connect));
                }
            }
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rs232);
    }

    @Override
    protected void onStart(){
        super.onStart();
        findViewById(R.id.rs232_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTest();
            }
        });
        findViewById(R.id.rs232_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCounter();
                fail();
            }
        });
        mRs232Msg=(TextView)findViewById(R.id.rs232_msg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        doTest();
    }

    private void resetCounter(){
        mCountdownHandler.removeCallbacksAndMessages(null);
        STAGE=0;
        COUNTDOWN=30;
    }


    private void doTest(){
        switch (STAGE){
            case 0:
		mRs232Msg.setText(getString(R.string.rs232_please_wait));
                if (readOnlyTest()){
                    STAGE++;
                    mRs232Msg.setText(getString(R.string.rs232_disconnect));
                } else {
                    STAGE +=2;
                    doTest();
                }
                break;
            case 1:
                if (readOnlyTest()){
                    STAGE=0;
                    fail();
                } else {
                    STAGE++;
                    doTest();
                }
                break;
            case 2:
                STAGE++;
                doCountdown();
                break;
            case 3:
                if (readOnlyTest()){
                    resetCounter();
                    pass();
                } else {
                    resetCounter();
                    fail();
                }
        }
    }

    private void doCountdown(){
        mCountdownHandler.postDelayed(runnable, 500l);
    }

    protected boolean readOnlyTest() {
        if (mRs232Tester == null) {
            mRs232Tester = new Rs232Tester("/dev/ttyHSL1",9600,300);
        }

        if (mRs232TesterOther==null){
            mRs232TesterOther = new Rs232Tester("/dev/ttyHSL1",9600,300);
        }

        byte[] buffer = new byte[data.length];
        Runnable run = new Runnable() {
            @Override
            public void run() {
                mRs232Tester.writeToSerial(data,data.length);
            }
        };
        run.run();
        Log.println(Log.ASSERT,"Out Data: ",new String(data));
        mRs232TesterOther.readMaxSize(buffer,buffer.length);
        Log.println(Log.ASSERT,"In  Data: ",new String(buffer));
        mRs232Tester.closeSerialPort();
        mRs232TesterOther.closeSerialPort();
        mRs232Tester=null;
        mRs232TesterOther=null;

        for (int i = 0; i < BYTES_TO_READ; i++) {
            if (buffer[i]!=data[i]) return false;
        }
        return true;
    }

    @Override
    protected String getTag() {
        return TAG;
    }


}
