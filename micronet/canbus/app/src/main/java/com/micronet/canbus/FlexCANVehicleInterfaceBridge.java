package com.micronet.canbus;

import android.util.Log;

final class FlexCANVehicleInterfaceBridge implements IVehicleInterfaceBridge {
    private boolean listeningModeEnable;
    private boolean termination;
    private int bitrate;
    private static final String TAG = "FlexCAN-VehicleI/FBr";
    private int fdCanPort1 = 0;
    private int fdCanPort2 = 0;
    private int fdJ1708 = 0;
//    private int fdJ1708Read = 0;
//    private int fdJ1708Write = 0;
    static int canPort1Number = 2;
    static int canPort2Number = 3;
//    static int j708ReadPortNumber = 4;
//    static int j708WritePortNumber = 5;

    /**
     * Creates new Canbus interface (up).
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
     */
    public void create(CanbusHardwareFilter[] hardwareFilters, int portNumber) throws CanbusException{
        int result = configureCanInterface(false,250000,true,hardwareFilters,portNumber,null);
        if (result < 0){
            throw new CanbusException("Error creating Interface", result);
        }
    }

    /**
     * Creates new Canbus interface (up).
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
     * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     */

    public void create(CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControls) throws CanbusException {
        int result = configureCanInterface(false,250000,true,hardwareFilters,portNumber,flowControls);
        if (result < 0){
            throw new CanbusException("Error creating Interface", result);
        }
    }


    /**
     * Creates new CAN interface in the specified mode
     * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
     *                            This mode may be used to analyze a CANbus without disturbing the bus.
     *                            false, turns on the CAN module's transmitter and receiver.
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
     */
    public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters,int portNumber) throws CanbusException {
        int result = configureCanInterface(listeningModeEnable, 250000, true,hardwareFilters,portNumber,null);
        if (result < 0){
            throw new CanbusException("Error creating Interface", result);
        }
    }



    /**
     * Creates new CAN interface in the specified mode with a default Baud rate [Baud rate - 2500000]
     * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
     *                            This mode may be used to analyze a CANbus without disturbing the bus.
     *                            false, turns on the CAN module's transmitter and receiver.
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     */
    public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls) throws CanbusException{
        int result = configureCanInterface(listeningModeEnable, 250000, true,hardwareFilters,portNumber,flowControls);
        if (result < 0){
            throw new CanbusException("Error creating Interface", result);
        }
    }


    /**
     *
     * @param listeningModeEnable
     * @param bitrate
     * @param termination
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
     */
    public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber) throws CanbusException{
        this.listeningModeEnable = listeningModeEnable;
        this.termination = termination;
        this.bitrate = bitrate;
        int result = configureCanInterface(listeningModeEnable, bitrate, termination, hardwareFilters, portNumber, null);
        if (result < 0){
            throw new CanbusException("Error creating Interface", result);
        }
    }

    /**
     *
     * @param listeningModeEnable
     * @param bitrate
     * @param termination
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *@param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     *
     */
    public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls) throws CanbusException{
        this.listeningModeEnable = listeningModeEnable;
        this.termination = termination;
        this.bitrate = bitrate;
        int result = configureCanInterface(listeningModeEnable, bitrate, termination, hardwareFilters,portNumber,flowControls);
        if (result < 0){
            throw new CanbusException("Error creating Interface", result);
        }
    }

    /**
     * Removes Canbus interface for port 1 (down).
     */
    public void removeCAN1() {
        removeCAN1Interface();
    }

    /**
     * Removes Canbus interface for port 2 (down).
     */
    public void removeCAN2() {
        removeCAN2Interface();
    }

    /**
     * Sets interface bitrate by creating an interface.
     * Interface must be removed first!
     * @param bitrate
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
     */
    public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters, int portNumber) {
        configureCanInterface(this.listeningModeEnable, bitrate, this.termination, hardwareFilters, portNumber,null);
    }


    /**
     * Sets interface bitrate by creating an interface.
     * Interface must be removed first!
     * @param bitrate
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
     * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     */
    public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls) {
        configureCanInterface(this.listeningModeEnable, bitrate, this.termination, hardwareFilters, portNumber,flowControls);
    }

    /**
     * Changing termination will result in the CAN module being re-opened.
     * @param termination
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
     */
    public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber) {
        configureCanInterface(this.listeningModeEnable, this.bitrate, termination,hardwareFilters, portNumber,null);
    }



    /**
     * Changing termination will result in the CAN module being re-opened.
     * @param termination
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
     */
    public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls) {
        Log.d(TAG, "Listening Mode: " + listeningModeEnable + ", Termination: " + termination + ", Port Number: " + portNumber);
        configureCanInterface(this.listeningModeEnable, this.bitrate, termination,hardwareFilters, portNumber,flowControls);
    }

    /**
     *	Creates new socket on Canbus interface for CAN port 1
     */
    public CanbusSocket createSocketCAN1(){
        return new FlexCANCanbusSocket(fdCanPort1,canPort1Number);
    }

    /**
     *	Creates new socket on Canbus interface for CAN port 2
     */
    public CanbusSocket createSocketCAN2(){
        return new FlexCANCanbusSocket(fdCanPort2,canPort2Number);
    }

    /**
     *	Creates a new interface for J1708 communication
     */
    public int createJ1708(){
       return createJ1708Interface();
    }

    /**
     *	Removes the interface for J1708 communication
     */
    public int removeJ1708(){
        return removeJ1708Interface();
    }

    /**
     *  Creates new socket on J1708 interface
     */
    public J1708Socket createSocketJ1708(){
        return new FlexCANJ1708Socket(fdJ1708);
    }


    /**
     * {@inheritDoc}
     */
    public void setListeningMode(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters, int portNumber){
        configureCanInterface(listeningModeEnable, this.bitrate, this.termination,hardwareFilters, portNumber,null);
    }

    /**
     * {@inheritDoc}
     */
    public void setListeningMode(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControl){
        configureCanInterface(listeningModeEnable, this.bitrate, this.termination,hardwareFilters, portNumber,flowControl);
    }

    private native int configureCanInterface(boolean listeningModeEnable, int bitrate, boolean termination,CanbusHardwareFilter[] hardwareFilters, int portNumber, CanbusFlowControl[] flowControl);
    private native int removeCAN1Interface();
    private native int removeCAN2Interface();
    private native int createJ1708Interface();
    private native int removeJ1708Interface();

    static
    {
        System.loadLibrary("vehiclebus");
    }
}
