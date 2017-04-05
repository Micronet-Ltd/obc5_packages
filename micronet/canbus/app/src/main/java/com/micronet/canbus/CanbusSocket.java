package com.micronet.canbus;

/**
 * Handles the read/write Canbus communication via CanSocket interface.
 * CanbusSocket can be created when Canbus interface is available.
 */
public class CanbusSocket implements CanbusListener{

	private static final String TAG = "CanbusSocket";


	protected CanbusSocket() {}
	/**
	 * Reads Canbus frame. Will block the calling thread until data
	 * is written to Canbus socket.
	 */
	public CanbusFrame read(){
		throw new IllegalArgumentException("Not implemented");
	}

	/**
	 * Reads Canbus frame. Will block the calling thread until data
	 * is written to Canbus socket or timeout has elapsed.
	 *
	 * @param timeout how long to wait before giving up, in units of milliseconds
	 *
	 * @return the head Canbus frame, or null if the specified waiting time elapses before Canbus frame is available
	 */
	public CanbusFrame read(long timeout) {
		throw new IllegalArgumentException("Not implemented");
	}
	
	/**
     * Reads J1708 frame. Will block the calling thread until data
     * is written to Canbus socket.
     */
	public J1708Frame readJ1708(){
		throw new IllegalArgumentException("Not implemented");
	}

	/**
	 * Reads J1708 frame. Will block the calling thread until data
	 * is written to Canbus socket or timeout has elapsed.
	 *
	 * @param timeout how long to wait before giving up, in units of milliseconds
	 *
	 * @return the head J1708 frame, or null if th specified waiting time elapses before J1708 frame is available
	 */
	public J1708Frame readJ1708(long timeout){
		throw new IllegalArgumentException("Not implemented");
	}
	
	/**
	 * For internal use only.
	 */
	@Override
	public void onPacketReceive(CanbusFrame frame) {
	}
	
	/**
	 * For internal use only.
	 */
	@Override
	public void onPacketReceiveJ1708(J1708Frame frame) {
	}

	/**
     * Sends Canbus frame through socket.
     */
	public void write(CanbusFrame frame){
	}

	/**
     * Sends Canbus frame through socket.
     */
	public void writeJ1708(J1708Frame frame){
	}


	/**
	 * Opens Canbus socket for read/write operations.
	 */
	public void open() {
	}

	/**
	 * Closes Canbus socket.
	 */
	public void close(){
	}
	/**
	 * Discards packets from the queue's receive buffer. Due to the nature of the QBridge buffer,
	 * packets will be discarded for 3 seconds following execution of this command.
	 */
	public void discardInBuffer(){
	}
	/**
	 * Returns Canbus socket id.
	 */
	public int getId(){
		return -1;
	}
	
	
	/**
	 * Adds software filters to be apply only to the this socket.
	 */
	/*public void setMasks(CanbusSoftwareFilter[] filters){
		throw new IllegalArgumentException("Software filter not supported");
	}*/
	
}
