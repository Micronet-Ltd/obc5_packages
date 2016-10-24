package com.micronet.mcontrol;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by brigham.diaz on 5/24/2016.
 */
public class MControlTextAdapter extends BaseAdapter {

    private Context context;
    private static final String TAG = "MControlTextAdapter";
    final String DEGREE = "\u00b0";
    final String BRIGHTNESS = "\u2600";
    static public MControl mc = null;
    private int[] leftLEDVal = new int[]{-1, 0xFFFFFF};
    private int[] centerLEDVal = new int[]{-1, 0xFFFFFF};
    private int[] rightLEDVal = new int[]{-1, 0xFFFFFF};
    private int logInterval = 1;
    private BufferedReader br;
    private BufferedWriter bw;
    String gpio917Value = null;
    private float linear_acceleration[];
    private String thermalZone0 = "";
    private String thermalZone1 = "";
    private String thermalZone2 = "";
    private String thermalZone3 = "";
    private String thermalZone4 = "";
    private String scaling_cur_freq = "";
    private int cpu0 = 0;
    private int cpu1 = 0;
    private int cpu2 = 0;
    private int cpu3 = 0;

    private List<Pair<String, String>> pairList = new ArrayList<Pair<String, String>>();

    public MControlTextAdapter(Context context) {
        this.context = context;
        // initialize mctl
        mc = new MControl();
        populateMctlTable();
    }

    public List<Pair<String, String>> getPairList() {
        return pairList;
    }

    public void populateMctlTable() {

        linear_acceleration = new float[]{0.0f, 0.0f, 0.0f};

        String getrtc = mc.get_rtc_date_time();
        String mcuver = mc.get_mcu_version();
        String fpgaver = mc.get_fpga_version();
        String adc_gpio_in1 = ADCs.ADC_GPIO_IN1.getValue() + " mv";
        String adc_gpio_in2 = ADCs.ADC_GPIO_IN2.getValue() + " mv";
        String adc_gpio_in3 = ADCs.ADC_GPIO_IN3.getValue() + " mv";
        String adc_gpio_in4 = ADCs.ADC_GPIO_IN4.getValue() + " mv";
        String adc_gpio_in5 = ADCs.ADC_GPIO_IN5.getValue() + " mv";
        String adc_gpio_in6 = ADCs.ADC_GPIO_IN6.getValue() + " mv";
        String adc_gpio_in7 = ADCs.ADC_GPIO_IN7.getValue() + " mv";
        String adc_power_in = ADCs.ADC_POWER_IN.getValue() + " mv";
        String adc_power_cap = ADCs.ADC_POWER_VCAP.getValue() + " mv";
        String rtc_battery = mc.check_rtc_battery();
        requestGPIO917();
        String power_on_reason = mc.get_power_on_reason();
        scaling_cur_freq = "";
        getCoreFrequencies();
        String celsius = String.valueOf((ADCs.ADC_TEMPERATURE.getValue() - 500.0f) / 10);
        getThermalZoneTemps();
        String thermalZone = thermalZone0 + ", " + thermalZone1 + ", " + thermalZone2 + ", " + thermalZone3 + ", " + thermalZone4;
        String accelerometer = "X: " + linear_acceleration[0] + " Y: " + linear_acceleration[1] + " Z: " + linear_acceleration[2];
        String adc_cable_type = ADCs.ADC_CABLE_TYPE.getValue() + " mv";
        int[] rtc_cal = mc.get_rtc_cal_reg();
        String dig_rtc_cal_reg = String.valueOf(rtc_cal[0]);
        String ana_rtc_cal_reg = String.valueOf(rtc_cal[1]);

        LEDs left = mc.get_led_status(LEDInterface.LEFT);
        LEDs center = mc.get_led_status(LEDInterface.CENTER);
        LEDs right = mc.get_led_status(LEDInterface.RIGHT);
        String brightness = left.BRIGHTNESS + " " + center.BRIGHTNESS + " " + right.BRIGHTNESS;
        String leftLED = String.format("%d %d %d %d", left.RED, left.GREEN, left.BLUE, left.BRIGHTNESS);
        String centerLED = String.format("%d %d %d %d", center.RED, center.GREEN, center.BLUE, center.BRIGHTNESS);
        String rightLED = String.format("%d %d %d %d", right.RED, right.GREEN, right.BLUE, right.BRIGHTNESS);

        pairList.clear();
        pairList.add(new Pair<>("LOG INTERVAL", String.valueOf(logInterval)));
        pairList.add(new Pair<>("RTC", getrtc));
        pairList.add(new Pair<>("MCU VER", mcuver));
        pairList.add(new Pair<>("FPGA VER", fpgaver));
        pairList.add(new Pair<>("GPIO IN1", adc_gpio_in1));
        pairList.add(new Pair<>("GPIO IN2", adc_gpio_in2));
        pairList.add(new Pair<>("GPIO IN3", adc_gpio_in3));
        pairList.add(new Pair<>("GPIO IN4", adc_gpio_in4));
        pairList.add(new Pair<>("GPIO IN5", adc_gpio_in5));
        pairList.add(new Pair<>("GPIO IN6", adc_gpio_in6));
        pairList.add(new Pair<>("GPIO IN7", adc_gpio_in7));
        pairList.add(new Pair<>("POWER IN", adc_power_in));
        pairList.add(new Pair<>("POWER VCAP", adc_power_cap));
        pairList.add(new Pair<>("RTC BATTERY STATUS", rtc_battery));
        pairList.add(new Pair<>("GPIO 917 VALUE", gpio917Value));
        pairList.add(new Pair<>("POWER ON REASON", power_on_reason));
        pairList.add(new Pair<>("SCALING CPU FREQ", scaling_cur_freq));
        pairList.add(new Pair<>("MCU TEMP", celsius));
        pairList.add(new Pair<>("THERMAL ZONES", thermalZone));
        pairList.add(new Pair<>("ACCELEROMETER", accelerometer));
        pairList.add(new Pair<>("CABLE TYPE", adc_cable_type));
        pairList.add(new Pair<>("DIG RTC CAL REG", dig_rtc_cal_reg));
        pairList.add(new Pair<>("ANA RTC CAL REG", ana_rtc_cal_reg));
        pairList.add(new Pair<>("BRIGHTNESS", brightness));
        leftLEDVal = new int[]{pairList.size(), left.getColorValue()};
        pairList.add(new Pair<>("LEFT LED", leftLED));
        centerLEDVal = new int[]{pairList.size(), center.getColorValue()};
        pairList.add(new Pair<>("CENTER LED", centerLED));
        rightLEDVal = new int[]{pairList.size(), right.getColorValue()};
        pairList.add(new Pair<>("RIGHT LED", rightLED));

    }

    private void requestGPIO917(){

        File gpio917File = new File("/sys/class/gpio/gpio917/value");

        // Write to file to request info. Check and see if file already exists.
        if(!gpio917File.exists()){
            try{
                File file = new File("/sys/class/gpio/export");

                bw = new BufferedWriter(new FileWriter(file));
                bw.write("917");

                bw.close();
            }catch(Exception e){
                Log.e(TAG, e.toString());
                gpio917Value = "Error";
            }
        }


        // Read value from file
        try{
            File file = new File("/sys/class/gpio/gpio917/value");

            br = new BufferedReader(new FileReader(file));
            gpio917Value = br.readLine();

            br.close();

        }catch(Exception e){
            Log.e(TAG, e.toString());
            gpio917Value = "Error";
        }



    }

    // Gets the scaling_cur_freq for the four cores. Extended code to check for bugs from indiviual files.
    // Will return -1 for the core if it has problems reading the file. Will return 0 if file doesn't exist.
    private void getCoreFrequencies() {

        try{

            File file = new File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");

            if(!file.exists()){
                cpu0 = 0;

            }else{
                br = new BufferedReader(new FileReader(file));
                cpu0 = Integer.valueOf(br.readLine());
                cpu0 /= 1000;
            }

            br.close();
        }catch(Exception e){
            Log.e(TAG, e.toString());
            cpu0 = -1;
        }

        try{

            File file = new File("/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq");

            if(!file.exists()){
                cpu1 = 0;

            }else{
                br = new BufferedReader(new FileReader(file));
                cpu1 = Integer.valueOf(br.readLine());
                cpu1 /= 1000;
            }

            br.close();
        }catch(Exception e){
            Log.e(TAG, e.toString());
            cpu1 = -1;
        }

        try{

            File file = new File("/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq");

            if(!file.exists()){
                cpu2 = 0;

            }else{
                br = new BufferedReader(new FileReader(file));
                cpu2 = Integer.valueOf(br.readLine());
                cpu2 /= 1000;
            }

            br.close();
        }catch(Exception e){
            Log.e(TAG, e.toString());
            cpu2 = -1;
        }

        try{

            File file = new File("/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq");

            if(!file.exists()){
                cpu3 = 0;

            }else{
                br = new BufferedReader(new FileReader(file));
                cpu3 = Integer.valueOf(br.readLine());
                cpu3 /= 1000;
            }

            br.close();
        }catch(Exception e){
            Log.e(TAG, e.toString());
            cpu3 = -1;
        }

        scaling_cur_freq = cpu0 + ", " + cpu1 + ", " + cpu2 + ", " + cpu3;
    }

    private void getThermalZoneTemps() {
        try {
            br = new BufferedReader(new FileReader("/sys/devices/virtual/thermal/thermal_zone0/temp"));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return pairList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /**
     * create a new TextView for each item referenced by the Adapter
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextHolder holder = new TextHolder();
        View rowView = ((LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.mcu_item_list, null);
        holder.title = (TextView) rowView.findViewById(R.id.textItem);
        holder.subitem = (TextView) rowView.findViewById(R.id.textSubItem);

        holder.title.setText(pairList.get(position).getLeft());
        holder.subitem.setText(pairList.get(position).getRight());
        if (position == leftLEDVal[0]) {
            rowView.setBackgroundColor(leftLEDVal[1]);
        } else if (position == centerLEDVal[0]) {
            rowView.setBackgroundColor(centerLEDVal[1]);
        } else if (position == rightLEDVal[0]) {
            rowView.setBackgroundColor(rightLEDVal[1]);
        }

        int c = ((ColorDrawable) rowView.getBackground()).getColor();
        if (isBrightColor(c)) {
            holder.title.setTextColor(Color.BLACK);
            holder.subitem.setTextColor(Color.BLACK);
        } else {
            holder.title.setTextColor(Color.WHITE);
            holder.subitem.setTextColor(Color.WHITE);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(pairList.get(position).getLeft()){
                    case("RIGHT LED"):
                        checkSetLedColor(0);
                        break;
                    case("CENTER LED"):
                        checkSetLedColor(1);
                        break;
                    case("LEFT LED"):
                        checkSetLedColor(2);
                        break;
                    case("BRIGHTNESS"):
                        checkSetBrightness();
                        break;
                }

                populateMctlTable();
                notifyDataSetChanged();
                Toast.makeText(context, pairList.get(position).getLeft() + " Data Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        return rowView;
    }

    //Keeps the same colors and adjusts the brightness of the LED. If the brightness is none of those values it will go to 255.
    //If the brightness of the three LEDs is not equal, it will set them all to 127 brightness.
    private void checkSetBrightness() {
        if(mc.get_led_status(0).BRIGHTNESS == mc.get_led_status(1).BRIGHTNESS && mc.get_led_status(1).BRIGHTNESS == mc.get_led_status(2).BRIGHTNESS){
            for (int i = 0; i < 3; i++) {
                int red = mc.get_led_status(i).RED;
                int green = mc.get_led_status(i).GREEN;
                int blue = mc.get_led_status(i).BLUE;

                switch(mc.get_led_status(i).BRIGHTNESS){
                    case(255):
                        mc.set_led_status(i, 191, Color.argb(255, red, green, blue));
                        break;
                    case(191):
                        mc.set_led_status(i, 127, Color.argb(255, red, green, blue));
                        break;
                    case(127):
                        mc.set_led_status(i, 64, Color.argb(255, red, green, blue));
                        break;
                    case(64):
                        mc.set_led_status(i, 0, Color.argb(255, red, green, blue));
                        break;
                    case(0):
                        mc.set_led_status(i, 255, Color.argb(255, red, green, blue));
                        break;
                    default:
                        mc.set_led_status(i, 255, Color.argb(255, red, green, blue));
                        break;
                }
            }
        }else{
            for (int i = 0; i < 3; i++) {
                int red = mc.get_led_status(i).RED;
                int green = mc.get_led_status(i).GREEN;
                int blue = mc.get_led_status(i).BLUE;

                mc.set_led_status(i, 127, Color.argb(255, red, green, blue));
            }
        }

    }

    //Rotates LED thru colors RED, GREEN, and then BLUE. If it is none of those colors it will change to RED. Brightness is not changed.
    private void checkSetLedColor(int led_num) {
        int brightness = mc.get_led_status(led_num).BRIGHTNESS;

        if (mc.get_led_status(led_num).RED == 255) {
            mc.set_led_status(led_num, brightness, Color.argb(255, 0, 255, 0));
        } else if (mc.get_led_status(led_num).GREEN == 255) {
            mc.set_led_status(led_num, brightness, Color.argb(255, 0, 0, 255));
        } else if (mc.get_led_status(led_num).BLUE == 255) {
            mc.set_led_status(led_num, brightness, Color.argb(255, 255, 0, 0));
        } else {
            mc.set_led_status(led_num, brightness, Color.argb(255, 255, 0, 0));
        }
    }

    public int getLogInterval() {
        return logInterval;
    }

    public void increaseLogInterval() {
        logInterval++;
    }

    public void clearLogInterval() {
        logInterval = 0;
    }


    public class TextHolder {
        TextView title;
        TextView subitem;
    }

    public static boolean isBrightColor(int color) {
        if (android.R.color.transparent == color)
            return true;

        boolean rtnValue = false;

        int[] rgb = {Color.red(color), Color.green(color), Color.blue(color)};

        // Brightness math based on:
        //   http://www.nbdtech.com/Blog/archive/2008/04/27/Calculating-the-Perceived-Brightness-of-a-Color.aspx
        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
                * rgb[1] * .691 + rgb[2] * rgb[2] * .068);

        // color is light
        if (brightness >= 200) {
            rtnValue = true;
        }

        return rtnValue;
    }
}
