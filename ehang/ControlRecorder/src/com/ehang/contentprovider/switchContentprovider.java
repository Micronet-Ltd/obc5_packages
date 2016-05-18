package com.ehang.contentprovider;

import com.ehang.show.ShowActivity;
import com.security.db.kaiguan;
import com.security.db.kaiguanzhi;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class switchContentprovider extends ContentProvider {

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		//Log.e("com.example.switchContentprovider", "进入");
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.e("com.example.switchContentprovider", "进入查询");
		Log.e("com.example.switchContentprovider",
				"uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu");
		kaiguan kk = new kaiguan(getContext());
		SQLiteDatabase database = kk.getReadableDatabase();

		Cursor cursor = database.query("kaiguan", new String[] { "state" },
				null, null, null, null, null);
		for (cursor.moveToFirst(); !cursor.isLast(); cursor.moveToNext()) {
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				System.out.println(cursor.getColumnName(i) + " "
						+ cursor.getString(i));
				Log.e("cursor的值是多少", "" + "" + cursor.getColumnName(i));
				Log.e("具体中", "" + "" + cursor.getString(i));
			}
		}

		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
