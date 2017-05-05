package com.micronet.canbus;

/**
 * Sets Canbus socket to pass certain packet ids.
 * Identifier fields of incoming messages are compared to the filter value. 
 * If there is a match, that message will pass and reach CanbusSocket. 
 */
public class CanbusSoftwareFilter { 
 
	private int mId;
	private CanbusFrameType mType = CanbusFrameType.STANDARD;
	
	/**
	 * Creates filter with frame id.
	 * 
	 * @param id register frame id to pass. 
	 * @param type Standard / Extended frame.
	 */
	public CanbusSoftwareFilter(int id, CanbusFrameType type) {
		super();
		mId = id;
		mType = type;
	}
	
	/**
     * Returns the frame type.
     */
	public CanbusFrameType getType() {
		return mType;
	}
	
	/**
     * Sets the frame type.
     */
	public void setType(CanbusFrameType type) {
		mType = type;
	}
	
	/**
     * Returns the frame id.
     */
	public int getId() {
		return mId;
	}
	
	/**
     * Sets the frame id.
     * @param id this id will pass filter and reach CanbusSocket

     */
	public void setId(int id) {
		mId = id;
	}
}
