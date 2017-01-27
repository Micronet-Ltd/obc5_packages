package com.micronet.canbus;

/**
 * Created by brigham.diaz on 1/27/2017.
 */

public class FlexCANCanbusInterfaceBridge {
    /**
     * Creates new Canbus interface (up).
     */
    public void create() {
        createInterface();
    }

    static {
        System.loadLibrary("canbus");
    }

    private native int createInterface();
}
