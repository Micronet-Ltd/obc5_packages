package com.micronet.canbus;

/**
 * Handles the readPort1/write1939Port1 Canbus communication via CanSocket interface.
 * CanbusSocket can be created when Canbus interface is available.
 */
public class CanbusSocket implements CanbusListener{

	private static final String TAG = "CanbusSocket";


	protected CanbusSocket() {}

	/**
	 * Reads Canbus frame. Will block the calling thread__port1 until data
	 * is written to Canbus socket.
	 */
	public CanbusFramePort1 readPort1(){
		throw new IllegalArgumentException("Not implemented");
	}

	/**
	 * Reads Canbus frame. Will block the calling thread__port1 until data
	 * is written to Canbus socket.
	 */
	public CanbusFramePort2 readPort2(){
		throw new IllegalArgumentException("Not implemented");
	}

	/**
	 * Reads Canbus frame. Will block the calling thread__port1 until data
	 * is written to Canbus socket or timeout has elapsed.
	 *
	 * @param timeout how long to wait before giving up, in units of milliseconds
	 *
	 * @return the head Canbus frame, or null if the specified waiting time elapses before Canbus frame is available
	 */
	public CanbusFramePort1 readPort1(long timeout) {
		throw new IllegalArgumentException("Not implemented");
	}


	/**
	 * Reads Canbus frame. Will block the calling thread__port1 until data
	 * is written to Canbus socket or timeout has elapsed.
	 *
	 * @param timeout how long to wait before giving up, in units of milliseconds
	 *
	 * @return the head Canbus frame, or null if the specified waiting time elapses before Canbus frame is available
	 */
	public CanbusFramePort2 readPort2(long timeout) {
		throw new IllegalArgumentException("Not implemented");
	}
	
	/**
     * Reads J1708 frame. Will block the calling thread__port1 until data
     * is written to Canbus socket.
     */
	public J1708Frame readJ1708(){
		throw new IllegalArgumentException("Not implemented");
	}

	/**
	 * Reads J1708 frame. Will block the calling thread__port1 until data
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
	public void onPacketReceive1939Port1(CanbusFramePort1 frame) {
	}

	/**
	 * For internal use only.
	 */
	@Override
	public void onPacketReceive1939Port2(CanbusFramePort2 frame) {

	}

	/**
	 * For internal use only.
	 */
	@Override
	public void onPacketReceiveJ1708Port(J1708Frame frame) {
	}

	/**
     * Sends Canbus frame through socket.
     */
	public void write1939Port1(CanbusFramePort1 frame){
	}

	/**
	 * Sends Canbus frame through socket.
	 */
	public void write1939Port2(CanbusFramePort2 frame){
	}

	/**
     * Sends Canbus frame through socket.
     */
	public void writeJ1708Port(J1708Frame frame){
	}


	/**
	 * Opens Canbus socket for readPort1/readPort1/write1939Port1 operations.
	 */
	public void open() {
	}

	/**
	 * Closes Canbus socket for Port 1.
	 */
	public void close1939Port1(){
	}

	/**
	 * Closes Canbus socket for Port 1.
	 */
	public void close1939Port2(){
	}

	/**
	 * Closes Canbus socket for Port 1.
	 */
	public void close1708Port(){
	}


	/**
	 * Discards packets from the queue's receive buffer. Due to the nature of the QBridge buffer,
	 * packets will be discarded for 3 seconds following execution of this command.
	 */
	public void discardInBuffer(){
	}
//
//	/**
//	 * Returns Canbus socket id.
//	 */
//	public int getId(){
//		return -1;
//	}

	/**
	 * Returns Canbus socket id for CAN Port 1
	 */
	public int getCan1PortId(){
		return -1;
	}

	/**
	 * Returns Canbus socket id for CAN Port 2
	 */
	public int getCan2PortId(){
		return -1;
	}

	/**
	 * Returns Canbus socket id for J1708 port
	 */
	public int getJ708PortId(){
		return -1;
	}

	public int getId(){
		return -1;
	}




}
