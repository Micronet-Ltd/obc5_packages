package com.micronet.mcontrol;

import android.graphics.Color;

/**
 * Created by brigham.diaz on 5/25/2016.
 */
public class LEDs implements LEDInterface {
    public int RED = 0;
    public int GREEN = 0;
    public int BLUE = 0;
    public int BRIGHTNESS = 0;

    public final int led;

    public LEDs(int led) {
        this.led = led;
    }

    @Override
    public void setValue(int rgb) {
        MControl mc = new MControl();
        mc.set_led_status(led, rgb);
    }

    @Override
    public int getColorValue() {
        return Color.argb(0xFF, RED, GREEN, BLUE);
    }
}
