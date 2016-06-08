package com.micronet.mcontrol;

/**
 * Created by brigham.diaz on 5/25/2016.
 */
public interface LEDInterface {
    int RIGHT = 0;
    int CENTER = 1;
    int LEFT = 2;
    void setValue(int rgb);
    int getColorValue();
}
