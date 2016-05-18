package com.ehang.sos;

import android.content.Context;
import android.content.SharedPreferences;

public class Storage {
	SharedPreferences sp;
	SharedPreferences.Editor editor;

	private static Storage instance = null;

	public static Storage getInstance() {
		if (instance == null) {
			instance = new Storage();
		}
		return instance;
	}

	public void putinStorage(Context context, String tag, String value) {
		sp = context.getSharedPreferences("SOSDataTable", Context.MODE_PRIVATE);
		editor = sp.edit();
		editor.putString(tag, value);
		editor.commit();
	}

	public String getoutStorage(Context context, String tag) {
		sp = context.getSharedPreferences("SOSDataTable", Context.MODE_PRIVATE);
		return sp.getString(tag, "");
	}

}
