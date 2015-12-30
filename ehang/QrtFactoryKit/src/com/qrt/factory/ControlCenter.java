/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory;

//Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 Begin
//import com.qualcomm.qcnvitems.QcNvItemTypes;
//import com.qualcomm.qcnvitems.QcNvItems;
//Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 End
import com.qrt.factory.auto.AutoTestController;
import com.qrt.factory.domain.TestItem;
import com.qrt.factory.domain.TestStatusBase;
import com.qrt.factory.util.ResultActivity;
import com.qrt.factory.util.Utilities;
import com.qrt.factory.util.XmlUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import static com.qrt.factory.TestSettings.BLUETOOTH_SCAN_TO_SUCESS;

public class ControlCenter extends ListActivity {

    private static final String TAG = "ControlCenter";

    private boolean mExitFlag = false;
    private boolean mDefaultRotation = false;
    private LayoutInflater mInflater;

    private Context mContext;

    private static final int MENU_CLEAN_STATE = Menu.FIRST;

    private static final int MENU_UNINSTALL = MENU_CLEAN_STATE + 1;

    private static final int MENU_MASTER_CLEAR = MENU_UNINSTALL + 1;

    private static final int MENU_TEST_INFO = MENU_MASTER_CLEAR + 1;

    private static final int MENU_SET_NV = MENU_TEST_INFO + 1;

    private static final String PKG_NAME = "com.android.settings";

    private static final String CLASS_NAME = "com.android.settings.MasterClear";
    //modified by tianfangzhou for autotest abort ,212 ,2013.5.21
    //public static List<TestItem> FactoryKit.mItemList;

    //private List<TestItem> FactoryKit.mAutoTestItemList;

    //private List<TestItem> FactoryKit.mUserTestItemList;
    private static String sn;                       //add by bwq for 810 log get sn  20141014
    private static final String KEY_SN = "SN_no";   //add by bwq for 810 log get sn  20141014

    private Bitmap PASS_ICON;

    private Bitmap FAIL_ICON;

    private AutoTestController mAutoTestController;

    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
    long gpsStartTime, wifiStartTime, btStartTime;
    long gpsEndTime, wifiEndTime, btEndTime;
    Float gpsUseTime, wifiUseTime, btUseTime;
    /*Add by zhangkaikai for QW810 Factorylog 2014-10-17 end*/

    //deleted by tianfangzhou for autotest aborted,2013.5.20
    //private boolean FactoryKit.isAutoTesting = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Bundle data = msg.getData();
                refreshTestItem(getItemIndex(data.getString("title")),
                        data.getBoolean("pass"), data.getString("result"), data.getString("time"));//Modify by zhangkaikai for QW810 Factorylog 2014-10-17
            } else if (msg.what == 2) {
                //TODO Auto Test finish
            } else if (msg.what == 3) {
                Utilities.loge(TAG, "msg.what == 3");
                sendBroadcast(new Intent("com.qrt.factory.AutoTestFail"));
            }
        }
    };
    //modified by tianfangzhou for autotest abort ,212 ,2013.5.21
    //private int testMode = 0;

    private InitializeGps mInitializeGps;

    private InitializeWifi mInitializeWifi;

    private InitializeBT mInitializeBT;

    private AudioManager mAudioManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setTitle(getString(R.string.app_name));
      /*add by bwq for 810 log get sn  20141014 begin*/
        final String getsn = getSn();
        sn = ("".equals(getsn.trim())) ? "Unknown" : getsn.trim();
        /*add by bwq for 810 log get sn  20141014 end*/
        init();

        FactoryKit.initItemList(getIntent());
        
       //modified by tianfangzhou for autotest abort ,212 ,2013.5.21
        mAutoTestController = new AutoTestController(this, mHandler,
                FactoryKit.mAutoTestItemList);
        setListAdapter(mBaseAdapter);

        //Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14
//        qcNvItems = new QcNvItems(this);

        mInitializeGps = new InitializeGps();
        mInitializeWifi = new InitializeWifi();
        mInitializeBT = new InitializeBT();

        /*Add by wangwenlong for hidn system ui when full screen (8916) HQ00000000 2014-07-11*/
        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        int groupId = 0;

        SubMenu addMenu = menu.addSubMenu(groupId, MENU_CLEAN_STATE, Menu.NONE,
                R.string.clean_state);
        addMenu.setIcon(android.R.drawable.ic_menu_revert);

        SubMenu clearMenu = menu.addSubMenu(groupId, MENU_MASTER_CLEAR,
                Menu.NONE,
                R.string.master_clear_menu_text);
        clearMenu.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

        SubMenu testInfoMenu = menu
                .addSubMenu(groupId, MENU_TEST_INFO, Menu.NONE,
                        R.string.test_info_menu_text);
        testInfoMenu.setIcon(android.R.drawable.ic_menu_search);

        SubMenu setNvMenu = menu
                .addSubMenu(groupId, MENU_SET_NV, Menu.NONE,
                        R.string.set_nv_menu_text);
        setNvMenu.setIcon(android.R.drawable.ic_menu_manage);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case (MENU_CLEAN_STATE):
                cleanTestState();
                setNV(ControlCenter.this, false);
                break;
            case (MENU_MASTER_CLEAR):
                int version = Build.VERSION.SDK_INT;
                if (version <= 10) {
                    Intent intent = new Intent();
                    intent.setComponent(
                            new ComponentName(PKG_NAME, CLASS_NAME));
                    startActivity(intent);
                } else {
                    startActivity(new Intent(
                            "android.settings.BACKUP_AND_RESET_SETTINGS"));
                }
                break;
            case (MENU_TEST_INFO):
                Intent resultIntent = new Intent(this, ResultActivity.class);
                startActivity(resultIntent);
                break;
            case (MENU_SET_NV):
                buildPasswordDialog(ControlCenter.this).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        if (position == 0) {
            cleanTestState();
            TestSettings.SAVE_RESULT = true;
			//mdoified by tianfangzhou for autotest aborted,2013.5.20
            FactoryKit.isAutoTesting = true;
            mInitializeGps.start();
            mInitializeWifi.start();
            mInitializeBT.start();
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.setParameters("auxmic_test_enabled=true");
			//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
            position = FactoryKit.mAutoTestItemList.size() + 1;
        } else if (position == FactoryKit.mItemList.size() - 1) {
            handleCTAVersionDisplay(this);
            return;
        } else {
            //mdoified by tianfangzhou for autotest aborted,2013.5.20
            FactoryKit.isAutoTesting = false;
            TestSettings.SAVE_RESULT = false;
        }
        startTestActivity(position);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {

        try {
            Utilities.logd(TAG, "onActivityResult ["
                    + "requestCode : " + requestCode + "]\n"
                    + "resultCode : " + resultCode + "]\n"
                    + "Intent : " + data + "]");
        } catch (Exception e) {

        }

        if (data != null && resultCode != 2) {
            refreshTestItem(requestCode,
                    resultCode == RESULT_OK, data.getStringExtra("RESULT"),data.getStringExtra("TIME"));//Modify by zhangkaikai for QW810 Factorylog 2014-10-17
        }

        int index = resultCode == 2 ? requestCode : requestCode + 1;
        //mdoified by tianfangzhou for autotest aborted,2013.5.20
        if (FactoryKit.isAutoTesting) {
		//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
            if (resultCode != 2 && index == FactoryKit.mItemList.size() - 4) {
                mInitializeGps.stop();
                mInitializeWifi.stop();
                mInitializeBT.stop();
                mAutoTestController.startTestInBackground();
            }
       //modified by tianfangzhou for autotest abort ,212 ,2013.5.21
            if (index == FactoryKit.mItemList.size() - 1) {
                //mdoified by tianfangzhou for autotest aborted,2013.5.20
                FactoryKit.isAutoTesting = false;
                saveTestResult();
                //mdoified by tianfangzhou for autotest aborted,2013.5.20
                mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.setParameters("auxmic_test_enabled=false");

                FileOutputStream flashlight = null;
                try {

                    flashlight = new FileOutputStream("/sys/class/leds/flashlight/brightness");
                    flashlight.write(TestSettings.LIGHT_OFF);
                    flashlight.close();
                    flashlight = null;
                } catch (Exception ignored) {

                } finally {
                    if (flashlight != null) {
                        try {
                            flashlight.close();
                        } catch (IOException e) {
                            flashlight = null;
                        }
                    }
                }

                Intent intent = new Intent(this, ResultActivity.class);
                startActivity(intent);
                mAutoTestController.setTestFinish(true);
            } else {
                Log.e(TAG, "index = " + index);
                startTestActivity(index);
            }
        }

    }

    private void saveTestResult() {

        FileOutputStream mFileOutputStream = null;
        try {
            mFileOutputStream = new FileOutputStream(Utilities.getCurrentFile(),
                    true);
       //modified by tianfangzhou for autotest abort ,212 ,2013.5.21
       /*modified by bwq for 810 add log 2014.10.06 begin*/
            for (TestItem testItem : FactoryKit.mItemList) {
                if (null != testItem.getPass()) {
                    String Tag = testItem.getName();
                    String result = testItem.getPass() ?
                            Utilities.RESULT_PASS : Utilities.RESULT_FAIL;
                    /*Modify by zhangkaikai for QW810 Factorylog 2014-10-17 begin*/
                    String time = testItem.getTime();
                    String info = testItem.getResult();
                    StringBuffer resultBuffer = new StringBuffer(
                            "PAT_TEST" + ";" + sn + ";" + Tag + ";");          //add by bwq for 810 log get sn  20141014
                    resultBuffer.append("9999.990" + ";" + "-9999.990" +";"+"0.000"+ ";"+result);
                    if (info.length()!=0 ) {
                        resultBuffer.append(";");
                        resultBuffer.append(info.replace("\n", " "));
                    }
                    if (time != null) {
                        resultBuffer.append(";");
                        if(Tag.equals("Gps")){
                            if(result.equals(Utilities.RESULT_FAIL)){
                                gpsUseTime =Float.parseFloat(String.valueOf(Long.parseLong(time) - gpsStartTime)) / 1000.0f;
                            }
                            resultBuffer.append(gpsUseTime);
                        }else if(Tag.equals("WiFi")){
                            if(result.equals(Utilities.RESULT_FAIL)){
                                wifiUseTime =Float.parseFloat(String.valueOf(Long.parseLong(time) - wifiStartTime)) / 1000.0f;
                            }
                            resultBuffer.append(wifiUseTime);
                        }else if(Tag.equals("Bluetooth")){
                            Log.d(TAG,"onCreate Bluetooth "+btUseTime);
                            if(result.equals(Utilities.RESULT_FAIL)){
                            btUseTime =Float.parseFloat(String.valueOf(Long.parseLong(time) - btStartTime)) / 1000.0f;
                            }
                            resultBuffer.append(btUseTime);
                        }else{
                            resultBuffer.append(time);
                        }
                        if (!info.endsWith("\n")) {
                            resultBuffer.append("\n");
                        }
                    }else{
                        resultBuffer.append(";");
                        if(Tag.equals("WiFi")){
                            resultBuffer.append(wifiUseTime);
                        }else if(Tag.equals("Bluetooth")){
                            resultBuffer.append(btUseTime);
                        }
                        if (!info.endsWith("\n")) {
                            resultBuffer.append("\n");
                        }
                    }
                    if(Tag.equals("Hall")){
                        resultBuffer.append("PAT_TRACK" + ";" + sn + ";"+SystemProperties.get("ro.build.display.id")+";"
                        +new   SimpleDateFormat("yyyy/MM/dd   hh:mm:ss").format(new Date())+ ";" +"000000"+ ";" +"S-MMI"+";"
                        +( isAllPass() ? Utilities.RESULT_PASS : Utilities.RESULT_FAIL)+"\n");
                        resultBuffer.append("#End");
                    }

                    /*Modify by zhangkaikai for QW810 Factorylog 2014-10-17 end*/
                    byte[] buffer = resultBuffer.toString().getBytes();
                    mFileOutputStream.write(buffer);
                    mFileOutputStream.flush();
                    Utilities.logd(TAG,
                            "Writed result= [" + Tag + "] : " + result);
                }
            }
        /*modified by bwq for 810 add log 2014.10.06 end*/
        } catch (Exception e) {
            Utilities.loge(TAG, e);
        } finally {
            try {
                if (null != mFileOutputStream) {
                    mFileOutputStream.close();
                }
                mFileOutputStream = null;
            } catch (IOException e) {
                mFileOutputStream = null;
                Utilities.loge(TAG, e);
            }
        }
    }

    @Override
    public void finish() {

        if ("1".equals(SystemProperties.get("ro.ftmtestmode"))) {
            return;
        }
        if (!mExitFlag) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.control_center_quit_confirm))
                    .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {

                                    mExitFlag = true;
                                    finish();
                                }
                            }).setNegativeButton(getString(R.string.no),
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {

                                }
                            }).create();

            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {
                    float fPx = TypedValue
                            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80,
                                    getResources().getDisplayMetrics());

                    int iPx = Math.round(fPx);

                    Button button = ((AlertDialog) dialog)
                            .getButton(AlertDialog.BUTTON_POSITIVE);

                    if (button != null) {
                        button.setHeight(iPx);
                    }

                    button = ((AlertDialog) dialog)
                            .getButton(AlertDialog.BUTTON_NEGATIVE);

                    if (button != null) {
                        button.setHeight(iPx);
                    }
                }
            });
            alertDialog.show();
            return;
        }

        enableWifi(false);
        enableBluetooth(false);
        enableGPS(false);
        /*start Add by baiwuqiang for hall lock 20140609PM*/
        Intent lock = new Intent("com.android.factory.halllock");
        lock.putExtra("isfactory", 0);
        sendBroadcast(lock);
        /*end Add by by baiwuqiang for hall lock 20140609PM*/
 
	  	/*Add by by baiwuqiang for close ACCELEROMETER_ROTATION 20140707 SW00063781 begin*/
		 if(mDefaultRotation)
		{
        mDefaultRotation = false;	
		Settings.System.putInt(getContentResolver(),
        Settings.System.ACCELEROMETER_ROTATION,
        1);
		}
		/*Add by by baiwuqiang for close ACCELEROMETER_ROTATION 20140707 SW00063781 end*/
        super.finish();
    }

    private BaseAdapter mBaseAdapter = new BaseAdapter() {

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item, null);
            }

            TextView text = (TextView) convertView
                    .findViewById(R.id.text_center);

            ImageView image = (ImageView) convertView
                    .findViewById(R.id.icon_center);
            //modified by tianfangzhou for autotest abort ,212 ,2013.5.21
            text.setText(FactoryKit.mItemList.get(position).getTitle());

            Boolean pass = FactoryKit.mItemList.get(position).getPass();
            if (null != pass) {
                image.setImageBitmap(
                        pass.booleanValue() ? PASS_ICON : FAIL_ICON);
            } else {
                image.setImageBitmap(null);
            }

            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
	    	//mdoified by tianfangzhou for autotest aborted,2013.5.20
            return !FactoryKit.isAutoTesting;
        }

        public int getCount() {
//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
            return FactoryKit.mItemList.size();
        }

        public Object getItem(int position) {
//modified by tianfangzhou for autotest abort ,212 ,2013.5.21		
            return FactoryKit.mItemList.get(position);
        }

        public long getItemId(int arg0) {

            return 0;
        }
    };

    void init() {

        mInflater = LayoutInflater.from(ControlCenter.this);

        PASS_ICON = BitmapFactory
                .decodeResource(ControlCenter.this.getResources(),
                        R.drawable.test_pass);
        FAIL_ICON = BitmapFactory
                .decodeResource(ControlCenter.this.getResources(),
                        R.drawable.test_fail);

        // To save test time, enable some device first
        enableBluetooth(true);
        enableWifi(true);
        enableGPS(true);

        /*Add by wangwenlong for pause music when factory test will start (QL1000) Begin*/
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);
        /*Add by wangwenlong for pause music when factory test will start (QL1000) End*/
        /*start Add by baiwuqiang for hall lock 20140609PM*/
        Intent lock = new Intent("com.android.factory.halllock");
        lock.putExtra("isfactory", 1);
        sendBroadcast(lock);
        /*end Add by by baiwuqiang for hall lock 20140609PM*/

        ContentResolver contentResolver = getContentResolver();
        Settings.System.putInt(contentResolver, Settings.System.DTMF_TONE_WHEN_DIALING, 0);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.unloadSoundEffects();
        Settings.System.putInt(getContentResolver(),
               Settings.System.SOUND_EFFECTS_ENABLED, 0);
        Settings.System.putInt(getContentResolver(),
                Settings.System.HAPTIC_FEEDBACK_ENABLED, 0);   
		/*end Add by wangwenlong for off dtmf when factory test will start (QL1005B) SW00048525 */
		/*Add by by baiwuqiang for close ACCELEROMETER_ROTATION 20140707 SW00063781 begin*/
		int accelerometerDefault = Settings.System.getInt(getContentResolver(),
          Settings.System.ACCELEROMETER_ROTATION, 0);
        if(accelerometerDefault == 1)
		{
        mDefaultRotation = true;	
		Settings.System.putInt(getContentResolver(),
        Settings.System.ACCELEROMETER_ROTATION,
        0);
        }
        /*Add by by baiwuqiang for close ACCELEROMETER_ROTATION 20140707  SW00063781 end*/
    }

    private void startTestActivity(int position) {
	//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
        if (FactoryKit.mItemList != null) {
            TestItem testItem = FactoryKit.mItemList.get(position);
            if (testItem != null) {
                startActivityForResult(testItem.getIntent(), position);
            }
        }
    }

    private void enableGPS(boolean enabled) {
        final ContentResolver resolver = ControlCenter.this
                .getContentResolver();
        Settings.Secure.setLocationProviderEnabled(resolver,
                LocationManager.GPS_PROVIDER,
                enabled);
    }

    private void enableBluetooth(boolean enable) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (enable) {
                mBluetoothAdapter.enable();
            } else {
                mBluetoothAdapter.disable();
            }
        }
    }

    private void enableWifi(boolean enable) {

        WifiManager mWifiManager = (WifiManager) getSystemService(
                Context.WIFI_SERVICE);
        if (mWifiManager != null) {
            mWifiManager.setWifiEnabled(enable);
        }
    }

    private void cleanTestState() {
//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
        for (TestItem testItem : FactoryKit.mItemList) {
            testItem.clearResult();
            testItem.clearPass();
        }

        mBaseAdapter.notifyDataSetChanged();
        Utilities.createNewCurrentFile();
    }


    private TestItem createAutoTestItem() {
        TestItem testItem = new TestItem();
        testItem.setTitle(getString(R.string.auto_test));
        return testItem;
    }

    private TestItem createVersionTestItem() {
        TestItem testItem = new TestItem();
        testItem.setTitle(getString(R.string.version_info));
        return testItem;
    }

    private void handleCTAVersionDisplay(Context context) {
        //Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 Begin
        /*QcNvItemTypes.Nv22AllType nv22AllType = null;
        try {
            nv22AllType = qcNvItems.getNv22All();
        } catch (IOException ignored) {

        }*/
        //Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 End

        StringBuffer info = new StringBuffer();
        info.append(context.getString(R.string.software_version))
                .append(SystemProperties.get("ro.build.display.id"))
                .append("\r\n")
                .append(context.getString(R.string.Hardware_version))
                .append(SystemProperties.get("ro.hardware.custom_version"))
                .append("\r\n")
                .append(context.getString(R.string.Product_version))
                .append(SystemProperties.get("ro.product.model"));

        //Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14
//        appendTestInfo(nv22AllType, info);
        //Add By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 Begin
        TestStatusBase testStatusBase = new TestStatusBase(getApplicationContext(),
                getResources().getInteger(R.integer.default_model));
        info.append(testStatusBase.getDisplayString());

        /*+ "\r\n"
        +context.getString(R.string.build_version) +
        getBuildVersion();*/
        Log.i(TAG, "the cta version is : " + info);
        AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle(R.string.version_info)
                .setMessage(info.toString())
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false).create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                float fPx = TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80,
                                getResources().getDisplayMetrics());

                int iPx = Math.round(fPx);

                Button button = ((AlertDialog) dialog)
                        .getButton(AlertDialog.BUTTON_POSITIVE);

                if (button != null) {
                    button.setHeight(iPx);
                }
            }
        });
        alert.getWindow()
                .setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        alert.show();
    }

    //Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 Begin
    /*private void appendTestInfo(QcNvItemTypes.Nv22AllType nv22AllType,
            StringBuffer info) {

        if (nv22AllType == null) {
            return;
        }

        int defaultModel = getResources().getInteger(R.integer.default_model);
        if (defaultModel == 0) {
            info.append(getString(R.string.cdma_adjustment))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(20))).append("\r\n")
                    .append(getString(R.string.cdma_1x_final_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(21))).append("\r\n")
                    .append(getString(R.string.cdma_evdo_final_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(22))).append("\r\n")
                    .append(getString(R.string.gsm_adjustment))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(24),
                            nv22AllType.getByteByIndex(25))).append("\r\n")
                    .append(getString(R.string.gsm_final_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(28),
                            nv22AllType.getByteByIndex(29))).append("\r\n")
                    .append(getString(R.string.smt_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(23))).append("\r\n")
                    .append(getString(R.string.phone_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(39))).append("\r\n")
                    .append(getString(R.string.coupling))
                    .append(getCouplingTestStromgByNvBtye(
                            nv22AllType.getByteByIndex(34),
                            nv22AllType.getByteByIndex(32)));
        } else if (defaultModel == 1) {
            info.append(getString(R.string.wcdma_adjustment))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(26),
                            nv22AllType.getByteByIndex(27))).append("\r\n")
                    .append(getString(R.string.wcdma_final_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(30),
                            nv22AllType.getByteByIndex(31))).append("\r\n")
                    .append(getString(R.string.gsm_adjustment))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(24),
                            nv22AllType.getByteByIndex(25))).append("\r\n")
                    .append(getString(R.string.gsm_final_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(28),
                            nv22AllType.getByteByIndex(29))).append("\r\n")
                    .append(getString(R.string.smt_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(23))).append("\r\n")
                    .append(getString(R.string.phone_test))
                    .append(getTestStringByNvBtye(
                            nv22AllType.getByteByIndex(39))).append("\r\n")
                    .append(getString(R.string.coupling))
                    .append(getCouplingTestStromgByNvBtye(
                            nv22AllType.getByteByIndex(34),
                            nv22AllType.getByteByIndex(33)));
        }
    }*/
    //Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 End

    private void refreshTestItem(int requestCode, boolean b, String result, String time) {//Modify by zhangkaikai for QW810 Factorylog 2014-10-17
	//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
        TestItem testItem = FactoryKit.mItemList.get(requestCode);
        testItem.setPass(b);
        testItem.clearResult();
        testItem.addResult(result);
        testItem.setTime(time);//Add by zhangkaikai for QW810 Factorylog 2014-10-17

        Utilities.logd(TAG, "Test:" + testItem.getTitle() + " pass = " + b);
        mBaseAdapter.notifyDataSetChanged();
   //modified by tianfangzhou for autotest abort ,212 ,2013.5.21
        if (isAllPass() && FactoryKit.testMode != 2) {
            setNV(ControlCenter.this, true);
        }
    }

    private void setNV(Context context, boolean isAllPass) {
        boolean isSMT = "1".equals(SystemProperties.get("ro.ftmtestmode"));
        //Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 Begin
        /*qcNvItems.setFactoryNV(isAllPass, isSMT);
        Utilities.synchronizedNV();*/
        //Del By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 End

        //Add By Wangwenlong to find test status from file issue (general) HQ00000000 2013-09-14 Begin
        TestStatusBase.setFactoryTestStatus(isAllPass, isSMT);
    }

    private boolean isAllPass() {
	//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
        for (int i = 1; i < FactoryKit.mItemList.size() - 1; i++) {
            TestItem testItem = FactoryKit.mItemList.get(i);
            if (testItem.getPass() == null || !testItem.getPass()) {
                return false;
            }
        }
        return true;
    }

    private int getItemIndex(String title) {
	//modified by tianfangzhou for autotest abort ,212 ,2013.5.21
        for (int i = 0; i < FactoryKit.mItemList.size(); i++) {
            if (FactoryKit.mItemList.get(i).getTitle().equals(title)) {
                return i;
            }
        }
        return -1;
    }

    private Dialog buildPasswordDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater
                .inflate(R.layout.password_dialog, null);
        final EditText editText = (EditText) textEntryView
                .findViewById(R.id.password_edit);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.set_nv_menu_password);
        builder.setView(textEntryView);
        builder.setPositiveButton(R.string.dlg_ok,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = editText.getText().toString();
                        if (password != null && "Qrt*937*70#"
                                .equals(password.trim())) {
                            setNV(ControlCenter.this, true);
                            showToast(getString(R.string.set_nv_pass));
                            dialog.dismiss();
                        } else {
                            showToast(getString(
                                    R.string.set_nv_menu_password_bad));
                        }
                    }
                });
        builder.setNegativeButton(R.string.dlg_cancel,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        return builder.create();
    }

    private void showToast(Object s) {
        if (s != null && !isFinishing()) {
            Toast.makeText(this, s.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //Del By Wangwenlong move to TestStatusBase.java issue (general) HQ00000000 2013-09-14 Begin
    /*private String getCouplingTestStromgByNvBtye(byte b1, byte b2) {
        if (TestSettings.TEST_PASS == b1
                || TestSettings.TEST_PASS == b2) {
            return getString(R.string.nv_pass);
        } else if (TestSettings.NOT_TEST == b1
                && TestSettings.NOT_TEST == b2) {
            return getString(R.string.nv_null);
        } else {
            return getString(R.string.nv_fail);
        }
    }*/

    /*private String getTestStringByNvBtye(byte b) {
        if (TestSettings.NOT_TEST == b) {
            return getString(R.string.nv_null);
        } else if (TestSettings.TEST_PASS == b) {
            return getString(R.string.nv_pass);
        } else {
            return getString(R.string.nv_fail);
        }
    }*/

    /*private String getTestStringByNvBtye(byte b1, byte b2) {
        if (TestSettings.NOT_TEST == b1 && TestSettings.NOT_TEST == b2) {
            return getString(R.string.nv_null);
        } else if (TestSettings.NOT_TEST == b1 && TestSettings.TEST_PASS == b2) {
            return getString(R.string.nv_pass);
        } else {
            return getString(R.string.nv_fail);
        }
    }*/
    //Del By Wangwenlong move to TestStatusBase.java issue (general) HQ00000000 2013-09-14 End

    private class InitializeGps {

        private LocationManager mLocationManager;

        public void start() {
            gpsStartTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            TestSettings.GPS_RESULT = false;
            mLocationManager = (LocationManager) getSystemService(
                    Context.LOCATION_SERVICE);
            if (mLocationManager != null) {

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(true);
                criteria.setBearingRequired(true);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                String provider = mLocationManager
                        .getBestProvider(criteria, true);
                if (provider != null) {
                    mLocationManager
                            .requestLocationUpdates(provider, 500, 0,
                                    mLocationListener);
                    mLocationManager.addGpsStatusListener(gpsStatusListener);
                }

            }
        }

        public void stop() {
            try {
                mLocationManager.removeUpdates(mLocationListener);
                mLocationManager.removeGpsStatusListener(gpsStatusListener);
            } catch (Exception e) {

            }
        }

        LocationListener mLocationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
                TestSettings.GPS_RESULT = true;
                gpsEndTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
                gpsUseTime = Float.parseFloat(String.valueOf(gpsStartTime - gpsEndTime)) / 1000.0f;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            }

            public void onProviderDisabled(String provider) {

            }

            public void onProviderEnabled(String provider) {

            }

            public void onStatusChanged(String provider, int status,
                    Bundle extras) {
            }
        };

        GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

            public void onGpsStatusChanged(int arg0) {

                switch (arg0) {
                    case GpsStatus.GPS_EVENT_FIRST_FIX:

                        TestSettings.GPS_RESULT = true;
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        GpsStatus mGpsStatus = mLocationManager
                                .getGpsStatus(null);
                        Iterable<GpsSatellite> mSatellites = mGpsStatus
                                .getSatellites();
                        Iterator<GpsSatellite> it = mSatellites.iterator();
                        int count = 0;
                        while (it.hasNext()) {
                            //Add By Wangwenlong to delete snr 0 (825) HQ00000000 2013-11-01
                            GpsSatellite gpsS = (GpsSatellite) it.next();
                            if (gpsS.getPrn() < 64 && gpsS.getSnr() > 0) {
                                count++;
                            }
                        }

                        if (count >= TestSettings.GPS_MIN_SAT_NUM) {
                            TestSettings.GPS_RESULT = true;
                            gpsEndTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
                            gpsUseTime = Float.parseFloat(String.valueOf(gpsEndTime - gpsStartTime)) / 1000.0f;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }


    private class InitializeWifi {

        private WifiManager mWifiManager;

        private WifiManager.WifiLock mWifiLock;

        private boolean isOver = false;

        private List<ScanResult> wifiScanResult;

        public void start() {
            wifiStartTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            isOver = false;
            TestSettings.WIFI_RESULT = false;
            mWifiManager = (WifiManager) ControlCenter.this.getSystemService(
                    Context.WIFI_SERVICE);

            mWifiLock = mWifiManager
                    .createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "Auto WiFi");
            if (!mWifiLock.isHeld()) {
                mWifiLock.acquire();
            }

            IntentFilter mFilter = new IntentFilter();
            mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(mReceiver, mFilter);

            new Thread(mOpenThread).start();
        }

        public void stop() {
            isOver = true;
            try {
                if (mWifiLock.isHeld()) {
                    mWifiLock.release();
                }
                unregisterReceiver(mReceiver);
            } catch (Exception e) {

            }
        }

        private void setWifiResult() {
            isOver = true;
            if (wifiScanResult != null && wifiScanResult.size() > 0) {

                TestSettings.WIFI_RESULT = true;
                 wifiEndTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
                 wifiUseTime = Float.parseFloat(String.valueOf(wifiEndTime - wifiStartTime)) / 1000.0f;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            }
        }

        private BroadcastReceiver mReceiver = new BroadcastReceiver() {

            public void onReceive(Context c, Intent intent) {

                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION
                        .equals(intent.getAction())) {
                    Utilities.loge(TAG, "" + intent.getAction());
                    wifiScanResult = mWifiManager.getScanResults();
                    setWifiResult();
                }
                if (WifiManager.WIFI_STATE_CHANGED_ACTION
                        .equals(intent.getAction())) {
                    if (mWifiManager.getWifiState()
                            == WifiManager.WIFI_STATE_ENABLED) {
                        new Thread(mScanThread).start();
                    }
                }
            }
        };

        private Runnable mScanThread = new Runnable() {
            @Override
            public void run() {

                try {
                    while (!Thread.interrupted()) {

                        if (!isOver) {
                            Utilities.logd(TAG, "start wifi scan");
                            if (mWifiManager.getWifiState()
                                    == WifiManager.WIFI_STATE_ENABLED) {
                                mWifiManager.startScan();
                            }
                            Thread.sleep(3000);
                        }
						Thread.sleep(100);
                    }
                } catch (InterruptedException ignored) {

                }

            }
        };

        private Runnable mOpenThread = new Runnable() {
            @Override
            public void run() {

                try {
                    while (!Thread.interrupted()) {

                        if (!isOver) {
                            Utilities.logd(TAG, "start wifi open");
                            if (mWifiManager.getWifiState()
                                    == WifiManager.WIFI_STATE_DISABLED) {
                                enableWifi(true);
                                return;
                            }
                            Thread.sleep(1000);
                        }
						Thread.sleep(100);
                    }
                } catch (InterruptedException ignored) {

                }
            }
        };
    }

    private class InitializeBT {

        private IBluetooth btService;

        private BluetoothAdapter mBluetoothAdapter;

        Time time = new Time();

        long startTime;

        long endTime;

        boolean recordTime = false;

        int btResultCount = 0;

        public void start() {
             btStartTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            btResultCount = 0;
            TestSettings.BT_RESULT = false;
            if (btService == null) {
                IBinder b = ServiceManager.getService("bluetooth");
                btService = IBluetooth.Stub.asInterface(b);
            }

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (null != mBluetoothAdapter) {

                startScanAdapterUpdate();

                IntentFilter filter = new IntentFilter(
                        BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

                if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                    if (BLUETOOTH_SCAN_TO_SUCESS) {
                        scanDevice();
                    } else {
                        TestSettings.BT_RESULT = true;
                    }
                } else {
                    if (mBluetoothAdapter.getState()
                            != BluetoothAdapter.STATE_TURNING_ON) {
                        time.setToNow();
                        startTime = time.toMillis(true);
                        recordTime = true;
                        mBluetoothAdapter.enable();
                    }
                }
                registerReceiver(mReceiver, filter);
            }

        }

        public void stop() {
            try {
                unregisterReceiver(mReceiver);
                if (null != mBluetoothAdapter && mBluetoothAdapter
                        .isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
            } catch (Exception e) {

            }
        }

        private void scanDevice() {

            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
        }

        private void startScanAdapterUpdate() {
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter
                    .getBondedDevices();
            btResultCount += bondedDevices.size();
        }

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    btResultCount++;
                    if (btResultCount >= 1) {
                        TestSettings.BT_RESULT = true;
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                        .equals(action)) {

                    if (btResultCount >= 1) {
                        TestSettings.BT_RESULT = true;
                         btEndTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
                         btUseTime =  Float.parseFloat(String.valueOf(btEndTime - btStartTime)) / 1000.0f;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
                        .equals(action)) {
                    startScanAdapterUpdate();
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED
                        .equals(action)) {

                    if (BluetoothAdapter.STATE_ON == intent
                            .getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {

                        if (BLUETOOTH_SCAN_TO_SUCESS) {
                            scanDevice();
                        } else {
                            TestSettings.BT_RESULT = true;
                        }

                        if (recordTime) {
                            time.setToNow();
                            endTime = time.toMillis(true);
                            recordTime = false;
                        } else if (BluetoothAdapter.STATE_OFF == intent
                                .getIntExtra(
                                        BluetoothAdapter.EXTRA_STATE, 0)) {
                            mBluetoothAdapter.enable();
                        }
                    }
                }
            }
        };
    }
    /*add by bwq for 810 log get sn  20141014 begin*/
    private static String getSn() {
        byte[] data = loadByteFromTestStatusFile();
        String sn = "";
        if (data != null) {
            String temp = new String(data);
            sn = temp;
            if (temp.length() > 32) {
                sn = temp.substring(0, 32);
            }
        }
        return sn;
    }

    private static byte[] loadByteFromTestStatusFile() {
        byte[] buffer = new byte[128];
        InputStream inStream = null;
        try {
            File file = new File("/persist/.sn.bin");
            if (!file.exists()) {
                Log.w(TAG, "/persist/.sn.bin not exists");
                return null;
            }
            inStream = new FileInputStream(file);
            while (inStream.read(buffer) != -1) {
            }
            Log.d(TAG, "/persist/.sn.bin = " + Arrays.toString(buffer));
        } catch (IOException e) {
            buffer = null;
            Log.w(TAG, "loadByteFromTestStatusFile error : ", e);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ignore) {
                }
            }
        }
        return buffer;
    }
    /*add by bwq for 810 log get sn  20141014 end*/
}
