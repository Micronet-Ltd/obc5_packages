package com.micronet.canbus;

/**
 * Defines the frame types - Standard and Extended.
 */
public class CanbusFrameType {

	private int mType;
	
	/**
	 * Defines standard frame type.
	 */
	static public CanbusFrameType STANDARD = new CanbusFrameType(0);
	
	/**
	 * Defines extended frame type.
	 */
	static public CanbusFrameType EXTENDED = new CanbusFrameType(1);
	
	
	private CanbusFrameType(int type) {
		mType = type;
	}
	
}
