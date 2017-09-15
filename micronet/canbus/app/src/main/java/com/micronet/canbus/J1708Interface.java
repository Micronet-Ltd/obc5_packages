package com.micronet.canbus;

/**
 * Represents the J1708 network interface.
 * And exposes operations like CreateInterface, Remove Interface etc. related to Vehicle bus driver level
 */

public class J1708Interface {

    private static final String TAG = "J1708Interface - CanbusSocketListener";

    IVehicleInterfaceBridge impl;

    public J1708Interface()
    {
        switch(J1708Interface.getImplId())
        {
            case 2: impl = new FlexCANVehicleInterfaceBridge(); break;
        }
    }
    //TODO: CreateInterface()

    /**
     * Creates an Interface for J1708 Communication
     * */
    public int createJ1708() {
        return impl.createJ1708();
    }

    /**
     * Removes the Interface for J1708 Communication
     * NOTE: Removing the J1708 interface will also disable CAN1.
     * */
    public int removeJ1708() {
       return impl.removeJ1708();
    }


    /**
     *	Creates new socket on J1708 interface.
     */
    public J1708Socket createSocketJ1708(){
        return impl.createSocketJ1708();
    }

    private native static int getImplId();

    static
    {
        System.loadLibrary("canbus");
    }

}
