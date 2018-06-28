package com.micronet.canbus;

import android.util.Log;

/**
 * Represents Canbus network interface and exposes operations like
 * create/removeCAN1 interface with filters, configure bitrate &amp which are all related to canbus device driver level.
 */

public class CanbusInterface { 
	private static final String TAG = "CanbusSocketPort1";

	IVehicleInterfaceBridge impl;

	public CanbusInterface()
	{
		switch(CanbusInterface.getImplId())
		{
			case 2: impl = new FlexCANVehicleInterfaceBridge(); break;
		}
	}

	/**
	 * Creates new Canbus interface with hardware filters and default values [ListeningMode=false, Baud rate=250000,Termination=true] (up).
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
	 */
	public void create(CanbusHardwareFilter[] hardwareFilters,int portNumber)
			throws CanbusException {
		impl.create(hardwareFilters,portNumber);
	}



	/**
	 * Creates new Canbus interface with hardware filters and default values [ListeningMode=false, Baud rate=250000,Termination=true] (up).
	 * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
	 * @param portNumber 2, CAN1.
	 *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
	 *
	 * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
	 */
	public void create(CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls)
			throws CanbusException {
		impl.create(hardwareFilters,portNumber,flowControls);
	}



	/**
	 * Creates new Canbus interface in the specified mode with the default parameters [Baud rate=250000,Termination=true](up).
	 * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
	 */
	public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters, int portNumber)
			throws CanbusException {
		impl.create(listeningModeEnable,hardwareFilters,portNumber);
	}



	/**
	 * Creates new Canbus interface with a default baud rate [Baud rate = 2500000](up).
	 * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
	 * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
	 * @param portNumber 2, CAN1.
	 *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
	 *
	 * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
	 */
	public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters, int portNumber,CanbusFlowControl[] flowControls)
			throws CanbusException {
		impl.create(listeningModeEnable,hardwareFilters,portNumber,flowControls);
	}



	/**
	 * Creates new Canbus interface (up).
	 * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
	 * @param bitrate Supported bit rates: 10/20/33.33/50/100/125/250/500/800 Kbits/ second or 1Megbits/sec
	 * @param termination Changing termination will result in the CAN module being re-opened.
	 *                    true, enables the termination resistor in the device.
	 *                    false, disables the termination resistor in the device.
     * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
     * @param portNumber 2, CAN1.
     *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
     *
	 */
	public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters,int portNumber)
			throws CanbusException {
		impl.create(listeningModeEnable, bitrate,termination,hardwareFilters, portNumber);
	}



	/**
	 * Creates new Canbus interface (up).
	 * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
	 * @param bitrate Supported bit rates: 10/20/33.33/50/100/125/250/500/800 Kbits/ second or 1Megbits/sec
	 * @param termination Changing termination will result in the CAN module being re-opened.
	 *                    true, enables the termination resistor in the device.
	 *                    false, disables the termination resistor in the device.
	 * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
	 * @param portNumber 2, CAN1.
	 *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
	 *
	 * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
	 */
	public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls)
			throws CanbusException {
		impl.create(listeningModeEnable, bitrate,termination,hardwareFilters, portNumber,flowControls);
	}

	/**
	 * Removes Canbus interface for Port 1(down).
	 */
	public void removeCAN1() {
		impl.removeCAN1();
	}

	/**
	 * Removes Canbus interface for Port 2(down).
	 */
	public void removeCAN2() {
		impl.removeCAN2();
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
	public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters,int portNumber) {
		impl.setBitrate(bitrate,hardwareFilters, portNumber);
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
	public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls) {
		impl.setBitrate(bitrate,hardwareFilters, portNumber,flowControls);
	}



	/**
	 * Changing termination will result in the CAN module being re-opened.
	 * @param termination
	 * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
	 * @param portNumber 2, CAN1.
	 *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
	 *
	 */
	public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters,int portNumber)
			throws CanbusException {
		impl.create(termination, hardwareFilters,portNumber);
	}



	/**
	 * Changing termination will result in the CAN module being re-opened.
	 * @param termination
	 * @param hardwareFilters Filters, masks and filter types used for filtering CAN packets.
	 * @param portNumber 2, CAN1.
	 *                   3, CAN2 (Can also be used as single wired CAN if the baud rate is set to 33.33 Kbits per seconds).
	 *
	 * @param flowControls Search Ids, Response Ids, Response Data lengths, Response Data pairs to set auto respond flow control messages.
	 */
	public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters,int portNumber,CanbusFlowControl[] flowControls)
			throws CanbusException {
		impl.create(termination, hardwareFilters,portNumber,flowControls);
	}



	/**
	 *	Creates new socket on Canbus interface on CAN port 1.
	 */
	public CanbusSocket createSocketCAN1(){
		return impl.createSocketCAN1();
	}

	/**
	 *	Creates new socket on Canbus interface on CAN port 2 .
	 */
	public CanbusSocket createSocketCAN2(){
		return impl.createSocketCAN2();
	}


	private native static int getImplId();
	static
	{
		System.loadLibrary("canbus");
	}


}


