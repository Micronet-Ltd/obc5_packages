package com.micronet.canbus;

/**
 * Created by brigham.diaz on 1/27/2017.
 */

public class FlexCANCanbusInterfaceBridge implements ICanbusInterfaceBridge {
    private boolean listeningModeEnable;
    private boolean termination;
    private int bitrate;
    private CanbusHardwareFilter[] canbusHardwareFilters;
    /**
     * Creates new Canbus interface (up).
     */
    private static final String TAG = "CanbusSocket";
    private int fd=0;
    /**
     *
     */
    public void create(CanbusHardwareFilter[] hardwareFilters) {
        createInterface(false,250000,true,hardwareFilters); //TO-DO: changed listening mode to false and termination to true because of a bug
    }

    /**
     * Creates new CAN interface in the specified mode (QBridge support only).
     * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
     *                            This mode may be used to analyze a CANbus without disturbing the bus.
     *                            false, turns on the CAN module's transmitter and receiver.
     *                            This mode doesn't affect the J1708 transmission line.
     */
    public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters) {
        createInterface(listeningModeEnable, 250000, false,hardwareFilters);
    }

    /**
     *
     * @param listeningModeEnable
     * @param bitrate
     * @param termination
     */
    public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters) {
        this.listeningModeEnable = listeningModeEnable;
        this.termination = termination;
        this.bitrate = bitrate;
        this.
        createInterface(listeningModeEnable, bitrate, termination, hardwareFilters);
    }


    /**
     * Removes Canbus interface (down).
     */
    public void remove() {
        removeInterface();
    }

    /**
     * Sets interface bitrate.
     * Interface must be removed first!
     */
    public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters) {
        createInterface(this.listeningModeEnable, bitrate, this.termination, hardwareFilters);
    }

    /**
     * Changing termination will result in the CAN module being re-opened.
     * @param termination
     */
    public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters) {
        createInterface(this.listeningModeEnable, this.bitrate, termination,hardwareFilters );
    }
    /**
     * Sets filters in Canbus hardware controller.
     */
   /* public void setFilters(CanbusHardwareFilter[] hardwareFilters) {
        setHardwareFilter(hardwareFilters);
    }*/

    /**
     *	Creates new socket on Canbus interface.
     */
    public CanbusSocket createSocket(){
        return new FlexCANCanbusSocket(fd);//new QBridgeCanbusSocket(fd);
    }

    /**
     * {@inheritDoc}
     */
    public void setListeningMode(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters){
        createInterface(listeningModeEnable, this.bitrate, this.termination,hardwareFilters );
    }

    /* public boolean checkJ1708Support() {return true;} */
    private native int createInterface(boolean listeningModeEnable, int bitrate, boolean termination,CanbusHardwareFilter[] hardwareFilters );
    private native int removeInterface();


    static
    {
        System.loadLibrary("canbus");
    }
}
