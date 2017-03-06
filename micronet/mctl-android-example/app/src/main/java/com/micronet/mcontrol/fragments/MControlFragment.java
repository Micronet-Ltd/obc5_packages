package com.micronet.mcontrol.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.micronet.mcontrol.R;
import com.micronet.mcontrol.Logger;
import com.micronet.mcontrol.adapters.MControlTextAdapter;
import com.micronet.mcontrol.Pair;
import com.micronet.mcontrol.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.micronet.mcontrol.MControl.setSysPropPowerCtlShutdown;

/**
 * Created by brigham.diaz on 10/26/2016.
 */

public class MControlFragment extends Fragment {
    public static final long LOG_INTERVAL_MS = 30000;
    private final String TAG = "MControlFragment";
    private Handler saveLogHandler = null;
    private boolean writeLogHeader = true;
    private boolean pauseLog = false;
    private boolean deleteLog = false;
    private String filename = null;
    private MControlTextAdapter mctlAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_section_hardware, container, false);

        mctlAdapter = new MControlTextAdapter(getContext().getApplicationContext());

        final GridView gridview = (GridView) rootView.findViewById(R.id.gridview);
        gridview.setAdapter(mctlAdapter);

        final Button btnRefresh = (Button) rootView.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mctlAdapter.populateMctlTable();
                mctlAdapter.notifyDataSetChanged();
                Toast.makeText(getContext().getApplicationContext(), "Data Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        final Button btnDeleteLog = (Button) rootView.findViewById(R.id.btnDeleteLog);
        btnDeleteLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLog = true;
            }
        });

        final Button btnPauseLogging = (Button) rootView.findViewById(R.id.btnPauseLog);
        btnPauseLogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pauseLog) {
                    btnPauseLogging.setText("Pause Logging");
                    saveLogHandler.postDelayed(saveLogRunnable, 0);

                } else {
                    btnPauseLogging.setText("Resume Logging");
                    saveLogHandler.removeCallbacks(saveLogRunnable);
                }
                pauseLog = !pauseLog;
            }
        });

        final Button btnSetRTC = (Button) rootView.findViewById(R.id.btnSetRTC);
        btnSetRTC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
                String currentDateTime = simpleDate.format(new Date());
                MControlTextAdapter.mc.set_rtc_date_time(currentDateTime);
                mctlAdapter.populateMctlTable();
                mctlAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "RTC Set", Toast.LENGTH_SHORT).show();
            }
        });

        final Button btnPowerOff = (Button) rootView.findViewById(R.id.btnPowerOff);
        btnPowerOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                try{
                    Process proc = Runtime.getRuntime()
                            .exec(new String[] {"/bin/sh", "-c", "reboot -p" });

                    proc.waitFor();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
                Toast.makeText(getContext(), "Device Power Off in 2 Seconds", Toast.LENGTH_SHORT).show();
*/
                Toast.makeText(getContext(), "Device via OS Power Off in 2 Seconds", Toast.LENGTH_SHORT).show();
                //Shutdown using the OS
                setSysPropPowerCtlShutdown();
/*
                //shutdown via the MCU
                MControlTextAdapter.mc.set_device_power_off(10);
                Toast.makeText(getContext(), "Device Power Off in 10 Seconds", Toast.LENGTH_SHORT).show();
*/
            }
        });




        return rootView;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startSaveLogThread();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        mctlAdapter.populateMctlTable();
        mctlAdapter.notifyDataSetChanged();
        Log.d(TAG, "onResume");
    }

    private void startSaveLogThread() {
        if (saveLogHandler == null) {
            saveLogHandler = new Handler();
            saveLogHandler.postAtTime(saveLogRunnable, 5000);
        }
    }

    Runnable saveLogRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                // generate the filename only once to reuse during app lifetime
                if (filename == null || filename.isEmpty()) {
                    filename = String.format("%s_%s", Utils.formatDateShort(System.currentTimeMillis()), Build.SERIAL);
                }

                if (deleteLog) {
                    // deleting log here to make prevent race condition on file
                    if (Logger.deleteLog()) {
                        mctlAdapter.clearLogInterval();
                        Toast.makeText(getContext(), "Log Cleared", Toast.LENGTH_SHORT).show();
                        writeLogHeader = true;
                    } else {
                        Toast.makeText(getContext(), "Could not clear log", Toast.LENGTH_SHORT).show();
                    }
                }

                mctlAdapter.increaseLogInterval();
                mctlAdapter.populateMctlTable();
                mctlAdapter.notifyDataSetChanged();
                StringBuilder sb = new StringBuilder();

                if (writeLogHeader) {
                    sb.append("Timestamp,");
                    for (Pair<String, String> pair : mctlAdapter.getPairList()) {
                        if (pair.getLeft().equals("THERMAL ZONES")) {
                            sb.append("THERMAL ZONE 0, THERMAL ZONE 1, THERMAL ZONE 2, THERMAL ZONE 3, THERMAL ZONE 4,");
                        } else if (pair.getLeft().equals("SCALING CPU FREQ")) {
                            sb.append("CPU 0, CPU 1, CPU 2, CPU 3,");
                        } else {
                            sb.append(pair.getLeft() + ",");
                        }


                    }
                    // save header without prepending timestamp
                    Logger.log(sb.toString(), false);
                }

                sb = new StringBuilder();
                // build csv
                for (Pair<String, String> pair : mctlAdapter.getPairList()) {
                    sb.append(pair.getRight() + ",");
                }

                Logger.log(sb.toString());
                Logger.createNewLogFile(String.format("%s_mctl.csv", filename), false);
                if (Logger.saveLog()) {
                    Toast.makeText(getContext(), "Logs saved to " + Logger.getLogFilePath(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Log saving error", Toast.LENGTH_SHORT).show();
                }

                // create and save snapshot that includes header and last saved buffer
                StringBuilder header = new StringBuilder();
                header.append("Timestamp,");
                for (Pair<String, String> pair : mctlAdapter.getPairList()) {
                    header.append(pair.getLeft() + ",");
                }
                Logger.log(header.toString(), false);

                Logger.createNewLogFile(String.format("%s_snapshot.csv", filename), true);
                Logger.log(sb.toString());
                Logger.saveLog();


            } catch (Exception ex) {
                Toast.makeText(getContext(), "Log saving exception", Toast.LENGTH_SHORT).show();
                Log.d(TAG, ex.getMessage());
            } finally {
                writeLogHeader = false;
                deleteLog = false;

                saveLogHandler.postDelayed(this, LOG_INTERVAL_MS);
            }
        }
    };
}