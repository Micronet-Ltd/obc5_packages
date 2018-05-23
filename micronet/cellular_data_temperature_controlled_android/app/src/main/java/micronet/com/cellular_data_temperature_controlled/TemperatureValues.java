package micronet.com.cellular_data_temperature_controlled;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by eemaan.siddiqi on 12/19/2016.
 */
public class TemperatureValues {

    public static final String TAG = "TemperatureValues";

    public static Boolean HighTempResult;
    public static Boolean NormalTempResult;
    private static String thermalZone0 = "";
    private static String thermalZone1 = "";
    private static String thermalZone2 = "";
    private static String thermalZone3 = "";
    private static String thermalZone4 = "";
    private static String[] thermalZoneVal = new String[5];
    public static String temperaturevalues="";
    private static float zone0;
    private static float zone1;
    private static float zone2;
    private static float zone3;
    private static float zone4;
    private static final float High_Temp =90; //Temperature at which the data is disabled
    private static final float Normal_Temp = 80; //Temperature at which the data is re-enabled

    //Getting core temperatures
    public static void getThermalZoneTemp() {
        try {
            StringBuilder buffer = new StringBuilder();
            int length = thermalZoneVal.length;
            BufferedReader br = new BufferedReader(new FileReader("/sys/devices/virtual/thermal/thermal_zone0/temp"));
            thermalZone0 = br.readLine();
            br = new BufferedReader(new FileReader("/sys/devices/virtual/thermal/thermal_zone1/temp"));
            thermalZone1 = br.readLine();
            br = new BufferedReader(new FileReader("/sys/devices/virtual/thermal/thermal_zone2/temp"));
            thermalZone2 = br.readLine();
            br = new BufferedReader(new FileReader("/sys/devices/virtual/thermal/thermal_zone3/temp"));
            thermalZone3 = br.readLine();
            br = new BufferedReader(new FileReader("/sys/devices/virtual/thermal/thermal_zone4/temp"));
            thermalZone4 = br.readLine();
            br.close();
            //populating the string array:
            thermalZoneVal[0]=thermalZone0;
            thermalZoneVal[1]=thermalZone1;
            thermalZoneVal[2]=thermalZone2;
            thermalZoneVal[3]=thermalZone3;
            thermalZoneVal[4]=thermalZone4;

            for (int index = 0; index < length; index++){
                if(index > 0)
                    buffer.append(" - ");
                buffer.append(thermalZoneVal[index]);
            }
            temperaturevalues =("Temp values: ")+buffer.toString();
            //manipulateString();
            Log.d(TAG, "getThermalZoneTemp: (Zone0-Zone4)   " +temperaturevalues);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Converting string values into integer
    public static void ConvertVal() {
        try {
            getThermalZoneTemp();
            zone0 = Float.valueOf(thermalZone0);
            zone1 = Float.valueOf(thermalZone1);
            zone2 = Float.valueOf(thermalZone2);
            zone3 = Float.valueOf(thermalZone3);
            zone4 = Float.valueOf(thermalZone4);
            //Log.d(TAG, "getThermalZoneTemp result:    " + zone0 + zone1 + zone2 + zone3 + zone4);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //Function which finds if any of the temperature zones are equal to or above 90
    public static Boolean HigherTemp(Context context) {
        //The variable Hresult returns true if any of thermal zone values are above 90 else false
        ConvertVal();
        HighTempResult = false;
        if ((zone0 >= High_Temp) || (zone1 >= High_Temp) || (zone2 >= High_Temp) || (zone3 >= High_Temp) || (zone4 >= High_Temp)) {
            HighTempResult = true;
            Log.d(TAG, "HigherTemp() values Hresult:    " + HighTempResult);
            return HighTempResult;
        } else {
            Log.d(TAG, "HigherTemp() values Hresult:    " + HighTempResult);
            return HighTempResult;
        }
    }
    //Function which finds if all the temperature zones are below 85
    public static boolean NormalTemp(Context context) {
        ConvertVal();
        //The variable Hresult returns true if all the thermal zone values are below 85
        NormalTempResult = false;
            if ((zone0 < Normal_Temp) && (zone1 < Normal_Temp) && (zone2 < Normal_Temp) && (zone3 < Normal_Temp) && (zone4 < Normal_Temp)) {
                NormalTempResult = true;
                Log.d(TAG, "NormalTemp() values Nresult:    " + NormalTempResult);
                return NormalTempResult;
            }
            else {
                Log.d(TAG, "NormalTemp() values Zone values 0-4:    " + zone0 + zone1 + zone2 + zone3 + zone4);
                return NormalTempResult;
            }
    }
}
