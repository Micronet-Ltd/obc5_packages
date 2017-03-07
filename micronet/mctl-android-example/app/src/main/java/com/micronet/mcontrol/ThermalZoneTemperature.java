package com.micronet.mcontrol;

import com.micronet.mcontrol.interfaces.TemperatureInterface;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by brigham.diaz on 1/20/2017.
 */

public class ThermalZoneTemperature implements TemperatureInterface {
    private static final String THERMAL_ZONE_PATH = "/sys/devices/virtual/thermal/thermal_zone%d/temp";
    private static final int NUM_ZONE_TEMPS = 5;
    public ThermalZoneTemperature() { }
    @Override
    public float getTemp() {
        return 0;
    }

    @Override
    public float[] getTemps() {
        float[] temps = new float[NUM_ZONE_TEMPS];

        if(MControl.DBG) { return temps; }
        try {
            for(int i = 0; i < 5; i++) {
                BufferedReader br = new BufferedReader(new FileReader(String.format(THERMAL_ZONE_PATH, i)));
                temps[i] = Float.parseFloat(br.readLine());
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return temps;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        // insert space between each value except last one
        String space = "";
        for(float temp : getTemps()) {
            builder.append(space)
            .append((int)temp);
            space = ", ";
        }
        return builder.toString();
    }
}
