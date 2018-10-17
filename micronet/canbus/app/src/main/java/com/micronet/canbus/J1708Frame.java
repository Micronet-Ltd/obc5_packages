package com.micronet.canbus;

/**
 * J1708 frame encapsulator.
 * Includes byte array that represents the payload data within Canbus packet,
 * id that holds both Standard & Extended identifiers and packet type indicator (Standard/Extended).  
 */
public class J1708Frame{

	private static final String TAG = "Canbus";
	private int mId;
	private byte[] mData;
	
	/**
     * Constructs data frame with id and data buffer.
     */
	public J1708Frame( int id, byte[] data)
	{
		mData = data;
		mId = id;
	}
	
    /**
     * Returns the frame message data buffer.
     */
	public byte[] getData() {
		return mData;
	}
	
	/**
     * Sets the frame message data buffer.
     */
	public void setData(byte[] data) {
		this.mData = data;
	}
	
	/**
     * Returns the frame message identifier.
     */
	public int getId() {
		return mId;
	}
	
	/**
     * Sets the frame message identifier.
     */
	public void setId(int id) {
		this.mId = id;
	}

}
