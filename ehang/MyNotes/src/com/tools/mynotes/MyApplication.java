
package com.tools.mynotes;

import android.app.Activity;
import java.util.*;
import android.app.*;

public class MyApplication extends Application {

	private List<Activity> activityList = new LinkedList<Activity>(); 
	private static MyApplication instance;
	private MyApplication() {
		
	}
	
	public static MyApplication getInstance() {
		if(null == instance)
		{
			instance = new MyApplication();
		}
		return instance;
	}
	
	/**
	 * add activity to container
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		for(Activity a : activityList) {
			if (a == activity) {
				a.finish();
				break;
			}
		}
		activityList.add(activity);
	}
	
	/**
	 * Iterate over all Activity and finish 
	 */
	public void exit() {
		for(Activity activity : activityList) {
			activity.finish();
		}
		System.exit(0);
	}
}