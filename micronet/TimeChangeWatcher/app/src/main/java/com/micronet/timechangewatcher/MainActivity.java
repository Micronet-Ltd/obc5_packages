package com.micronet.timechangewatcher;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Calculate the difference between current time and elapsedRealtime
        long difference = Util.getCurrentDifference();

        //store this difference in SharedPreference (or anywhere)
        Util util = new Util(this);
        util.setLastTimeDifference(difference);
    }
}
