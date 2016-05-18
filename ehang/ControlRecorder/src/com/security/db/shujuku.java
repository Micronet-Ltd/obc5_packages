package com.security.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class shujuku extends SQLiteOpenHelper {
	// 数据库的名字
	private static String name = "xinren.db";
	// 设定一个版本号
	private static int version = 1;

	public shujuku(Context context) {
		super(context, name, null, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "create table xinren(uid integer primary key autoincrement ,state integer,packageName varchar(13345))";
		db.execSQL(sql);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
