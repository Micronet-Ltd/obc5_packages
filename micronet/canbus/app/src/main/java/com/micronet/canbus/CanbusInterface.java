package com.micronet.canbus;

/**
 * Represents Canbus network interface and exposes operations like
 * create/remove interface with filters, configure bitrate &amp which are all related to canbus device driver level.
 */
public class CanbusInterface { 
	private static final String TAG = "CanbusSocket";

	ICanbusInterfaceBridge impl;

	public CanbusInterface()
	{
		switch(CanbusInterface.getImplId())
		{
			case 2: impl = new FlexCANCanbusInterfaceBridge(); break;
		}
	}

	/**
	 * Creates new Canbus interface with hardware filters and default values [ListeningMode=false, Baud rate=250000,Termination=true] (up).
	 */
	public void create(CanbusHardwareFilter[] hardwareFilters) {
		impl.create(hardwareFilters);
	}

	/**
	 * Creates new Canbus interface (up).
	 * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
	 */
	public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters) {
		impl.create(listeningModeEnable,hardwareFilters);
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
	 */
	public void create(boolean listeningModeEnable, int bitrate, boolean termination, CanbusHardwareFilter[] hardwareFilters){
		impl.create(listeningModeEnable, bitrate,termination,hardwareFilters);
	}

	/**
	 * Removes Canbus interface (down).
	 */
	public void remove() {
		impl.remove();
	}

	/**
	 * Sets interface bitrate.
	 * Interface must be removed first!  
	 */
	public void setBitrate(int bitrate,CanbusHardwareFilter[] hardwareFilters) {
		impl.setBitrate(bitrate,hardwareFilters);
	}

	/**
	 * Changing termination will result in the CAN module being re-opened.
	 * @param termination
	 */
	public void setCANTermination(boolean termination, CanbusHardwareFilter[] hardwareFilters) {
		impl.create(termination, hardwareFilters);
	}

	/**
	 *	Creates new socket on Canbus interface. 
	 */
	public CanbusSocket createSocket(){
		return impl.createSocket();
	}

	private native static int getImplId();
	static
	{
		System.loadLibrary("canbus");
	}
}


