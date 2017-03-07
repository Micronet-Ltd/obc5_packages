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
	public void create() {
		impl.create();
	}

	/**
	 * Creates new Canbus interface (up).
	 * @param listeningModeEnable         true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
	 */
	public void create(boolean listeningModeEnable) {
		impl.create(listeningModeEnable);
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
	public void setBitrate(int bitrate) {
		impl.setBitrate(bitrate);
	}
/*
	*//**
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
	
/*
	*/
/**
	  * Check if J1708 is supported
	  *//*

	public boolean isJ1708Supported(){
		return impl.checkJ1708Support();
	}
*/

	/**
	 * Set the CAN module mode between Normal and Silent (QBridge support only).
	 * @param listeningModeEnable true, disables the CAN module's transmit signal. The CAN module is still able to receive messages from the CANbus.
	 *                            This mode may be used to analyze a CANbus without disturbing the bus.
	 *                            false, turns on the CAN module's transmitter and receiver.
	 *                            This mode doesn't affect the J1708 transmission line.
	 */
	public void setListeningMode(boolean listeningModeEnable) {
		impl.setListeningMode(listeningModeEnable);
	}
/*
	public void sendRecoveryCommand(int action) {
		impl.sendRecovery(action);
	}
*/

	private native static int getImplId();
	static
	{
		System.loadLibrary("canbus");
	}
}


