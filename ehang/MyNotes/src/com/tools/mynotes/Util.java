package com.tools.mynotes;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.ClipboardManager;
import android.text.format.DateFormat;

public class Util {
	private static final String FORMAT_YEAR = "yyyy-MM-dd HH:mm";
	
	/**
	 * Date display format<br/>
	 * YYYY/MM/dd  or  MM/dd HH:mm
	 * @param date
	 * @return
	 */
	public static String convertDate(Context context,Date date,boolean is24HourMode) {
		if(!is24HourMode){
			return new SimpleDateFormat("hh:mm a").format( date);
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_YEAR);
		String year = dateFormat.format(date);
		String yearNow = dateFormat.format(System.currentTimeMillis());
		String ret = null;
		if (/*com.mediatek.common.featureoption.FeatureOption.HQ_LAVA_MYNOTE_DATEFORMAT*/false){
			return DateFormat.getDateFormat(context).format(date);
		}
		if (!yearNow.substring(0, 4).equals(year.substring(0, 4))) {
			if (true){
				return DateFormat.getDateFormat(context).format(date);
			}
			ret = year.substring(0, 10);
		} else {
			ret = year.substring(5, 16);
			if (true){
				return getLavaTime(context, ret);
			}
		}
		return ret;
	}
	public static String getLavaTime(Context context, String date){
		String retDate = "";
		int index  = date.lastIndexOf(":");
		if (index < 3){
			return date;
		}
		StringBuffer bf = new StringBuffer();
		try{
		String subDateString = date.substring(index - 2, index);
		int hour = Integer.parseInt(subDateString);
		String ampm = "";
		if (!get24HourMode(context)){
			if (hour > 12){
				hour -= 12;
				ampm = " pm";
			}else{
				ampm = " am";
			}
		}

		bf.append(date.substring(0, index - 2));
		bf.append(hour);
		bf.append(date.substring(index));
		bf.append(ampm);
		}catch(Exception e){
			return date;
		}
	
		return bf.toString();
	}

	static boolean get24HourMode(Context context) {
		return android.text.format.DateFormat.is24HourFormat(context);
	}


	/** 
	    * Implemented text replication
	    * @param content To copy the content
	    */  
	public static void copy(String content, Context context) { 
	// Get the clipboard manager
	ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE); 
	cmb.setText(content.trim()); 
	}
}
