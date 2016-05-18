package com.security.db;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class kaiguanzhi {
	boolean flag = false;

	public boolean addUser(Object[] param, Context context) {
		String sql = "insert into kaiguan(state)values(?)";

		kaiguan db = new kaiguan(context);
		SQLiteDatabase database = null;
		try {
			database = db.getWritableDatabase();

			ContentValues values = new ContentValues();
			values.put("state", (Integer) param[0]);

			database.insert("kaiguan", null, values);
			flag = true;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {

			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

	public boolean deleteUser(Context context) {
		boolean flag = false;
		kaiguan db = new kaiguan(context);
		String sql = "delete from kaiguan";
		SQLiteDatabase database = null;
		try {
			database = db.getWritableDatabase();
			database.execSQL(sql);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

	public boolean updateUser(Object[] param, Context context) {
		boolean flag = false;
		SQLiteDatabase database = null;
		String sql = "update kaiguan set state=?";
		kaiguan db = new kaiguan(context);
		try {
			database = db.getWritableDatabase();
			database.execSQL(sql, param);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

}
