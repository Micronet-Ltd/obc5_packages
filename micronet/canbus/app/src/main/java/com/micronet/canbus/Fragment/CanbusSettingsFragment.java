package com.micronet.canbus.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

/**
 * Created by brigham.diaz on 1/13/2017.
 */

public class CanbusSettingsFragment extends Fragment {
    Switch swStartOnBoot;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onStart() {
        super.onStart();
        View rootView = getView();
        //swStartOnBoot = (Switch)rootView.findViewById(R.id.swStartOnBoot);
    }
}
