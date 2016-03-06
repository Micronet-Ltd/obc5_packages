package com.yihang.gesture.provider;
import java.text.SimpleDateFormat;

public class util {
	
	public static String getCurrentDate() {		
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		return dateFormat.format(new java.util.Date());
	}
}
