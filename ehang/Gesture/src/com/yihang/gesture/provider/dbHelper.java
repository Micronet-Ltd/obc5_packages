package com.yihang.gesture.provider;

import com.yihang.gesture.R;

import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class dbHelper extends SQLiteOpenHelper {

	private static final String DBNAME = "list.db";
	private static final int DBVERSION = 1;
	final Resources res;
	public dbHelper(Context context) {
		super(context, DBNAME, null, DBVERSION);
		res =  context.getResources();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String date = util.getCurrentDate();
		db.execSQL("CREATE TABLE 'list' ('id' INTEGER PRIMARY KEY  NOT NULL , 'action' TEXT NOT NULL , 'intent' TEXT NOT NULL , 'state' INTEGER , 'createTime' TEXT)");
		if(res.getBoolean(R.bool.light_screen))
		{
			 db.execSQL("INSERT INTO 'list' VALUES(1,'" + EnumAction.LIGHT_SCREEN + "','','1','" + date + "');");
		}
		if(res.getBoolean(R.bool.open_camera))
		{
			 db.execSQL("INSERT INTO 'list' VALUES(2,'" + EnumAction.OPEN_CAMERA + "','','1','" + date + "');");
		}
		if(res.getBoolean(R.bool.open_counter))
		{
			 db.execSQL("INSERT INTO 'list' VALUES(3,'" + EnumAction.OPEN_COUNTER + "','','1','" + date + "');");
		}
		db.execSQL("CREATE TABLE 'switchstate' ('switchstate' INTEGER PRIMARY KEY  NOT NULL, 'updateTime' TEXT, 'createTime' TEXT)");
		db.execSQL("INSERT INTO 'switchstate' VALUES(0,NULL,'" + date + "');");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS list");
		db.execSQL("DROP TABLE IF EXISTS switchstate");
		onCreate(db);
	}
}
