
package com.micronet.canbus.Fragment;

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
import com.micronet.canbus.R;


/**
 * Created by eemaan.siddiqi on 9/11/2017.
 */



public class J1708OverviewFragment extends Fragment {

    private Thread updateUIThread;
    private  ConfigureInterface configureInterface;

    private CanTest canTest;
    private TextView txtBaud;
    private TextView txtCanSpeed;
    private TextView txtJ1708Speed;
    private TextView textView;

/*
  *  Socket dependent UI
  * */

    private Button btnTransmitJ1708;
    private Switch swCycleTransmitJ1708;
    private SeekBar seekBarJ1708Send;

/*
    * Interface dependent UI
    * *//*
*/

    private Button btnCreateInterface;

    //private ChangeBaudRateTask changeBaudRateTask;
    Switch swFilters;

    public J1708OverviewFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canTest = CanTest.getInstance();
    }

    private void setStateSocketDependentUI() {
        boolean open = canTest.isj1708SocketOpen();
        btnTransmitJ1708.setEnabled(open);
        swCycleTransmitJ1708.setEnabled(open);
        seekBarJ1708Send.setEnabled(open);
    }

    private void setStateInterfaceDependentUI() {
        boolean open = canTest.isJ1708InterfaceOpen();

    }

    private void updateInterfaceStatusUI(String status) {
        final TextView txtInterfaceStatus = getView().findViewById(R.id.txtJ1708InterfaceStatus);
        if(status != null) {
            txtInterfaceStatus.setText(status);
            txtInterfaceStatus.setBackgroundColor(Color.YELLOW);
        } else if(canTest.isJ1708InterfaceOpen()) {
            txtInterfaceStatus.setText(getString(R.string.open));
            txtInterfaceStatus.setBackgroundColor(Color.GREEN);
        } else { // closed
            txtInterfaceStatus.setText(getString(R.string.closed));
            txtInterfaceStatus.setBackgroundColor(Color.RED);
        }

        final TextView txtSocketStatus = getView().findViewById(R.id.txtJ1708SocketStatus);
        if(status != null) {
            txtSocketStatus.setText(status);
            txtSocketStatus.setBackgroundColor(Color.YELLOW);
        } else if(canTest.isj1708SocketOpen()) {
            txtSocketStatus.setText(getString(R.string.open));
            txtSocketStatus.setBackgroundColor(Color.GREEN);
        } else { // closed
            txtSocketStatus.setText(getString(R.string.closed));
            txtSocketStatus.setBackgroundColor(Color.RED);
        }
    }

    private void
    updateInterfaceStatusUI() {
        updateInterfaceStatusUI(null);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set title bar
        //((MainActivity) getActivity()).setActionBarTitle(String.format("Native CANbus %s   (CANbus API %s)", getAppVersion(), Info.VERSION));

        View rootView = getView();

        final Button btnCloseInterface = rootView.findViewById(R.id.btnCloseInterface);
        btnCreateInterface = rootView.findViewById(R.id.btnInterface1708);
        btnTransmitJ1708 = rootView.findViewById(R.id.btnSendJ1708);
        seekBarJ1708Send = rootView.findViewById(R.id.seekBarSendJ1708Speed);
        textView = rootView.findViewById(R.id.txt1708Frames);
        swCycleTransmitJ1708 = rootView.findViewById(R.id.swCycleTransmitJ1708);


        btnTransmitJ1708.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.sendJ1708();
            }
        });

        btnCreateInterface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int code = canTest.create1708Interface();
                canTest.setRemoveJ1708InterfaceState(false);
                changeJ1708Interface();
            }
        });

        btnCloseInterface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                canTest.setRemoveJ1708InterfaceState(true);
                changeJ1708Interface();
                //int code=canTest.closeJ1708Interface();

            }
        });

        swCycleTransmitJ1708.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canTest.setAutoSendJ1708(swCycleTransmitJ1708.isChecked());
                canTest.sendJ1708();
            }
        });


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

        //txtCanSpeed.setText(canTest.getJ1939IntervalDelay() + "ms");
        //txtJ1708Speed.setText(canTest.getJ1708IntervalDelay() + "ms");
        updateInterfaceStatusUI();
        setStateInterfaceDependentUI();
        setStateSocketDependentUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_1708_overview, container, false);
    }

    private void changeJ1708Interface() {
        if (configureInterface == null
                || configureInterface.getStatus() != AsyncTask.Status.RUNNING) {
            configureInterface = new ConfigureInterface();
            configureInterface.execute();
        }
    }

    private void updateCountUI() {
        String s = "\nJ1708 Frames/Bytes: " + canTest.getJ1708FrameCount() + "/" + canTest.getJ1708ByteCount();
        swCycleTransmitJ1708.setChecked(canTest.isAutoSendJ1708());
        textView.setText(s);
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

    private class ConfigureInterface extends AsyncTask<Void, String, Void> {
        boolean removeJ1708;
        public ConfigureInterface() {
        this.removeJ1708=canTest.getRemove1708InterfaceState();
        }

        @Override
        protected Void doInBackground(Void... params) {
            if(canTest.isJ1708InterfaceOpen() || canTest.isj1708SocketOpen()) {
                publishProgress("Closing interface, please wait...");
                canTest.closeJ1708Interface();
                publishProgress("Closing socket, please wait...");
                canTest.closeJ1708Socket();
            }

            if(removeJ1708){
                return null;

            }

            publishProgress("Opening, please wait...");
            canTest.create1708Interface();
            return null;
        }

        protected void onProgressUpdate(String... params) {
            updateInterfaceStatusUI(params[0]);
            setStateSocketDependentUI();
            setStateInterfaceDependentUI();
        }

        protected void onPostExecute(Void result) {
            startUpdateUIThread();
            updateInterfaceStatusUI();
            setStateInterfaceDependentUI();
            setStateSocketDependentUI();
        }
    }


}
