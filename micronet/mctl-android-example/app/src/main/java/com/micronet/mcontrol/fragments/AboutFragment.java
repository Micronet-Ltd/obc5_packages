package com.micronet.mcontrol.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.micronet.mcontrol.BuildConfig;
import com.micronet.mcontrol.R;

/**
 * Created by brigham.diaz on 10/26/2016.
 */

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_section_about, container, false);
        TextView txtAbout = (TextView)rootView.findViewById(R.id.txtAbout);
        txtAbout.setText(String.format("MCTL Demo App v %s\nCopyright © 2017 Micronet Inc.\n", BuildConfig.VERSION_NAME));
        return rootView;
    }
}
