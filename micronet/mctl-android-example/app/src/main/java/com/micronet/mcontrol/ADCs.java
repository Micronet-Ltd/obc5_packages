package com.micronet.mcontrol;

/**
 * Created by brigham.diaz on 5/25/2016.
 */
public enum ADCs implements ADC {
    ANALOG_IN1(ADC.kADC_ANALOG_IN1),
    ADC_GPIO_IN1(ADC.kADC_GPIO_IN1),
    ADC_GPIO_IN2(ADC.kADC_GPIO_IN2),
    ADC_GPIO_IN3(ADC.kADC_GPIO_IN3),
    ADC_GPIO_IN4(ADC.kADC_GPIO_IN4),
    ADC_GPIO_IN5(ADC.kADC_GPIO_IN5),
    ADC_GPIO_IN6(ADC.kADC_GPIO_IN6),
    ADC_GPIO_IN7(ADC.kADC_GPIO_IN7),
    ADC_POWER_IN(ADC.kADC_POWER_IN),
    ADC_POWER_VCAP(ADC.kADC_POWER_VCAP),
    ADC_TEMPERATURE(ADC.kADC_TEMPERATURE),
    ADC_CABLE_TYPE(ADC.kADC_CABLE_TYPE);

    public final int gpi_num;

    private ADCs(int gpi_num) {
        this.gpi_num= gpi_num;
    }

    @Override
    public int getValue() {
        MControl mc = new MControl();
        return mc.get_adc_or_gpi_voltage(gpi_num);
    }
}
