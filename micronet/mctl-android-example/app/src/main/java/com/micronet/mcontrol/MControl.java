package com.micronet.mcontrol;

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
    private native static void jniSetLEDValue(int led, int rgb);
    private native static int jniGetPowerOnThresholdCfg();
    private native static int jniSetPowerOnThresholdCfg();
    private native static int jniGetPowerOnReason();
    private native static int jniSetDevicePowerOff();
    private native static String jniGetRTCDateTime();
    private native static void jniSetRTCDateTime(long time);
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
        try {
            return Integer.toHexString(jniGetFPGAVersion());
        } catch(Exception e) {
            return "error=" + e.getMessage();
        }
    }

    public int get_adc_or_gpi_voltage(int gpi_num) {
        if(DBG) return 1234;
        try {
            return jniGetADCorGPIVoltage(gpi_num);
        } catch(Exception e) {
            return -1;
        }
    }

    public String get_rtc_date_time() {
        if(DBG) {
            SimpleDateFormat formatter = new SimpleDateFormat("hh.mm.ss");
            Date today = Calendar.getInstance().getTime();
            return formatter.format(today);
        }

        try {
            return jniGetRTCDateTime();
        } catch(Exception e) {
            return "error=" + e.getMessage();
        }
    }

    /**
     * get the digital and analog rtc cal registers
     * @return an int array of length two containing digital and analog rtc cal, respectively.
     * A value of -1 indicates that value is invalid.
     */
    public int[] get_rtc_cal_reg() {
        int[] arr = {-1, -1};

        if (DBG) { return arr; }

        try {
            arr = jniGetRTCCalReg();
            if (arr.length == 1) {
                arr = new int[]{arr[0], -1};
            } else if (arr.length != 2) {
                arr = new int[]{-1, -1};
            }
        } catch (Exception e) { }

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
        try {
            int[] arr = jniGetLEDStatus(led_num);
            led.RED = arr[0];
            led.GREEN = arr[1];
            led.BLUE = arr[2];
            led.BRIGHTNESS = arr[3];
        } catch (Exception e) { }

        return led;
    }

    /**
     * Throws exception
     *
     * @param led
     * @param rgb
     */
    public void set_led_status(int led, int rgb) {
        try {
            jniSetLEDValue(led, rgb);
        } catch (Exception e) { throw e; }
    }

    public void get_power_on_threshold_cfg() { }
    public void get_power_on_reason() { }
    public void get_rtc_reg_dbg() { }
    public void set_power_on_threshold_cfg() {};
    public void set_device_power_off() {};
    public void set_rtc_date_time() {};
    public void set_rtc_cal_reg() {};
    public void set_rtc_reg_dbg() {};

//    int get_mcu_version(int * fd, uint8_t * fw_version, size_t size);
//    int get_fpga_version(int * fd, uint32_t * fpga_version, size_t size);
//    int get_adc_or_gpi_voltage(int * fd, uint8_t gpi_num, uint32_t * gpi_voltage, size_t size);
//    int get_led_status(int * fd, uint8_t led_num, uint8_t *brightness, uint8_t *red, uint8_t *green, uint8_t *blue);
//    int set_led_status(int * fd, uint8_t led_num, uint8_t brightness, uint8_t red, uint8_t green, uint8_t blue);
//    int get_power_on_threshold_cfg(int * fd, uint16_t *wiggle_count, uint16_t *wig_cnt_sample_period, uint16_t *ignition_threshold);
//    int set_power_on_threshold_cfg(int * fd, uint16_t wiggle_count, uint16_t wig_cnt_sample_period, uint16_t ignition_threshold);
//    int get_power_on_reason(int * fd, uint8_t *power_on_reason);
//    int set_device_power_off(int * fd, uint8_t wait_time);
//    int get_rtc_date_time(int * fd, char * dt_str);
//    int set_rtc_date_time(int * fd, char * dt_str);
//    int get_rtc_cal_reg(int * fd, uint8_t * dig_cal, uint8_t * anal_cal);
//    int set_rtc_cal_reg(int * fd, uint8_t dig_cal, uint8_t analog_cal);
//    int get_rtc_reg_dbg(int * fd, uint8_t address, uint8_t * data);
//    int set_rtc_reg_dbg(int * fd, uint8_t address, uint8_t data);
//    bool check_rtc_battery(int * fd);

}
