/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.qrt.factory.TestSettings.BLUETOOTH_SCAN_TO_SUCESS;

public class Bluetooth extends AbstractActivity {

    final String TAG = "Bluetooth Test";

    ListView mListView = null;

    Button cancelButton = null;

    Button scanButton = null;

    LayoutInflater mInflater = null;

    BluetoothAdapter mBluetoothAdapter = null;

    List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();

    Set<BluetoothDevice> bondedDevices;

    Time time = new Time();

    long startTime;

    long endTime;

    boolean recordTime = false;

    private final static int MIN_COUNT = 1;

    boolean isUserCanncel = false;

    IntentFilter filter;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        isUserCanncel = false;
        mInflater = LayoutInflater.from(this);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.bluetooth);
        bindView();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null == mBluetoothAdapter) {
            mResultBuffer.append("Default BluetoothAdapter Not Found");
            fail();
            return;
        } else {
            startScanAdapterUpdate();
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                if (BLUETOOTH_SCAN_TO_SUCESS) {
                    scanDevice();
                } else {
                    pass();
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
        }
    }

    void bindView() {

        mListView = (ListView) findViewById(R.id.devices_list);
        mListView.setAdapter(mAdapter);
        scanButton = (Button) findViewById(R.id.bluetooth_scan);
        scanButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                scanDevice();
            }
        });
        cancelButton = (Button) findViewById(R.id.bluetooth_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isUserCanncel = true;
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });
    }

    private void scanDevice() {

        showToast(getString(R.string.bluetooth_scan_start));
        setProgressBarIndeterminateVisibility(true);
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    private void cancelScan() {
        setProgressBarIndeterminateVisibility(false);
        if (null != mBluetoothAdapter && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public void updateAdapter() {
        mAdapter.notifyDataSetChanged();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

//            foundAction(action,device);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                mDeviceList.add(new DeviceInfo(device.getName(),
                        device.getAddress()));
                updateAdapter();
                if (mDeviceList.size() >= MIN_COUNT) {
                    pass();
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                if (mDeviceList.size() >= MIN_COUNT) {
                    pass();
                } else if (!isUserCanncel) {
                    showToast(getString(R.string.bluetooth_scan_null));
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED
                    .equals(action)) {
                startScanAdapterUpdate();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {

                if (BluetoothAdapter.STATE_ON == intent
                        .getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {

                    if (BLUETOOTH_SCAN_TO_SUCESS) {
                        scanDevice();
                    } else {
                        pass();
                    }

                    if (recordTime) {
                        time.setToNow();
                        endTime = time.toMillis(true);
                        recordTime = false;
                        showToast("Turn on bluetooth cost "
                                + (endTime - startTime) / 1000 + "S");
                    } else if (BluetoothAdapter.STATE_OFF == intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE, 0)) {
                        mBluetoothAdapter.enable();
                    }
                } else if (BluetoothAdapter.STATE_TURNING_ON == intent
                        .getIntExtra(
                                BluetoothAdapter.EXTRA_STATE, 0)) {
                    showToast(getString(R.string.bluetooth_turning_on));
                    setProgressBarIndeterminateVisibility(true);
                }
            }
        }

//        private void foundAction(String action, BluetoothDevice device) {
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                mDeviceList.add(new DeviceInfo(device.getName(),
//                        device.getAddress()));
//                updateAdapter();
//                if (mDeviceList.size() >= MIN_COUNT + 1) {
//                    pass();
//                }
//            }
//        }
    };

    BaseAdapter mAdapter = new BaseAdapter() {

        public Object getItem(int arg0) {

            return null;
        }

        public long getItemId(int arg0) {

            return 0;
        }

        public View getView(int index, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.bluetooth_item, null);
            }
            ImageView image = (ImageView) convertView
                    .findViewById(R.id.bluetooth_image);
            TextView text = (TextView) convertView
                    .findViewById(R.id.bluetooth_text);
            text.setText(mDeviceList.get(index).getName() + "\n"
                    + mDeviceList.get(index).getAddress());
            image.setImageResource(android.R.drawable.stat_sys_data_bluetooth);
            return convertView;
        }

        public int getCount() {
            if (mDeviceList != null) {
                return mDeviceList.size();
            } else {
                return 0;
            }
        }
    };

    private void startScanAdapterUpdate() {
        mDeviceList.clear();
        bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            DeviceInfo deviceInfo = new DeviceInfo(device.getName(),
                    device.getAddress());
            mDeviceList.add(deviceInfo);
        }
        updateAdapter();
    }

    protected void onResume() {
        registerReceiver(mReceiver, filter);
        updateAdapter();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    public void finish() {
        cancelScan();
        super.finish();
    }

    private class DeviceInfo {

        private String name = "";

        private String address = "";

        public DeviceInfo(String name, String address) {

            this.name = name;
            this.address = address;
        }

        public String getName() {

            return name;
        }

        public String getAddress() {

            return address;
        }
    }
}
