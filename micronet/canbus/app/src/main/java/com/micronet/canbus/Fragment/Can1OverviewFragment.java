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
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.micronet.canbus.CanTest;
import com.micronet.canbus.Info;
import com.micronet.canbus.MainActivity;
import com.micronet.canbus.R;

import java.util.Calendar;
import java.util.Date;

public class Can1OverviewFragment extends Fragment {

    private Date LastCreated;
    private Date LastClosed;

    private int BITRATE_250K = 250000;
    private int BITRATE_500K = 500000;
    private boolean silentMode = false;
    private boolean termination = false;
    private int baudRateSelected = BITRATE_250K;

    private Thread updateUIThread;

    private CanTest canTest;
    private TextView txtInterfaceClsTime;
    private TextView txtInterfaceOpenTime;
    private TextView txtCanSpeed;

    private TextView textViewFrames;

    // Socket dependent UI
    private Button btnTransmitCAN;
    private Switch swCycleTransmitJ1939;
    private SeekBar seekBarJ1939Send;

    //Interface dependent UI
    private ToggleButton toggleButtonTerm;
    private ToggleButton toggleButtonListen;
    private RadioGroup baudRateCan1;

    private Button openCan1;
    private Button closeCan1;

    private ChangeBaudRateTask changeBaudRateTask;

    public Can1OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canTest = CanTest.getInstance();
    }

    private void setStateSocketDependentUI() {
        boolean open = canTest.isPort1SocketOpen();
        btnTransmitCAN.setEnabled(open);
        swCycleTransmitJ1939.setEnabled(open);
        seekBarJ1939Send.setEnabled(open);
    }

    private void setStateInterfaceDependentUI() {
    boolean open = canTest.isCAN1InterfaceOpen();
        //btnGetBaudrateCam.setEnabled(open);

    }

    private void updateInterfaceStatusUI(String status) {
        final TextView txtInterfaceStatus = getView().findViewById(R.id.textCan1InterfaceStatus);
        if(status != null) {
            txtInterfaceStatus.setText(status);
            txtInterfaceStatus.setBackgroundColor(Color.YELLOW);
        } else if(canTest.isCAN1InterfaceOpen()) {
            txtInterfaceStatus.setText(getString(R.string.open));
            txtInterfaceStatus.setBackgroundColor(Color.GREEN);
        } else { // closed
            txtInterfaceStatus.setText(getString(R.string.closed));
            txtInterfaceStatus.setBackgroundColor(Color.RED);
        }

        final TextView txtSocketStatus = getView().findViewById(R.id.textCan1SocketStatus);
        if(status != null) {
            txtSocketStatus.setText(status);
            txtSocketStatus.setBackgroundColor(Color.YELLOW);
        } else if(canTest.isPort1SocketOpen()) {
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
        ((MainActivity) getActivity()).setActionBarTitle(String.format("Canbus: %s (API %s)", getAppVersion(), Info.VERSION));

        final View rootView = getView();

        textViewFrames = rootView.findViewById(R.id.textViewCan1Frames);

        baudRateCan1 = rootView.findViewById(R.id.radioGrCan1BaudRates);
        toggleButtonListen = rootView.findViewById(R.id.toggleButtonListenCan1);
        toggleButtonTerm = rootView.findViewById(R.id.toggleButtonTermCan1);

        openCan1 = rootView.findViewById(R.id.buttonOpenCan1);
        closeCan1 = rootView.findViewById(R.id.buttonCloseCan1);
        txtInterfaceClsTime = rootView.findViewById(R.id.textViewClosedTime);
        txtInterfaceOpenTime = rootView.findViewById(R.id.textViewCreatedTime);
        txtCanSpeed = rootView.findViewById(R.id.textViewCan1CurrBaudRate);

        btnTransmitCAN = rootView.findViewById(R.id.btnCan1SendJ1939);
        seekBarJ1939Send = rootView.findViewById(R.id.seekBarSendSpeedCan1);
        swCycleTransmitJ1939 = rootView.findViewById(R.id.swCan1CycleTransmitJ1939);

        seekBarJ1939Send.setProgress(canTest.getJ1939IntervalDelay());
        btnTransmitCAN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.sendJ1939Port1();
            }
        });

        baudRateCan1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radio250K:
                        baudRateSelected = BITRATE_250K;
                        break;
                    case R.id.radio500K:
                        baudRateSelected = BITRATE_500K;
                        break;
                }
            }
        });

        toggleButtonTerm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                termination = isChecked;
            }
        });


        toggleButtonListen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                silentMode = isChecked;
            }
        });

        openCan1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.setRemoveCan1InterfaceState(false);
                canTest.setBaudrate(baudRateSelected);
                canTest.setPortNumber(2);
                canTest.setSilentMode(silentMode);
                canTest.setTermination(termination);
                canTest.setRemoveCan1InterfaceState(false);
                executeChangeBaudrate();
            }
        });

        closeCan1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canTest.setRemoveCan1InterfaceState(true);
                executeChangeBaudrate();

            }
        });

        swCycleTransmitJ1939.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canTest.setAutoSendJ1939Port1(swCycleTransmitJ1939.isChecked());
                canTest.sendJ1939Port1();
            }
        });

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

        //txtCanSpeed.setText(canTest.getJ1939IntervalDelay() + "ms");
        updateBaudRateUI();
        updateInterfaceTime();
        updateInterfaceStatusUI();
        setStateInterfaceDependentUI();
        setStateSocketDependentUI();
    }

    private void
    executeChangeBaudrate() {
        if (changeBaudRateTask == null || changeBaudRateTask.getStatus() != AsyncTask.Status.RUNNING) {
            changeBaudRateTask = new ChangeBaudRateTask( canTest.isSilentChecked(),canTest.getBaudrate(),canTest.getTermination(),canTest.getPortNumber());
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
        String s = "J1939 Frames/Bytes: " + canTest.getPort1CanbusFrameCount() + "/" + canTest.getPort1CanbusByteCount() + "\n";
        swCycleTransmitJ1939.setChecked(canTest.isAutoSendJ1939Port1());
        textViewFrames.setText(s);
    }

    private void updateBaudRateUI() {
        String baudrateDesc = getString(R.string._000k_desc);
        if (canTest.getBaudrate() == BITRATE_250K) {
            baudrateDesc = getString(R.string._250k_desc);
        } else if (canTest.getBaudrate() == BITRATE_500K) {
            baudrateDesc = getString(R.string._500k_desc);
        }
        txtCanSpeed.setText("Baudrate: " + baudrateDesc);
    }

    private void updateInterfaceTime() {
        String closedDate = " None ";
        String createdDate = " None ";
        if(LastClosed != null){
            closedDate = LastClosed.toString();
        }
        if(LastCreated != null){
            createdDate = LastCreated.toString();
        }

        txtInterfaceOpenTime.setText(createdDate);
        txtInterfaceClsTime.setText(closedDate);
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
        return inflater.inflate(R.layout.fragment_can1_overview, container, false);
    }

    private class ChangeBaudRateTask extends AsyncTask<Void, String, Void> {

        int baudrate;
        boolean silent;
        boolean termination;
        int port;
        boolean removeInterface;

        public ChangeBaudRateTask(boolean silent,int baudrate,boolean termination, int port ) {
            this.baudrate = baudrate;
            this.silent = silent;
            this.termination=termination;
            this.port=port;
            this.removeInterface=canTest.getRemoveCan1InterfaceState();
        }

        @Override
        protected Void doInBackground(Void... params) {
            LastClosed = Calendar.getInstance().getTime();
            if(canTest.isCAN1InterfaceOpen() || canTest.isPort1SocketOpen()) {
                publishProgress("Closing interface, please wait...");
                canTest.closeCan1Interface();
                publishProgress("Closing socket, please wait...");
                canTest.closeCan1Socket();
            }
            if(removeInterface==true){
                return null;
            }

            publishProgress("Opening, please wait...");
            canTest.CreateCanInterface1(silent,baudrate,termination,port);
            LastCreated = Calendar.getInstance().getTime();
            return null;
        }

        protected void onProgressUpdate(String... params) {
            updateInterfaceStatusUI(params[0]);
            setStateSocketDependentUI();
            setStateInterfaceDependentUI();
        }

        protected void onPostExecute(Void result) {
            updateBaudRateUI();
            updateInterfaceTime();
            startUpdateUIThread();
            updateInterfaceStatusUI();
            setStateInterfaceDependentUI();
            setStateSocketDependentUI();
        }
    }

}
