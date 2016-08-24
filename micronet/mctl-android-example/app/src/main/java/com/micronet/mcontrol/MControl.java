package com.micronet.mcontrol;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by brigham.diaz on 5/24/2016.
 */
public class MControl {

    private static boolean DBG = false;

    static {
        System.loadLibrary("mctl");
    }

    private native static String jniGetMCUVersion();
    private native static int jniGetFPGAVersion();
    private native static int jniGetADCorGPIVoltage(int gpi_num);
    private native static int[] jniGetLEDStatus(int led_num);
    private native static void jniSetLEDValue(int led, int brightness, int rgb);
    private native static int jniGetPowerOnThresholdCfg();
    private native static int jniSetPowerOnThresholdCfg();
    private native static int jniGetPowerOnReason();
    private native static int jniSetDevicePowerOff();
    private native static String jniGetRTCDateTime();
    private native static int jniSetRTCDateTime(String dateTime);
    private native static int[] jniGetRTCCalReg();
    private native static int jniSetRTCCalReg();
    private native static int jniGetRTCRegDBG();
    private native static int jniSetRTCRegDBG();
    private native static boolean jniCheckRTCBattery();

    public String get_mcu_version() {
        if(DBG) return "1234DBG";
        return jniGetMCUVersion();
    }

    public String get_fpga_version() {
        if(DBG) return "1234DBG";
        return Integer.toHexString(jniGetFPGAVersion());
    }

    public void set_led_status(int led, int brightness, int rgb) {
        jniSetLEDValue (led, brightness, rgb);
    }

    public int get_adc_or_gpi_voltage(int gpi_num) {
        if(DBG) return 1234;
        return jniGetADCorGPIVoltage(gpi_num);
    }

    public void set_device_power_off() {

    }

    public String get_rtc_date_time() {
        if(DBG) {
            SimpleDateFormat formatter = new SimpleDateFormat("hh.mm.ss");
            Date today = Calendar.getInstance().getTime();
            return formatter.format(today);
        }
        return jniGetRTCDateTime();
    }

    public int set_rtc_date_time(String dateTime) {
        return jniSetRTCDateTime(dateTime);
    }

    /**
     * get the digital and analog rtc cal registers
     * @return an int array of length two containing digital and analog rtc cal, respectively.
     * A value of -1 indicates that value is invalid.
     */
    public int[] get_rtc_cal_reg() {
        if(DBG) {
            return new int[] {-1,-1};
        }

        int[] arr = jniGetRTCCalReg();
        if(arr.length == 1) {
            arr = new int[] {arr[0], -1};
        } else if(arr.length != 2) {
            arr = new int[] {-1, -1};
        }

        return arr;
    }

    /**
     To get the LED status, the following command can be sent. Right LED is 0 and Center LED is 1. Brightness ranges from 0-255. Zero means the led is off. The RGB color code used is are standard RGB color codes defined at:
     http://www.rapidtables.com/web/color/RGB_Color.htm

     $ mctl api 0205<led num (Right LED = 0, Center LED = 1, Left LED = 2)>
     $ mctl api 020500
     get led num 0, brightness = 10, red = 255, green = 255, blue = 127 ret = 4

     */
    public LEDs get_led_status(int led_num) {
        LEDs led = new LEDs(led_num);

        if(DBG) {
            return led;
        }

        int[] arr = jniGetLEDStatus(led_num);
        led.RED = arr[0];
        led.GREEN = arr[1];
        led.BLUE = arr[2];
        led.BRIGHTNESS = arr[3];

        return led;
    }
}
