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

/**
 * Created by eemaan.siddiqi on 3/3/2017.
 */

public class CanbusFramesPort1Fragment extends Fragment {
    private ArrayAdapter<String> j1939FrameAdapter;
//    private ArrayAdapter<String> j1708FrameAdapter;
    private TextView lvJ1939FramesPort1;
//    private TextView lvJ1708Frames;
    private CanTest canTest;
    private Handler mHandler;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CanbusFramesPort1Fragment() {
    }

    @SuppressWarnings("unused")
    public static CanbusFramesPort1Fragment newInstance(int columnCount) {
        return new CanbusFramesPort1Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canTest = CanTest.getInstance();
        mHandler = new Handler(Looper.getMainLooper());

    }

    @Override
    public void onStart() {
        super.onStart();
        mHandler.post(updateUIRunnable);
    }

    Runnable updateUIRunnable = new Runnable() {
        @Override
        public void run() {
            updateCountUI();
            mHandler.postDelayed(this, 1000);
        }
    };


    private void updateCountUI()
    {
        if(lvJ1939FramesPort1.length() > 2000) {
            lvJ1939FramesPort1.setText("");
        }
        lvJ1939FramesPort1.append(canTest.can1Data);
        canTest.can1Data.setLength(0);

       /* if(lvJ1708Frames.length() > 2000) {
            lvJ1708Frames.setText("");
        }
        lvJ1708Frames.append(canTest.j1708Data);
        canTest.j1708Data.setLength(0);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_can1_frames, container, false);
        lvJ1939FramesPort1 = (TextView)  view.findViewById(R.id.lvJ1939FramesPort1);
        lvJ1939FramesPort1.setMovementMethod(new ScrollingMovementMethod());
/*      lvJ1708Frames = (TextView)  view.findViewById(R.id.lvJ1708Frames);
        lvJ1708Frames.setMovementMethod(new ScrollingMovementMethod());*/
        return view;
    }
}
