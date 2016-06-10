package com.micronet.mcontrol;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MCTL - MainActivity";
    private static final int REQUEST_WRITE_STORAGE = 112;
    public static final long LOG_INTERVAL_MS = 5000;

    private MControlTextAdapter mctlAdapter;
    private Handler saveLogHandler = null;
    private boolean writeLogHeader = true;
    private boolean pauseLog = false;
    private boolean deleteLog = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isStoragePermissionGranted();
        mctlAdapter = new MControlTextAdapter(this);
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(mctlAdapter);

        final Button btnRefresh = (Button) findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mctlAdapter.populateMctlTable();
                mctlAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Data Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        final Button btnDeleteLog = (Button) findViewById(R.id.btnDeleteLog);
        btnDeleteLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLog = true;
            }
        });

        final Button btnPauseLogging = (Button) findViewById(R.id.btnPauseLog);
        btnPauseLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pauseLog) {
                    btnPauseLogging.setText("Pause Logging");
                    saveLogHandler.postDelayed(saveLogRunnable, 0);

                } else {
                    btnPauseLogging.setText("Resume Logging");
                    saveLogHandler.removeCallbacks(saveLogRunnable);
                }
                pauseLog = !pauseLog;
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
        } catch (PackageManager.NameNotFoundException ex) {

        }

        builder.setContentIntent(pIntent);

        // Sets an ID for the notification
        int mNotificationId = 11;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, builder.build());

        startSaveLogThread();
    }

    private void startSaveLogThread() {
        try {
            saveLogHandler = new Handler();
            saveLogHandler.postAtTime(saveLogRunnable, 0);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    Runnable saveLogRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if(deleteLog) {
                    // deleting log here to make prevent race condition on file
                    if(Logger.deleteLog()) {
                        mctlAdapter.clearLogInterval();
                        Toast.makeText(MainActivity.this, "Log Cleared", Toast.LENGTH_SHORT).show();
                        writeLogHeader = true;
                    } else {
                        Toast.makeText(MainActivity.this, "Could not clear log", Toast.LENGTH_SHORT).show();
                    }
                }

                mctlAdapter.increaseLogInterval();
                mctlAdapter.populateMctlTable();
                mctlAdapter.notifyDataSetChanged();
                StringBuilder sb = new StringBuilder();

                if (writeLogHeader) {
                    sb.append("Timestamp,");
                    for (Pair<String, String> pair : mctlAdapter.getPairList()) {
                        sb.append(pair.getLeft() + ",");
                    }
                    // save header without prepending timestamp
                    Logger.log(sb.toString(), false);
                }

                sb = new StringBuilder();
                // write data
                for (Pair<String, String> pair : mctlAdapter.getPairList()) {
                    sb.append(pair.getRight() + ",");
                }

                Logger.log(sb.toString());

                if(Logger.saveLog()) {
                    Toast.makeText(MainActivity.this, "Logs saved to " + Logger.getLogFilePath(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Log saving error", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Toast.makeText(MainActivity.this, "Log saving exception", Toast.LENGTH_SHORT).show();
                Log.d(TAG, ex.getMessage());
            } finally {
                writeLogHeader = false;
                deleteLog = false;

                saveLogHandler.postDelayed(this, LOG_INTERVAL_MS);
            }
        }
    };
}
