package com.micronet.canbus;

import java.util.Arrays;

/**
 * Sets Canbus hardware module to pass certain packet ids.
 * Filtering is done using both Mask and Frame Id to achieve maximum flexability.    
 */
public class CanbusHardwareFilter {
	int i;
	private int[] mIds;
	private int[] mMask;
	private int[] filterType={EXTENDED};

	static public int STANDARD=0;
	static public int EXTENDED=1;

	/**
	 * Creates filter with frame ids and mask.
	 *
	 *
	 *
	 * @param ids register frame ids. 
	 * @param mask filter mask to be used in conjunction with frame ids.
	 * @param type Standard / Extended frame.
	 */
	public CanbusHardwareFilter(int[] ids, int[] mask, int[] type){
		mIds = ids;
		mMask = mask;
		filterType = type;
	}

	/**
	 * Creates filter with frame ids only.
	 * @param ids register frame ids to pass.
	 * @param type Standard / Extended frame.
	 */
	public CanbusHardwareFilter(int[] ids, int[] filtType){
		mIds = ids;
		filterType = filtType;
	}
	
	/**
	 * Sets the filter mask.
	 */
	public void setMask(int[] mask) {
		mMask = mask;
	}
	
	/**
	 * Sets the filter type.
	 */
	/*public void setType(CanbusFrameType type) {
		mType = type;
	}*/
	
	/**
	 * Returns the filter mask being used.
	 */
	public int[] getMask() {
		return mMask;
	}
	
	/**
	 * Returns the filter type being used.
	 */
	/*public CanbusFrameType[] getType() {
		return mType;
	}*/
	
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

	/**
	 * Returns filter type
	 */
	public int[] getFilterMaskType() {
		//int[] integerArray = Arrays.copyOf(filterType, filterType.length, int[].class);
//		Integer[] intArray = new Integer[filterType.length];
//
//		for(int i=0; i<filterType.length; i++){
//			intArray[i] = (Integer) filterType[i];
//		}

		return filterType;
	}
}
