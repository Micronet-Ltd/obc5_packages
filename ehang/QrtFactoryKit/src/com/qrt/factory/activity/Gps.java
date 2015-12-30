/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.TestSettings;

import android.R.drawable;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Gps extends AbstractActivity {

    private static final String TAG = "GPS Test";

    private TextView mTextView;

    private Button startButton;

    private Button stopButton;

    private ListView mListView = null;

    private Location location;

    private LayoutInflater mInflater = null;

    private LocationManager mLocationManager = null;

    private static final int OUT_TIME = 150 * 1000;

    private boolean running = false;

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        mInflater = LayoutInflater.from(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gps);
        getService();
        bindView();
        startGPS();
    }

    CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME, 3000) {

        @Override
        public void onTick(long arg0) {

        }

        @Override
        public void onFinish() {

            mResultBuffer.append(getString(R.string.time_out));
            fail();
        }
    };

    void startGPS() {

        if (running) {
            return;
        }

        mCountDownTimer.start();

        if (!mLocationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER)) {
            showToast(getString(R.string.gps_enable_first));
            final ContentResolver resolver = Gps.this.getContentResolver();
            Settings.Secure.setLocationProviderEnabled(resolver,
                    LocationManager.GPS_PROVIDER,
                    true);
            return;
        }

        Criteria criteria;
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        String provider = mLocationManager.getBestProvider(criteria, true);
        if (provider == null) {
            mResultBuffer.append("Fail to get GPS Provider!");
            fail();

        }
        mLocationManager
                .requestLocationUpdates(provider, 500, 0, mLocationListener);
        mLocationManager.addGpsStatusListener(gpsStatusListener);

        location = mLocationManager.getLastKnownLocation(provider);
        setLocationView(location);
        running = true;
    }

    @Override
    protected void onPause() {
        stopGPS();
        super.onPause();
    }

    private void setLocationView(Location location) {

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            double speed = location.getSpeed();
            double altitude = location.getAltitude();
            double bearing = location.getBearing();
            String text = getString(R.string.gps_latitude) + latitude + "\n"
                    + getString(R.string.gps_longitude) + longitude + "\n"
                    + getString(R.string.gps_speed) + speed + getString(
                    R.string.gps_speed_unit) + "\n"
                    + getString(R.string.gps_altitude) + altitude + getString(
                    R.string.gps_altitude_unit) + "\n"
                    + getString(R.string.gps_bearing) + bearing + "\n";
            mTextView.setText(text);
        } else {
            mTextView.setText(R.string.gps_unknown);
        }
    }

    LocationListener mLocationListener = new LocationListener() {

        public void onLocationChanged(Location location) {

            setLocationView(location);
            pass();
        }

        public void onProviderDisabled(String provider) {

            setLocationView(null);
        }

        public void onProviderEnabled(String provider) {

            showToast(getString(R.string.gps_provider_enabled));

        }

        public void onStatusChanged(String provider, int status,
                Bundle extras) {

        }
    };

    private GpsStatus mGpsStatus;

    private Iterable<GpsSatellite> mSatellites;

    List<String> satelliteList = new ArrayList<String>();

    GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

        public void onGpsStatusChanged(int arg0) {

            switch (arg0) {
                case GpsStatus.GPS_EVENT_STARTED:
                    showToast(getString(R.string.gps_start));
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    // toast("GPS Stop");

                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    showToast(getString(R.string.gps_locate_sucess));
                    pass();
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    logd("GPS_EVENT_SATELLITE_STATUS");
                    mGpsStatus = mLocationManager.getGpsStatus(null);
                    mSatellites = mGpsStatus.getSatellites();
                    Iterator<GpsSatellite> it = mSatellites.iterator();
                    int count = 0;
                    satelliteList.clear();
                    while (it.hasNext()) {
                        GpsSatellite gpsS = (GpsSatellite) it.next();
                        //Add By Wangwenlong to delete snr 0 (825) HQ00000000 2013-11-01
                        if (gpsS.getPrn() < 64 && gpsS.getSnr() > 0) {
                            satelliteList.add(count++, gpsS.toString());
                        }
                    }
                    updateAdapter();
                    if (count >= TestSettings.GPS_MIN_SAT_NUM) {
                        pass();
                    }
                    break;
                default:
                    break;
            }

        }

    };

    public void updateAdapter() {

        mAdapter.notifyDataSetChanged();
    }

    void stopGPS() {

        try {
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager.removeGpsStatusListener(gpsStatusListener);
        } catch (Exception e) {
            loge(e);
        }
        running = false;
    }

    void bindView() {

        mTextView = (TextView) findViewById(R.id.gps_hint);
        startButton = (Button) findViewById(R.id.gps_start);
        startButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                mCountDownTimer.start();
                startGPS();
            }
        });
        stopButton = (Button) findViewById(R.id.gps_stop);
        stopButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                mResultBuffer.append(getString(R.string.user_canceled));
                fail();
            }
        });

        mListView = (ListView) findViewById(R.id.gps_list);
        mListView.setAdapter(mAdapter);
        registerForContextMenu(mListView);

    }

    public void finish() {
        stopGPS();
        super.finish();
    }

    void getService() {

        mLocationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            mResultBuffer.append("Fail to get LOCATION_SERVICE!");
            fail();
        }
    }

    @Override
    protected void onDestroy() {

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        super.onDestroy();
    }

    BaseAdapter mAdapter = new BaseAdapter() {

        public Object getItem(int arg0) {

            return null;
        }

        public long getItemId(int arg0) {

            return 0;
        }

        public View getView(int index, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.gps_item, null);
            }
            TextView mText = (TextView) convertView.findViewById(R.id.gps_text);
            ImageView mImage = (ImageView) convertView
                    .findViewById(R.id.gps_image);
            mText.setText(satelliteList.get(index));
            mImage.setImageResource(drawable.presence_online);
            return convertView;
        }

        public int getCount() {

            if (satelliteList != null) {
                return satelliteList.size();
            } else {
                return 0;
            }
        }
    };
}
