package com.micronet.canbus;

/**
 * Sets Canbus hardware module to pass certain packet ids.
 * Filtering is done using both Mask and Frame Id to achieve maximum flexability.    
 */
public class CanbusHardwareFilter {

	private int[] mIds;
	private int mMask = 0xFFFFFFFF;
	private CanbusFrameType mType = CanbusFrameType.STANDARD;
	
	/**
	 * Creates filter with frame ids and mask.
	 * 
	 * @param ids register frame ids. 
	 * @param mask filter mask to be used in conjunction with frame ids.
	 * @param type Standard / Extended frame.
	 */
	public CanbusHardwareFilter(int[] ids, int mask, CanbusFrameType type){
		mIds = ids;
		mMask = mask;
		mType = type;
	}
	
	/**
	 * Creates filter with frame ids only.
	 * 
	 * @param ids register frame ids to pass. 
	 * @param type Standard / Extended frame.
	 */
	public CanbusHardwareFilter(int[] ids, CanbusFrameType type){
		mIds = ids;
		mType = type;
	}
	
	/**
	 * Sets the filter mask.
	 */
	public void setMask(int mask) {
		mMask = mask;
	}
	
	/**
	 * Sets the filter type.
	 */
	public void setType(CanbusFrameType type) {
		mType = type;
	}
	
	/**
	 * Returns the filter mask being used.
	 */
	public int getMask() {
		return mMask;
	}
	
	/**
	 * Returns the filter type being used.
	 */
	public CanbusFrameType getType() {
		return mType;
	}
	
	/**
	 *  Sets the filters ids.
	 */
	public void setIds(int[] ids) {
		mIds = ids;
	}
	
	/**
	 *  Returns the filter ids being used.
	 */
	public int[] getIds() {
		return mIds;
	}
}
