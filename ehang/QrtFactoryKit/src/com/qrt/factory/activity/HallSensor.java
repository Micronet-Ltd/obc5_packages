/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

public class HallSensor extends AbstractActivity {

    private static final String TAG = "HallSensor Test";
    private static final String HALL_SENSOR_FILE_PATH
            = "/sys/class/input";
    private static final String defaul_HALL_SENSOR_FILE_PATH
    = "";
    private static  String HALL_SENSOR_FILE
    = null;
    private boolean pass = false;
    private String hallSensorInfo ;
    private boolean hallOK = false;
    private int testTimeCount = 0;
    private TextView mTextView;
    private TextView mTestResultView;

    @Override
    public void finish() {

        try {

        } catch (Exception e) {
            loge(e);
        }
        super.finish();
    }

    void bindView() {

        mTextView = (TextView) findViewById(R.id.hallsensor_result);
        mTestResultView = (TextView) findViewById(R.id.hallsensor_test_result);

        Button cancel = (Button) findViewById(R.id.hallsensor_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });
    }

    void getService() {
    	File f = new File(HALL_SENSOR_FILE_PATH);
    	HALL_SENSOR_FILE = FindFile(f, "hall_gpio_value");

        Log.d("hallsensor","get Filename == "+ HALL_SENSOR_FILE);
        hallSensorInfo =  Utilities
                        .getFileInfo(HALL_SENSOR_FILE);
        Log.d("hallsensor","hall_value == "+ hallSensorInfo);
        
        mTextView.setText(R.string.hall_sensor_check);
        mTestResultView.setText(
                 "");
      //  getString(R.string.hall_sensor_verison) 
    }


    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.hallsensor);

        bindView();
        getService();
     //   mHandler.sendEmptyMessage(0);
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }


    @Override
    protected void onDestroy() {
    	mHandler.removeMessages(0);
        super.onDestroy();
    }
    
    private void checkHall() {	  	
            if (hallOK) {
      //          pass();
            	mHandler.sendEmptyMessageDelayed(3, 600);
            } else {
                if (testTimeCount < 60) {
                    mHandler.sendEmptyMessageDelayed(1, 500);
                } else {
                    fail();
                }
            }
            testTimeCount++;          
    }
 /*******add by baiwuqiang to get hall file 20140418 start*************/   
    private static String FindFile(File file, String key_search)
    {
    	if((file==null)|| (file.getName()=="")){
    		Log.d("HallActivity","paramter invalid");
    		return null;
    	}

   	    if(file.getAbsolutePath().length()>45 ){
   	    	return null;
   	    }
        if (file.isDirectory()) {
        	Log.d("HallActivity", "file.isDirectory()== "+file.getName());
            File[] all_file = file.listFiles();
            //Log.d("HallActivity", "all_file count: "+all_file.size);
            if (all_file != null) {
                for (File tempf : all_file) {
                    if (tempf.isDirectory()) {
                    	 Log.d("HallActivity","check file dir: == "+tempf.getName());
                    	 String result ="";
                    	 if(tempf.getAbsolutePath().startsWith("/sys/class/input/input")){
                    		 result = FindFile(tempf,key_search);
	                    	 if(result!=null&&!result.equals(""))
	                    		 return result;
                    	 }
                    }
                    else 
                    {
                    	Log.d("HallActivity","check file:"+tempf.getAbsolutePath());
	                    if(tempf.getName().equals(key_search))
	                    {
	                    	Log.d("HallActivity","find path:"+tempf.getAbsolutePath());
	                    	return tempf.getAbsolutePath();
	                    }
                    }
                }
            }
        
        }else if(file.getName().equals(key_search))
        {
        	Log.d("HallActivity", " check file second:"+file.getAbsolutePath());
        	return file.getAbsolutePath();
        }
        return null;
    }
    /*******add by baiwuqiang to get hall file 20140418 end*************/ 
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (!isFinishing()) {
                	File f = new File(HALL_SENSOR_FILE_PATH);
                	HALL_SENSOR_FILE = FindFile(f, "hall_gpio_value");
                    hallSensorInfo =  Utilities
                                    .getFileInfo(HALL_SENSOR_FILE);
          //          hallSensorInfo =  Utilities
          //                  .getFileInfo(HALL_SENSOR_FILE_PATH);
                    if ("1".equals(hallSensorInfo) ) {
                        mHandler.sendEmptyMessageDelayed(1, 500);
                    }else{                  
               //     	pass();
                    	mHandler.sendEmptyMessageDelayed(1, 500);
                    }
                }
                
            } else if (msg.what == 1) {
            	File f = new File(HALL_SENSOR_FILE_PATH);
            	HALL_SENSOR_FILE = FindFile(f, "hall_gpio_value");
                hallSensorInfo =  Utilities
                                .getFileInfo(HALL_SENSOR_FILE);
             //   hallSensorInfo =  Utilities
              //          .getFileInfo(HALL_SENSOR_FILE_PATH);
                Log.d("hallsensor","hall_value == "+ hallSensorInfo);
                if("1".equals(hallSensorInfo)){
                	hallOK = false;
                    mTextView.setText(R.string.hall_sensor_check);
                    mTestResultView.setText("");
 //                   mTestResultView.setText(R.string.nv_fail);
                }else if("0".equals(hallSensorInfo)){
                	hallOK = true;
                    mTextView.setText(R.string.hall_sensor_check_ok);
                    mTestResultView.setText(R.string.nv_pass);
                    mTextView.setTextColor(android.graphics.Color.GREEN);
                    mTestResultView.setTextColor(android.graphics.Color.GREEN);
                }
            	checkHall();
            }
            else if (msg.what == 3){
            	pass();
            }
        }
    };
}
