package com.micronet.mcontrol;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brigham.diaz on 5/24/2016.
 */
public class MControlTextAdapter extends BaseAdapter {

    private Context context;
    private static final String TAG = "MControlTextAdapter";
    private static LayoutInflater inflater = null;
    final String DEGREE  = "\u00b0";
    final String BRIGHTNESS = "\u2600";
    final MControl mc;
    private int[] leftLEDVal = new int[]{-1, 0xFFFFFF};
    private int[] centerLEDVal = new int[]{-1, 0xFFFFFF};
    private int[] rightLEDVal = new int[]{-1, 0xFFFFFF};


    private List<Pair<String,String>> pairList = new ArrayList<Pair<String,String>>();

    public MControlTextAdapter(Context context) {
        this.context = context;
        // initialize mctl
        mc = new MControl();
        populateMctlTable();
    }

    public List<Pair<String,String>> getPairList() {
        return pairList;
    }

    public void populateMctlTable() {

        String rtc = mc.get_rtc_date_time();
        String mcuver = mc.get_mcu_version();
        String fpgaver = mc.get_fpga_version();
        String adc_gpio_in1 = ADCs.ADC_GPIO_IN1.getValue()+ " mv";
        String adc_gpio_in2 = ADCs.ADC_GPIO_IN2.getValue()+ " mv";
        String adc_gpio_in3 = ADCs.ADC_GPIO_IN3.getValue()+ " mv";
        String adc_gpio_in4 = ADCs.ADC_GPIO_IN4.getValue()+ " mv";
        String adc_gpio_in5 = ADCs.ADC_GPIO_IN5.getValue()+ " mv";
        String adc_gpio_in6 = ADCs.ADC_GPIO_IN6.getValue()+ " mv";
        String adc_gpio_in7 = ADCs.ADC_GPIO_IN7.getValue()+ " mv";
        String adc_power_in = ADCs.ADC_POWER_IN.getValue() + " mv";
        String adc_power_cap = ADCs.ADC_POWER_VCAP.getValue() + " mv";
        String celsius = ((ADCs.ADC_TEMPERATURE.getValue() - 500.0f) / 10) + " " + DEGREE +"C";
        String adc_cable_type = ADCs.ADC_CABLE_TYPE.getValue() + " mv";
        int[] rtc_cal = mc.get_rtc_cal_reg();
        String dig_rtc_cal_reg = String.valueOf(rtc_cal[0]);
        String ana_rtc_cal_reg = String.valueOf(rtc_cal[1]);

        LEDs left = mc.get_led_status(LEDInterface.LEFT);
        LEDs center = mc.get_led_status(LEDInterface.CENTER);
        LEDs right = mc.get_led_status(LEDInterface.RIGHT);
        String leftLED = String.format("RGB"+BRIGHTNESS+"(%d, %d, %d, %d)", left.RED, left.GREEN, left.BLUE, left.BRIGHTNESS);
        String centerLED = String.format("RGB"+BRIGHTNESS+"(%d, %d, %d, %d)", center.RED, center.GREEN, center.BLUE, center.BRIGHTNESS);
        String rightLED = String.format("RGB"+BRIGHTNESS+"(%d, %d, %d, %d)", right.RED, right.GREEN, right.BLUE, center.BRIGHTNESS);

        pairList.clear();
        pairList.add(new Pair<String, String>("RTC Date/Time", rtc));
        pairList.add(new Pair<String, String>("MCU VER", mcuver));
        pairList.add(new Pair<String, String>("FPGA VER", fpgaver));
        pairList.add(new Pair<String, String>("GPIO IN1", adc_gpio_in1));
        pairList.add(new Pair<String, String>("GPIO IN2", adc_gpio_in2));
        pairList.add(new Pair<String, String>("GPIO IN3", adc_gpio_in3));
        pairList.add(new Pair<String, String>("GPIO IN4", adc_gpio_in4));
        pairList.add(new Pair<String, String>("GPIO IN5", adc_gpio_in5));
        pairList.add(new Pair<String, String>("GPIO IN6", adc_gpio_in6));
        pairList.add(new Pair<String, String>("GPIO IN7", adc_gpio_in7));
        pairList.add(new Pair<String, String>("POWER IN", adc_power_in));
        pairList.add(new Pair<String, String>("POWER VCAP", adc_power_cap));
        pairList.add(new Pair<String, String>("TEMPERATURE", celsius));
        pairList.add(new Pair<String, String>("CABLE TYPE", adc_cable_type));
        pairList.add(new Pair<String, String>("DIG RTC CAL REG", dig_rtc_cal_reg));
        pairList.add(new Pair<String, String>("ANA RTC CAL REG", ana_rtc_cal_reg));
        leftLEDVal = new int[] {pairList.size(), left.getColorValue()};
        pairList.add(new Pair<String, String>("LEFT LED", leftLED));
        centerLEDVal = new int[] {pairList.size(), center.getColorValue()};
        pairList.add(new Pair<String, String>("CENTER LED", centerLED));
        rightLEDVal = new int[] {pairList.size(), right.getColorValue()};
        pairList.add(new Pair<String, String>("RIGHT LED", rightLED));

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
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextHolder holder = new TextHolder();
        View rowView = ((LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.mcu_item_list, null);
        holder.title = (TextView) rowView.findViewById(R.id.textItem);
        holder.subitem = (TextView) rowView.findViewById(R.id.textSubItem);

        holder.title.setText(pairList.get(position).getLeft());
        holder.subitem.setText(pairList.get(position).getRight());
        if(position == leftLEDVal[0]) {
            rowView.setBackgroundColor(leftLEDVal[1]);
        } else if(position == centerLEDVal[0]) {
            rowView.setBackgroundColor(centerLEDVal[1]);
        } else if(position == rightLEDVal[0]) {
            rowView.setBackgroundColor(rightLEDVal[1]);
        }

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateMctlTable();
                notifyDataSetChanged();
                Toast.makeText(context, pairList.get(position).getLeft() + " Data Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        return rowView;
    }

    public class TextHolder
    {
        TextView title;
        TextView subitem;
    }
}
