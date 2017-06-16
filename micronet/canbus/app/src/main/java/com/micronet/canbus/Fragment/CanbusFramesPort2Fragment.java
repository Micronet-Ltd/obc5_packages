package com.micronet.canbus.Fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.micronet.canbus.CanTest;
import com.micronet.canbus.R;

//This class is not being used
public class CanbusFramesPort2Fragment extends Fragment {
    private ArrayAdapter<String> j1939FrameAdapter;
    private TextView lvJ1939Port2Frames;
    private CanTest canTest;
    private Handler mHandler2;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CanbusFramesPort2Fragment() {
    }

    @SuppressWarnings("unused")
    public static CanbusFramesPort2Fragment newInstance(int columnCount) {
        return new CanbusFramesPort2Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canTest = CanTest.getInstance();
        mHandler2 = new Handler(Looper.getMainLooper());

    }

    @Override
    public void onStart() {
        super.onStart();
        mHandler2.post(updateUIRunnable);
    }

    Runnable updateUIRunnable = new Runnable() {
        @Override
        public void run() {
            updateCountUI();
            mHandler2.postDelayed(this, 1000);
        }
    };


    private void updateCountUI()
    {
        if(lvJ1939Port2Frames.length() > 2000) {
            lvJ1939Port2Frames.setText("");
        }
        lvJ1939Port2Frames.append(canTest.can2Data);
        canTest.can2Data.setLength(0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_can2_frames, container, false);
        lvJ1939Port2Frames = (TextView)  view.findViewById(R.id.lvJ1939FramesPort2);
        lvJ1939Port2Frames.setMovementMethod(new ScrollingMovementMethod());
        return view;
    }
}
