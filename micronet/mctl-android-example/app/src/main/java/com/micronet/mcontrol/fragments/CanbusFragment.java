package com.micronet.mcontrol.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.micronet.mcontrol.R;

/**
 * Created by brigham.diaz on 10/26/2016.
 */

public class CanbusFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_section_canbus, container, false);
        return rootView;
    }
}
