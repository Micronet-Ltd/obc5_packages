package com.micronet.mcontrol.interfaces;

/**
 * Created by brigham.diaz on 5/25/2016.
 */
public interface ADCInterface {
    int kADC_ANALOG_IN1 = 0x00; // (Ignition)
    int kADC_GPIO_IN1= 0x01;
    int kADC_GPIO_IN2= 0x02;
    int kADC_GPIO_IN3= 0x03;
    int kADC_GPIO_IN4= 0x04;
    int kADC_GPIO_IN5= 0x05;
    int kADC_GPIO_IN6= 0x06;
    int kADC_GPIO_IN7= 0x07;
    int kADC_POWER_IN = 0x08; // (Battery Voltage)
    int kADC_POWER_VCAP = 0x09; // (Super cap)
    int kADC_TEMPERATURE = 0x0a; // (Temp sensor)
    int kADC_CABLE_TYPE = 0xb;

    int getValue();
}
