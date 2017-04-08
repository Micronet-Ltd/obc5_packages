package com.micronet.canbus;

/**
 * Represents Canbus network interface and exposes operations like
 * create/remove interface, configure bitrate &amp; hw filters which are all related to canbus device driver level.   
 */
public class CanbusInterface { 
	private static final String TAG = "CanbusSocket";

	ICanbusInterfaceBridge impl;


	public CanbusInterface()
	{
		switch(CanbusInterface.getImplId())
		{
			case 2: impl = new FlexCANCanbusInterfaceBridge(); break;
			//case 1: impl = new SocketCANCanbusInterfaceBridge(); break;
		}
	}

	/**
	 * Creates new Canbus interface (up).
	 */
	public void create(CanbusHardwareFilter[] hardwareFilters) {
		impl.create(hardwareFilters);
	}

	/**
	 * Creates new Canbus interface (up).
	 * @param listeningModeEnable         true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
	 */
	public void create(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters) {
		impl.create(listeningModeEnable,hardwareFilters);
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
	 * Returns the CAN baud rate.
	 * @return possible CAN bad rate values of 125K, 250K, 500K, 1Meg. A value of 0 indicates that
	 * QBridge didn't respond with current baud rate. Try calling setBitrate if this occurs.
	 *//*
	public int getBitrate() {
		return impl.getBitrate();
	}*/

	/**
	 * Sets filters in Canbus hardware controller.
	 */
	public void setFilters(CanbusHardwareFilter[] hardwareFilters) {
		impl.setFilters(hardwareFilters);
	}

	/**
	 *	Creates new socket on Canbus interface. 
	 */
	public CanbusSocket createSocket(){
		return impl.createSocket();
	}

	/**
	 * Set the CAN module mode between Normal and Silent (QBridge support only).
	 * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
	 *                            This mode doesn't affect the J1708 transmission line.
	 */
	public void setListeningMode(boolean listeningModeEnable,CanbusHardwareFilter[] hardwareFilters) {
		impl.setListeningMode(listeningModeEnable,hardwareFilters);
	}

/*
	* Closes the Can Socket

	public FlexCANCanbusSocket close(){
		return impl.close();
	}*/

	private native static int getImplId();
	static
	{
		System.loadLibrary("canbus");
	}
}


