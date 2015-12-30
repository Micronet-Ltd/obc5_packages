package com.qrt.factory.auto;

import com.qrt.factory.R;
import com.qrt.factory.util.Utilities;

import android.content.Context;

import java.text.DecimalFormat;

/**
 * Created with IntelliJ IDEA. User: wangwenlong Date: 2012:08:10 Time: 15:37 To
 * change this template use File | Settings | File Templates.
 */
public class AutoBattery implements AutoTest {

    private static final String TAG = "Battery Test";

    private static final String VOLTAGE_NOW_FILE
            = "/sys/class/power_supply/battery/voltage_now";

    private static final String TEMPERATURE_FILE
            = "/sys/class/power_supply/battery/temperature";

    private static final String STATUS_FILE
            = "/sys/class/power_supply/battery/status";

    private static final String POWER_USB_FILE
            = "/sys/class/power_supply/usb/online";

    private static final String POWER_AC_FILE
            = "/sys/class/power_supply/ac/online";

    private Context mContext;

    @Override
    public void initialize(Context context) {

        mContext = context;
    }

    @Override
    public TestResult doingBackground() {

        TestResult testResult = new TestResult();

        String powerUsbValue = Utilities.getFileInfo(POWER_USB_FILE);
        String powerAcValue = Utilities.getFileInfo(POWER_AC_FILE);

        if ("1".equals(powerAcValue) || "1".equals(powerUsbValue)) {
            String status = getBatteryStatusInfo();
            boolean ret = pass(status);
            testResult.appendResult(
                    mContext.getString(R.string.battery_status) + status + "\n");
            testResult.appendResult(getBartteryVoltageInfo());
            testResult.appendResult(getBatteryTemperatureInfo());

            testResult.setPass(ret);
        }

        return testResult;
    }

    private String getBatteryTemperatureInfo() {
        String tmp = Utilities.getFileInfo(TEMPERATURE_FILE);
            return formateTemperature(tmp) + "\n";
    }

    private String formateTemperature(String tmp) {
        if (tmp == null) {
            return "";
        }
        float temperature = 0.1f * Float.valueOf(tmp);
        return mContext.getString(R.string.battery_temperature) + new DecimalFormat(
                "###.#").format(temperature) + "° \n";
    }

    private String getBartteryVoltageInfo() {
        String tmp = Utilities.getFileInfo(VOLTAGE_NOW_FILE);
        return formatVoltage(tmp);
    }

    private String formatVoltage(String tmp) {
        float voltage = Float.valueOf(tmp);
        if (voltage > 1000000) {
            voltage = voltage / 1000000;
        } else if (voltage > 1000) {
            voltage = voltage / 1000;
        }
        return mContext.getString(R.string.battery_voltage) + voltage + "V \n";
    }

    private String getBatteryStatusInfo() {
        return Utilities.getFileInfo(STATUS_FILE);
    }

    private boolean pass(String tmp) {
        return "Charging".equalsIgnoreCase(tmp) || "Full".equalsIgnoreCase(tmp);
    }
}
