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
import com.micronet.canbus.MainActivity;
import com.micronet.canbus.R;

import java.util.Calendar;
import java.util.Date;

public class Can2OverviewFragment extends Fragment {

    private Date LastCreated;
    private Date LastClosed;

    private int BITRATE_250K = 250000;
    private int BITRATE_500K = 500000;
    private boolean silentMode = false;
    private boolean termination = false;
    private int baudRateSelected = BITRATE_250K;

    private Thread updateUIThread;

    private CanTest canTest;
    private TextView txtInterfaceClsTimeCan2;
    private TextView txtInterfaceOpenTimeCan2;
    private TextView txtCanSpeedCan2;

    private TextView textViewFrames;

    //Socket dependent UI
    private Button btnTransmitCAN2;
    private Switch swCycleTransmitJ1939Can2;
    private SeekBar seekBarJ1939SendCan2;

    //Interface dependent UI
    private ToggleButton toggleButtonTermCan2;
    private ToggleButton toggleButtonListenCan2;
    private RadioGroup baudRateCan2;

    private Button openCan2;
    private Button closeCan2;

    private ChangeBaudRateTask changeBaudRateTask;

    public Can2OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canTest = CanTest.getInstance();
    }

    private void setStateSocketDependentUI() {
        boolean open = canTest.isPort1SocketOpen();
        btnTransmitCAN2.setEnabled(open);
        swCycleTransmitJ1939Can2.setEnabled(open);
        seekBarJ1939SendCan2.setEnabled(open);
    }

    private void setStateInterfaceDependentUI() {
    boolean open = canTest.isCAN2InterfaceOpen();
        //btnGetBaudrate.setEnabled(open);
    }

    private void updateInterfaceStatusUI(String status) {
        final TextView txtInterfaceStatus = getView().findViewById(R.id.textCan2InterfaceStatus);
        if(status != null) {
            txtInterfaceStatus.setText(status);
            txtInterfaceStatus.setBackgroundColor(Color.YELLOW);
        } else if(canTest.isCAN2InterfaceOpen()) {
            txtInterfaceStatus.setText(getString(R.string.open));
            txtInterfaceStatus.setBackgroundColor(Color.GREEN);
        } else { // closed
            txtInterfaceStatus.setText(getString(R.string.closed));
            txtInterfaceStatus.setBackgroundColor(Color.RED);
        }

        final TextView txtSocketStatus = getView().findViewById(R.id.textCan2SocketStatus);
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

        textViewFrames = rootView.findViewById(R.id.textViewCan2Frames);

        baudRateCan2 = rootView.findViewById(R.id.radioGrCan2BaudRates);
        toggleButtonListenCan2 = rootView.findViewById(R.id.toggleButtonListenCan2);
        toggleButtonTermCan2 = rootView.findViewById(R.id.toggleButtonTermCan2);

        openCan2 = rootView.findViewById(R.id.buttonOpenCan2);
        closeCan2 = rootView.findViewById(R.id.buttonCloseCan2);
        txtInterfaceClsTimeCan2 = rootView.findViewById(R.id.textViewClosedTime);
        txtInterfaceOpenTimeCan2 = rootView.findViewById(R.id.textViewCreatedTime);
        txtCanSpeedCan2 = rootView.findViewById(R.id.textViewCan2CurrBaudRate);

        btnTransmitCAN2 = rootView.findViewById(R.id.btnCan2SendJ1939);
        seekBarJ1939SendCan2 = rootView.findViewById(R.id.seekBarSendSpeedCan2);
        swCycleTransmitJ1939Can2 = rootView.findViewById(R.id.swCan2CycleTransmitJ1939);

        seekBarJ1939SendCan2.setProgress(canTest.getJ1939IntervalDelay());
        btnTransmitCAN2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.sendJ1939Port1();
            }
        });

        baudRateCan2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
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

        toggleButtonTermCan2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                termination = isChecked;
            }
        });


        toggleButtonListenCan2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                silentMode = isChecked;
            }
        });

        openCan2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.setRemoveCan2InterfaceState(false);
                canTest.setBaudrate(baudRateSelected);
                canTest.setPortNumber(3);
                canTest.setSilentMode(silentMode);
                canTest.setTermination(termination);
                canTest.setRemoveCan2InterfaceState(false);
                executeChangeBaudrate();
            }
        });

        closeCan2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canTest.setRemoveCan2InterfaceState(true);
                executeChangeBaudrate();

            }
        });

        swCycleTransmitJ1939Can2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canTest.setAutoSendJ1939Port2(swCycleTransmitJ1939Can2.isChecked());
                canTest.sendJ1939Port2();
            }
        });

        seekBarJ1939SendCan2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    canTest.setJ1939IntervalDelay(progress);
                    txtCanSpeedCan2.setText(progress + "ms");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //txtCanSpeedCan2.setText(canTest.getJ1939IntervalDelay() + "ms");
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
        swCycleTransmitJ1939Can2.setChecked(canTest.isAutoSendJ1939Port1());
        textViewFrames.setText(s);
    }

    private void updateBaudRateUI() {
        String baudrateDesc = getString(R.string._000k_desc);
        if (canTest.getBaudrate() == BITRATE_250K) {
            baudrateDesc = getString(R.string._250k_desc);
        } else if (canTest.getBaudrate() == BITRATE_500K) {
            baudrateDesc = getString(R.string._500k_desc);
        }
        txtCanSpeedCan2.setText("Baudrate: " + baudrateDesc);
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

        txtInterfaceOpenTimeCan2.setText(createdDate);
        txtInterfaceClsTimeCan2.setText(closedDate);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_can2_overview, container, false);
        return view;
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
            this.removeInterface=canTest.getRemoveCan2InterfaceState();
        }

        @Override
        protected Void doInBackground(Void... params) {
            LastClosed = Calendar.getInstance().getTime();
            if(canTest.isCAN2InterfaceOpen() || canTest.isPort2SocketOpen()) {
                publishProgress("Closing interface, please wait...");
                canTest.closeCan2Interface();
                publishProgress("Closing socket, please wait...");
                canTest.closeCan2Socket();
            }
            if(removeInterface==true){
                return null;
            }

            publishProgress("Opening, please wait...");
            canTest.CreateCanInterface2(silent,baudrate,termination,port);
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
