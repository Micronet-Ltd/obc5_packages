package com.micronet.mcontrol;

/**
 * Created by brigham.diaz on 5/25/2016.
 */
public enum ADCs implements ADCInterface {
    ANALOG_IN1(ADCInterface.kADC_ANALOG_IN1),
    ADC_GPIO_IN1(ADCInterface.kADC_GPIO_IN1),
    ADC_GPIO_IN2(ADCInterface.kADC_GPIO_IN2),
    ADC_GPIO_IN3(ADCInterface.kADC_GPIO_IN3),
    ADC_GPIO_IN4(ADCInterface.kADC_GPIO_IN4),
    ADC_GPIO_IN5(ADCInterface.kADC_GPIO_IN5),
    ADC_GPIO_IN6(ADCInterface.kADC_GPIO_IN6),
    ADC_GPIO_IN7(ADCInterface.kADC_GPIO_IN7),
    ADC_POWER_IN(ADCInterface.kADC_POWER_IN),
    ADC_POWER_VCAP(ADCInterface.kADC_POWER_VCAP),
    ADC_TEMPERATURE(ADCInterface.kADC_TEMPERATURE),
    ADC_CABLE_TYPE(ADCInterface.kADC_CABLE_TYPE);

    public final int gpi_num;

    ADCs(int gpi_num) {
        this.gpi_num= gpi_num;
    }

    @Override
    public int getValue() {
        MControl mc = new MControl();
        return mc.get_adc_or_gpi_voltage(gpi_num);
    }
}
