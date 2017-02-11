package com.micronet.canbus;

/**
 * Created by brigham.diaz on 1/27/2017.
 */

public class FlexCANCanbusInterfaceBridge {
    private boolean listeningModeEnable;
    private boolean termination;
    private int bitrate;
    /**
     * Creates new Canbus interface (up).
     */
    private static final String TAG = "CanbusSocket";

    /**
     *
     */
    public void create() {
        createInterface(true,250000,false);
    }

    /**
     * Creates new CAN interface in the specified mode (QBridge support only).
     * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
     *                            This mode may be used to analyze a CANbus without disturbing the bus.
     *                            false, turns on the CAN module's transmitter and receiver.
     *                            This mode doesn't affect the J1708 transmission line.
     */
    public void create(boolean listeningModeEnable) {
        createInterface(listeningModeEnable, 250000, false);
    }

    /**
     *
     * @param listeningModeEnable
     * @param bitrate
     * @param termination
     */
    public void create(boolean listeningModeEnable, int bitrate, boolean termination) {
        this.listeningModeEnable = listeningModeEnable;
        this.termination = termination;
        this.bitrate = bitrate;
        createInterface(listeningModeEnable, bitrate, termination);
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
    public void setBitrate(int bitrate) {
        createInterface(this.listeningModeEnable, bitrate, this.termination);
    }

    /**
     * Changing termination will result in the CAN module being re-opened.
     * @param termination
     */
    public void setCANTermination(boolean termination) {
        createInterface(this.listeningModeEnable, this.bitrate, termination);
    }


    /**
     * Sets filters in Canbus hardware controller.
     */
  /*  public void setFilters(CanbusHardwareFilter[] hardwareFilters) {
        setHardwareFilter(hardwareFilters);
    }*/

    /**
     *	Creates new socket on Canbus interface.
     */
    public CanbusSocket createSocket(){
        // TODO: pass fd to socket
        return null;//new QBridgeCanbusSocket(fd);
    }

    /**
     * {@inheritDoc}
     */
    public void setListeningMode(boolean listeningModeEnable) {
        createInterface(listeningModeEnable, this.bitrate, this.termination);
    }

    public boolean checkJ1708Support()
    {
        return true;
    }
   // private native int setHardwareFilter(CanbusHardwareFilter[] hardwareFilters);
    private native int createInterface(boolean listeningModeEnable, int bitrate, boolean termination);
    private native int removeInterface();
    private native int setInterfaceBitrate(int bitrate);
    private native int enableListeningMode(boolean enable);
    private native int setTermination(boolean enabled);

    static
    {
        System.loadLibrary("canbus");
    }
}
