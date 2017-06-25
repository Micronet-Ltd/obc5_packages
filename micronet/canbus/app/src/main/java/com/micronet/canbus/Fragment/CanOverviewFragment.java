package com.micronet.canbus.Fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.micronet.canbus.CanTest;
import com.micronet.canbus.Info;
import com.micronet.canbus.R;
import com.micronet.canbus.MainActivity;

public class CanOverviewFragment extends Fragment {
    private int BITRATE_250K = 6;
    private int BITRATE_500K = 7;

    private Thread updateUIThread;

    private CanTest canTest;
    private TextView txtBaud;
    private TextView txtCanSpeed;
    private TextView txtJ1708Speed;
    private TextView textView;

    /*
        Socket dependent UI
     */
    private Button btnTransmitCAN;
    /*  private Button btnTransmitJ1708;
      private Switch swCycleTransmitJ1708;*/
    private Switch swCycleTransmitJ1939;
    private Switch swDiscardInBuffer;
    private SeekBar seekBarJ1939Send;
    /*  private SeekBar seekBarJ1708Send;*/
    /*
        Interface dependent UI
     */
    private Button btnGetBaudrate;
    private Switch swSilentMode;
    private Button btn250K;
    private Button btn500K;

    private ChangeBaudRateTask changeBaudRateTask;
    Switch swFilters;

    public CanOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canTest = CanTest.getInstance();
    }

    private void setStateSocketDependentUI() {
        boolean open = canTest.isSocketOpen();
        btnTransmitCAN.setEnabled(open);
      /*  btnTransmitJ1708.setEnabled(open);*/
        swCycleTransmitJ1939.setEnabled(open);
/*        swCycleTransmitJ1708.setEnabled(open);*/
        seekBarJ1939Send.setEnabled(open);
       /* seekBarJ1708Send.setEnabled(open);*/
    }

    private void setStateInterfaceDependentUI() {
        boolean open = canTest.isInterfaceOpen();
        btnGetBaudrate.setEnabled(open);
        swFilters.setEnabled(open);
    }

    private void updateInterfaceStatusUI(String status) {
        final TextView txtInterfaceStatus = (TextView) getView().findViewById(R.id.txtInterfaceStatus);
        if(status != null) {
            txtInterfaceStatus.setText(status);
            txtInterfaceStatus.setBackgroundColor(Color.YELLOW);
        } else if(canTest.isInterfaceOpen()) {
            txtInterfaceStatus.setText(getString(R.string.open));
            txtInterfaceStatus.setBackgroundColor(Color.GREEN);
        } else { // closed
            txtInterfaceStatus.setText(getString(R.string.closed));
            txtInterfaceStatus.setBackgroundColor(Color.RED);
        }

        final TextView txtSocketStatus = (TextView) getView().findViewById(R.id.txtSocketStatus);
        if(status != null) {
            txtSocketStatus.setText(status);
            txtSocketStatus.setBackgroundColor(Color.YELLOW);
        } else if(canTest.isSocketOpen()) {
            txtSocketStatus.setText(getString(R.string.open));
            txtSocketStatus.setBackgroundColor(Color.GREEN);
        } else { // closed
            txtSocketStatus.setText(getString(R.string.closed));
            txtSocketStatus.setBackgroundColor(Color.RED);
        }
    }

    private void updateInterfaceStatusUI() {
        updateInterfaceStatusUI(null);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set title bar
        ((MainActivity) getActivity()).setActionBarTitle(String.format("CanTest: %s (API %s)", getAppVersion(), Info.VERSION));

        View rootView = getView();

        final Button btnCloseInterface = (Button) rootView.findViewById(R.id.btnCloseInterface);
        final Switch swBlockOnRead = (Switch) rootView.findViewById(R.id.swBlockOnRead);

        btn250K = (Button) rootView.findViewById(R.id.baud250);
        btn500K = (Button) rootView.findViewById(R.id.baud500);
        btnTransmitCAN = (Button) rootView.findViewById(R.id.btnSendJ1939);
        /*btnTransmitJ1708 = (Button) rootView.findViewById(R.id.btnSendJ1708);*/
        btnGetBaudrate = (Button) rootView.findViewById(R.id.btnGetBaudrate);
        seekBarJ1939Send = (SeekBar) rootView.findViewById(R.id.seekBarSendSpeed);
      /*  seekBarJ1708Send = (SeekBar) rootView.findViewById(R.id.seekBarSendJ1708Speed);*/
        txtCanSpeed = (TextView) rootView.findViewById(R.id.txtCanSpeed);
     /*   txtJ1708Speed = (TextView) rootView.findViewById(R.id.txtJ1708Speed);*/
        textView = (TextView) rootView.findViewById(R.id.textView);
        txtBaud = (TextView) rootView.findViewById(R.id.txtBaud);
        swSilentMode = (Switch) rootView.findViewById(R.id.swSilentMode);
        swDiscardInBuffer = (Switch) rootView.findViewById(R.id.swDiscardInBuffer);
        swFilters = (Switch) rootView.findViewById(R.id.swFilters);
        swCycleTransmitJ1939 = (Switch) rootView.findViewById(R.id.swCycleTransmitJ1939);
      /*  swCycleTransmitJ1708 = (Switch) rootView.findViewById(R.id.swCycleTransmitJ1708);*/

        seekBarJ1939Send.setProgress(canTest.getJ1939IntervalDelay());
        btnTransmitCAN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.sendJ1939();
            }
        });


        /* for J1708 library */
  /*      btnTransmitJ1708.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.sendJ1708();
            }
        });*/

        swFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (swFilters.isChecked()) {
                    canTest.setMasks();
                } else {
                    canTest.clearFilters();
                }*/
                if (swFilters.isChecked()) {
                    canTest.setFilters();
                } else {
                    canTest.clearFilters();
                }
            }
        });

        swSilentMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.silentMode(swSilentMode.isChecked());
                if (canTest.isInterfaceOpen()) {
                    executeChangeBaudrate();
                }
            }
        });

        btn250K.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.setBaudrate(BITRATE_250K);
                executeChangeBaudrate();
            }
        });

        btn500K.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.setBaudrate(BITRATE_500K);
                executeChangeBaudrate();
            }
        });

        btnGetBaudrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBaudRateUI();
            }
        });

        swDiscardInBuffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.setDiscardInBuffer(swDiscardInBuffer.isChecked());
            }
        });

/*
        btnCloseInterface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canTest.setBaudrate(0);
                executeChangeBaudrate();

            }
        });*/

        swCycleTransmitJ1939.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canTest.setAutoSendJ1939(swCycleTransmitJ1939.isChecked());
                canTest.sendJ1939();
            }
        });

/*        swCycleTransmitJ1708.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.setAutoSendJ1708(swCycleTransmitJ1708.isChecked());
                canTest.sendJ1708();
            }
        });*/

        seekBarJ1939Send.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    canTest.setJ1939IntervalDelay(progress);
                    txtCanSpeed.setText(progress + "ms");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

/*
        seekBarJ1708Send.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    canTest.setJ1708IntervalDelay(progress);
                    txtJ1708Speed.setText(progress + "ms");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
*/

        swBlockOnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.setBlockOnRead(swBlockOnRead.isChecked());
            }
        });

        txtCanSpeed.setText(canTest.getJ1939IntervalDelay() + "ms");
        /*txtJ1708Speed.setText(canTest.getJ1708IntervalDelay() + "ms");*/
        updateBaudRateUI();
        updateInterfaceStatusUI();
       /* setStateInterfaceDependentUI();*/
        setStateSocketDependentUI();
    }

    private void executeChangeBaudrate() {
        if (changeBaudRateTask == null || changeBaudRateTask.getStatus() != AsyncTask.Status.RUNNING) {
            changeBaudRateTask = new ChangeBaudRateTask( swSilentMode.isChecked(),canTest.getBaudrate(),canTest.getTermination(),canTest.getPortNumber());
            changeBaudRateTask.execute();
        }
    }

    private String getAppVersion() {
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }

    private void updateCountUI() {
        String s = "J1939 Frames/Bytes: " + canTest.getCanbusFrameCount() + "/" + canTest.getCanbusByteCount() + "\n";
//
//                + "Rollovers/MaxDiff: " + canTest.getCanbusRollovers() + "/" + canTest.getCanbusMaxdiff() + "\n";

      /*  *//* for J1708 library *//*
        s += "\nJ1708 Frames/Bytes: " + canTest.getJ1708FrameCount() + "/" + canTest.getJ1708ByteCount();
*/
        swCycleTransmitJ1939.setChecked(canTest.isAutoSendJ1939());
        textView.setText(s);
    }

    private void updateBaudRateUI() {
        String baudrateDesc = getString(R.string._000k_desc);
        if (canTest.getBaudrate() == BITRATE_250K) {
            baudrateDesc = getString(R.string._250k_desc);
        } else if (canTest.getBaudrate() == BITRATE_500K) {
            baudrateDesc = getString(R.string._500k_desc);
        }
        txtBaud.setText("Baudrate: " + baudrateDesc);
    }

    private void startUpdateUIThread() {
        if (updateUIThread == null) {
            updateUIThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateCountUI();
                            }
                        });
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }

        if (updateUIThread != null && !updateUIThread.isAlive()) {
            updateUIThread.start();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_can_overview, container, false);
    }

    private class ChangeBaudRateTask extends AsyncTask<Void, String, Void> {

        int baudrate;
        boolean silent;
        boolean termination;
        int port;

        public ChangeBaudRateTask(boolean silent,int baudrate,boolean termination, int port ) {
            this.baudrate = baudrate;
            this.silent = silent;
            this.termination=termination;
            this.port=port;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(canTest.isInterfaceOpen() || canTest.isSocketOpen()) {
                publishProgress("Closing interface, please wait...");
                canTest.closeInterface();
                publishProgress("Closing socket, please wait...");
                canTest.closeSocket();
            }
            if(baudrate == 0) {
                return null;
            }
            publishProgress("Opening, please wait...");
            canTest.CreateInterface(silent,baudrate,termination,port);
            return null;
        }

        protected void onProgressUpdate(String... params) {
            updateInterfaceStatusUI(params[0]);
            setStateSocketDependentUI();
            setStateInterfaceDependentUI();
        }

        protected void onPostExecute(Void result) {
            updateBaudRateUI();
            startUpdateUIThread();
            updateInterfaceStatusUI();
            setStateInterfaceDependentUI();
            setStateSocketDependentUI();
        }
    }
}