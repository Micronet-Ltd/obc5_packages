package com.yihang.gesture.service;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Application;

public class ExitApp extends Application{

	ArrayList<Activity> activitylist = new ArrayList<Activity>();        //放置所有的Activity
	private static ExitApp exitapp;
	
	public ExitApp(){
	}
	
	public static ExitApp getInstance(){
		if(null == exitapp){
			exitapp = new ExitApp();
		}
		
		return exitapp;
	}
	
	public void addActivity(Activity activity){ 
		activitylist.add(activity);
	}
	
	public void exit(){
		try {
			for(Activity activity: activitylist){
				activity.finish();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}
}