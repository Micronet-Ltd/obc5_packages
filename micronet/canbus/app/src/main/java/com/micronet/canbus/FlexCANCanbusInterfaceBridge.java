package com.micronet.canbus;

/**
 * Created by brigham.diaz on 1/27/2017.
 */

public class FlexCANCanbusInterfaceBridge implements ICanbusInterfaceBridge {
    private boolean listeningModeEnable;
    private boolean termination;
    private int bitrate;
    private static final String TAG = "CanbusSocket";
    private int fd=0;

    /**
     * Creates new Canbus interface (up).
     */
    public void create(CanbusHardwareFilter[] hardwareFilters) {
        createInterface(false,250000,true,hardwareFilters); //TODO: changed listening mode to false and termination to true because of a firmware bug
    }

    /**
     * Creates new CAN interface in the specified mode (QBridge support only).
     * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
     *                            This mode may be used to analyze a CANbus without disturbing the bus.
     *                            false, turns on the CAN module's transmitter and receiver.
     *                            This mode doesn't affect the J1708 transmission line.
     * @param hardwareFilters Filters masks and filter types used for filtering CAN packets.
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
     *	Creates new socket on Canbus interface.
     */
    public CanbusSocket createSocket(){
        return new FlexCANCanbusSocket(fd);
    }

    /**
     * {@inheritDoc}
     */
    public void setListeningMode(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters){
        createInterface(listeningModeEnable, this.bitrate, this.termination,hardwareFilters );
    }

    private native int createInterface(boolean listeningModeEnable, int bitrate, boolean termination,CanbusHardwareFilter[] hardwareFilters );
    private native int removeInterface();


    static
    {
        System.loadLibrary("canbus");
    }
}
