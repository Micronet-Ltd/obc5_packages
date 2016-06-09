package com.micronet.mcontrol;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MControlTextAdapter mctlAdapter;
    private Handler saveLogHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mctlAdapter = new MControlTextAdapter(this);
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(mctlAdapter);

        Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mctlAdapter.populateMctlTable();
                mctlAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Data Refreshed", Toast.LENGTH_SHORT).show();
            }
        });


        Intent resultIntent = new Intent(this, MainActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        // build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_subtext))
                .setContentIntent(pIntent);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int versionCode = pInfo.versionCode;
            builder.setContentInfo(String.format("%s (%d)", version, versionCode));
        } catch(PackageManager.NameNotFoundException ex) {

        }

        PendingIntent resultPendingIntent;
        builder.setContentIntent(pIntent);

        // Sets an ID for the notification
        int mNotificationId = 11;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, builder.build());

        try {
            saveLogHandler = new Handler();
            saveLogHandler.postAtTime(saveLogRunnable, 0);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public static final long LOG_INTERVAL_MS = 5000;

    Runnable saveLogRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                mctlAdapter.populateMctlTable();
                mctlAdapter.notifyDataSetChanged();
                for(Pair<String, String> pair : mctlAdapter.getPairList()) {
                    Logger.log(pair.getLeft() + ": " + pair.getRight());
                }
                Logger.saveLog();
                Toast.makeText(MainActivity.this, "Logs saved to " + Logger.getLogFilePath(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {

            } finally {
                saveLogHandler.postDelayed(this, LOG_INTERVAL_MS);
            }
        }
    };


}
