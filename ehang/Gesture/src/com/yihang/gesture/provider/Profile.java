package com.yihang.gesture.provider;

import java.util.ArrayList;
import java.util.HashMap;

import com.yihang.gesture.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Profile {
	private SQLiteDatabase db;
	public static Profile profile = null;

	public static Profile getInstance(Context c) {
		if (profile == null) {
			profile = new Profile(c);
		}
		return profile;
	}

	public Profile(Context c) {
		dbHelper helper = new dbHelper(c);
		db = helper.getWritableDatabase();
	}
	
	public ArrayList<HashMap<String, Object>> getAll() {
		String sql = "select id,action,intent,state from list";
		Cursor c = db.rawQuery(sql, null);
		ArrayList<HashMap<String, Object>> listData = new ArrayList<HashMap<String, Object>>();

		while (c.moveToNext()) {
			HashMap<String, Object> map = new HashMap<String, Object>();

			map.put("id", c.getString(0));
			if(c.getString(1).equals(EnumAction.OPEN_CAMERA)){
				map.put("action", R.string.open_camera);
				map.put("img", R.drawable.open_camera);
			}else if(c.getString(1).equals(EnumAction.LIGHT_SCREEN)){
				map.put("action", R.string.light_screen);
				map.put("img", R.drawable.light_screen);
			}
			else if(c.getString(1).equals(EnumAction.OPEN_COUNTER)){
				map.put("action", R.string.open_counter);
				map.put("img", R.drawable.open_counter);
			}
			map.put("intent", c.getString(2));
			map.put("state", c.getString(3));
			
			listData.add(map);
		}
		return listData;
	}
	
	public int getSwitchstate() {
		Cursor c = db.rawQuery("select switchstate from switchstate", null);
		int result=0;
		if (c.moveToNext()) {
			result=c.getInt(0);
		} 
		c.close();
		return result;
	}
	
	public void updateSwitchstate(int switchstate) {
		ContentValues values = new ContentValues();
		values.put("switchstate", switchstate);
		values.put("updateTime", util.getCurrentDate());
		db.update("switchstate", values, null, null);
	}
	
	public int getGloveSwitchstate() {
		Cursor c = db.rawQuery("select switchstate from golveswitchstate", null);
		int result=0;
		if (c.moveToNext()) {
			result=c.getInt(0);
		} 
		c.close();
		return result;
	}
	
	public void updateGloveSwitchstate(int switchstate) {
		ContentValues values = new ContentValues();
		values.put("switchstate", switchstate);
		values.put("updateTime", util.getCurrentDate());
		db.update("golveswitchstate", values, null, null);
	}
}
