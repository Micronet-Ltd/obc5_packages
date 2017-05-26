package com.micronet.canbus;

final class FlexCANCanbusInterfaceBridge implements ICanbusInterfaceBridge {
    private boolean listeningModeEnable;
    private boolean termination;
    private int bitrate;
    private static final String TAG = "CanbusSocket";
    private int fd=0;
    private int fd_can_port1=0;
    private int fd_can_port2=0;
    private int fd_j1708=0;

    /**
     * Creates new Canbus interface (up).
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     */
    public void create(CanbusHardwareFilter[] hardwareFilters, int portNumber) {
        createInterface(false,250000,true,hardwareFilters,portNumber,null);
    }



    /**
     * Creates new Canbus interface (up).
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     */
    public void create(CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControls) {
        createInterface(false,250000,true,hardwareFilters,portNumber,flowControls);
    }


    /**
     * Creates new CAN interface in the specified mode
     * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
     *                            This mode may be used to analyze a CANbus without disturbing the bus.
     *                            false, turns on the CAN module's transmitter and receiver.
     *                            This mode doesn't affect the J1708 transmission line.
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     */
    public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters,int portNumber) {
        createInterface(listeningModeEnable, 250000, true,hardwareFilters,portNumber,null);
    }



    /**
     * Creates new CAN interface in the specified mode with a default Baud rate [Baud rate - 2500000]
     * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
     *                            This mode may be used to analyze a CANbus without disturbing the bus.
     *                            false, turns on the CAN module's transmitter and receiver.
     *                            This mode doesn't affect the J1708 transmission line.
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     */
    public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls) {
        createInterface(listeningModeEnable, 250000, true,hardwareFilters,portNumber,flowControls);
    }


    /**
     *
     * @param listeningModeEnable
     * @param bitrate
     * @param termination
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     *
     */
    public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber) {
        this.listeningModeEnable = listeningModeEnable;
        this.termination = termination;
        this.bitrate = bitrate;
        this.
        createInterface(listeningModeEnable, bitrate, termination, hardwareFilters,portNumber,null);
    }



    /**
     *
     * @param listeningModeEnable
     * @param bitrate
     * @param termination
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     *@param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     *
     */
    public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls) {
        this.listeningModeEnable = listeningModeEnable;
        this.termination = termination;
        this.bitrate = bitrate;
        createInterface(listeningModeEnable, bitrate, termination, hardwareFilters,portNumber,flowControls);
    }

    /**
     * Removes Canbus interface (down).
     */
    public void remove() {
        //TODO: Check, which interface is removed? CAN1? CAN2?
        removeInterface();
    }



    /**
     * Sets interface bitrate by creating an interface.
     * Interface must be removed first!
     * @param bitrate
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     */
    public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters, int portNumber) {
        createInterface(this.listeningModeEnable, bitrate, this.termination, hardwareFilters, portNumber,null);
    }



    /**
     * Sets interface bitrate by creating an interface.
     * Interface must be removed first!
     * @param bitrate
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708/
     * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     */
    public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls) {
        createInterface(this.listeningModeEnable, bitrate, this.termination, hardwareFilters, portNumber,flowControls);
    }



    /**
     * Changing termination will result in the CAN module being re-opened.
     * @param termination
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     */
    public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber) {
        createInterface(this.listeningModeEnable, this.bitrate, termination,hardwareFilters, portNumber,null);
    }



    /**
     * Changing termination will result in the CAN module being re-opened.
     * @param termination
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *                   4, J1708
     * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     */
    public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls) {
        createInterface(this.listeningModeEnable, this.bitrate, termination,hardwareFilters, portNumber,flowControls);
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
    public void setListeningMode(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters, int portNumber){
        createInterface(listeningModeEnable, this.bitrate, this.termination,hardwareFilters, portNumber,null);
    }


    /**
     * {@inheritDoc}
     */
    public void setListeningMode(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControl){
        createInterface(listeningModeEnable, this.bitrate, this.termination,hardwareFilters, portNumber,flowControl);
    }


    private native int createInterface(boolean listeningModeEnable, int bitrate, boolean termination,CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControl);
    private native int removeInterface();





    static
    {
        System.loadLibrary("canbus");
    }
}
