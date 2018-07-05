package com.micronet.canbus.Fragment;

import static java.lang.Thread.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.micronet.canbus.CanTest;
import com.micronet.canbus.Info;
import com.micronet.canbus.MainActivity;
import com.micronet.canbus.R;

import java.util.Calendar;
import java.util.Date;

public class Can1OverviewFragment extends Fragment {

    private final String TAG = "Can1OverviewFragment";
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
    private TextView txtCanTxSpeed;
    private TextView txtCanBaudRate;

    private TextView textViewFrames;

    // Socket dependent UI
    private Button btnTransmitCAN;
    private ToggleButton swCycleTransmitJ1939;
    private SeekBar seekBarJ1939Send;

    //Interface dependent UI
    private ToggleButton toggleButtonTerm;
    private ToggleButton toggleButtonListen;
    private RadioGroup baudRateCan1;

    private Button openCan1;
    private Button closeCan1;

    private ChangeBaudRateTask changeBaudRateTask;

    private int mDockState = -1;
    private boolean reopenCANOnDockEvent = false;
    private IntentFilter dockFilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
    private DockStateReceiver dockStateReceiver = new DockStateReceiver();

    public Can1OverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canTest = CanTest.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        //mDockState = null;
        getActivity().registerReceiver(dockStateReceiver, dockFilter);
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(dockStateReceiver);
        super.onPause();
        Log.d(TAG, "onPause");
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
        txtCanTxSpeed = rootView.findViewById(R.id.textViewCan1CurrTransmitInterval);
        txtCanBaudRate = rootView.findViewById(R.id.textViewCan1CurrBaudRate);

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
                openCan1Interface();
            }
        });

        closeCan1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeCan1Interface();
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
                    txtCanTxSpeed.setText(progress + "ms");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //txtCanTxSpeed.setText(canTest.getJ1939IntervalDelay() + "ms");
        updateBaudRateUI();
        updateInterfaceTime();
        updateInterfaceStatusUI();
        setStateInterfaceDependentUI();
        setStateSocketDependentUI();
    }

    private void openCan1Interface(){
        canTest.setRemoveCan1InterfaceState(false);
        canTest.setBaudrate(baudRateSelected);
        canTest.setPortNumber(2);
        canTest.setSilentMode(silentMode);
        canTest.setTermination(termination);
        canTest.setRemoveCan1InterfaceState(false);
        executeChangeBaudrate();
    }

    private void closeCan1Interface(){
        canTest.setRemoveCan1InterfaceState(true);
        executeChangeBaudrate();
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
        if (canTest != null){
            String s = "J1939 Frames/Bytes: " + canTest.getPort1CanbusFrameCount() + "/" + canTest.getPort1CanbusByteCount();
            swCycleTransmitJ1939.setChecked(canTest.isAutoSendJ1939Port1());
            textViewFrames.setText(s);
        }

    }

    private void updateBaudRateUI() {
        String baudrateDesc = getString(R.string._000k_desc);
        if (canTest.getBaudrate() == BITRATE_250K) {
            baudrateDesc = getString(R.string._250k_desc);
        } else if (canTest.getBaudrate() == BITRATE_500K) {
            baudrateDesc = getString(R.string._500k_desc);
        }
        txtCanBaudRate.setText(baudrateDesc);
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
                            sleep(500);
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
            int ret = canTest.CreateCanInterface1(silent,baudrate,termination,port);
            if (ret == 0) {
                LastCreated = Calendar.getInstance().getTime();
            }
            else{
                publishProgress("Closing interface, please wait...");
                canTest.closeCan1Interface();
                publishProgress("Closing socket, please wait...");
                canTest.closeCan1Socket();
                publishProgress("failed");
            }
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

    private class DockStateReceiver extends BroadcastReceiver {
        private CanTest canTest;
        public final String TAG = getClass().getSimpleName();
        public DockStateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mDockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
            try {
                updateCradleIgnState();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateCradleIgnState() throws InterruptedException {
        String cradleStateMsg, ignitionStateMsg;
        switch (mDockState) {
            case Intent.EXTRA_DOCK_STATE_UNDOCKED:
                cradleStateMsg = getString(R.string.not_in_cradle_state_text);
                ignitionStateMsg = getString(R.string.ignition_unknown_state_text);
                if (canTest.isCAN1InterfaceOpen()){
                    Toast.makeText(getContext().getApplicationContext(), "closing CAN1 port since device was undocked", Toast.LENGTH_SHORT).show();
                    closeCan1Interface();
                    reopenCANOnDockEvent = true;
                }
                break;
            case Intent.EXTRA_DOCK_STATE_DESK:
            case Intent.EXTRA_DOCK_STATE_LE_DESK:
            case Intent.EXTRA_DOCK_STATE_HE_DESK:
                cradleStateMsg = getString(R.string.in_cradle_state_text);
                //ignitionStateMsg = getString(R.string.ignition_off_state_text);
                ignitionStateMsg = getString(R.string.ignition_off_state_text);
                if (reopenCANOnDockEvent){
                    Toast.makeText(getContext().getApplicationContext(), "Reopening CAN1 port since device was docked", Toast.LENGTH_SHORT).show();
                    sleep(2000);
                    openCan1Interface();
                    reopenCANOnDockEvent = false;
                }
                break;
            case Intent.EXTRA_DOCK_STATE_CAR:
                cradleStateMsg = getString(R.string.in_cradle_state_text);
                ignitionStateMsg = getString(R.string.ignition_on_state_text);
                if (reopenCANOnDockEvent){
                    Toast.makeText(getContext().getApplicationContext(), "Reopening CAN1 port since device was docked", Toast.LENGTH_SHORT).show();
                    sleep(2000);
                    openCan1Interface();
                    reopenCANOnDockEvent = false;
                }
                break;
            default:
                /* this state indicates un-defined docking state */
                cradleStateMsg = getString(R.string.not_in_cradle_state_text);
                ignitionStateMsg = getString(R.string.ignition_unknown_state_text);
                break;
        }

        TextView cradleStateTextview = (TextView) getView().findViewById(R.id.textViewCradleState);
        TextView ignitionStateTextview = (TextView) getView().findViewById(R.id.textViewIgnitionState);
        cradleStateTextview.setText(cradleStateMsg);
        ignitionStateTextview.setText(ignitionStateMsg);
    }


}
