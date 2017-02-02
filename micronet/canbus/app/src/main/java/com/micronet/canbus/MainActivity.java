package com.micronet.canbus;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by brigham.diaz on 1/27/2017.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FlexCANCanbusInterfaceBridge canbus = new FlexCANCanbusInterfaceBridge();
        canbus.create();
    }
}