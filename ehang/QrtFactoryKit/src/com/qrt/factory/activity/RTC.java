package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created with IntelliJ IDEA. Owner: wangwenlong Date: 2013.01.30 Time: 13:58
 */
public class RTC extends AbstractActivity {

    private static final String TAG = "RTC Test";

    private static final String RTC_ATCION = "RTC_2s";

    @Override
    protected String getTag() {
        return TAG;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rtc);

        Button mButton = (Button) findViewById(R.id.fail);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fail();
            }
        });

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent =new Intent();
        intent.setAction(RTC_ATCION);
        PendingIntent sender=
                PendingIntent.getBroadcast(RTC.this, 0, intent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+2*1000, sender);
    }

    @Override
    protected void onResume() {
        registerReceiver(alarmReceiver, new IntentFilter(RTC_ATCION));
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(alarmReceiver);
        super.onPause();
    }

    public BroadcastReceiver alarmReceiver  = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(RTC_ATCION.equals(intent.getAction())){
                pass();
            }
        }
    };
}