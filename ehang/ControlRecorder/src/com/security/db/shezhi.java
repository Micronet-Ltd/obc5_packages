package com.security.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class shezhi {
	// 添加数据
	boolean flag = false;

	// 添加信息或者往数据中写入信息
	public boolean addUser(Object[] param, Context context) {
		String sql = "insert into xinren(state,packageName)values(?,?)";
		// 获取sqliteDatabase对象，并且操作数据库
		// 获取数据库对象
		shujuku db = new shujuku(context);
		SQLiteDatabase database = null;
		try {
			database = db.getWritableDatabase();
			// 利用contentValues来存放参数
			ContentValues values = new ContentValues();
			values.put("state", (Integer) param[0]);
			values.put("packageName", (String) param[1]);
			// 调用Android自己的插入的方法
			database.insert("xinren", null, values);
			flag = true;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			// 执行完之后要关闭database
			if (database != null) {
				database.close();
			}
		}
		return flag;
	}

	public boolean updateUser(Object[] param, Context context) {
		boolean flag = false;
		SQLiteDatabase database = null;
		String sql = "update xinren set state=?where packageName=?";
		shujuku db = new shujuku(context);
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

	// 查出状态
	// 查询数据的方法，查询出state是1的应用
	public List<Map<String, Object>> list(Context context) {
		List<Map<String, Object>> uu = new ArrayList<Map<String, Object>>();
		shujuku db = new shujuku(context);
		SQLiteDatabase database = null;
		try {
			database = db.getReadableDatabase();
			// 查询后的结果返回的是一个游标,查寻出所有数据
			Cursor cursor = database.query("xinren",
					new String[] { "packageName" }, "state=?",
					new String[] { "1" }, null, null, null);
			while (cursor.moveToNext()) {
				// map里边存放的是一行的值
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					// 列名及其对应的值
					row.put(cursor.getColumnName(i), cursor.getString(i));
				}
				uu.add(row);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();

			}
		}

		return uu;
	}

	// 查询数据的方法，查询出state是0的应用
	public List<Map<String, Object>> lo(Context context) {
		List<Map<String, Object>> uu = new ArrayList<Map<String, Object>>();
		shujuku db = new shujuku(context);
		SQLiteDatabase database = null;
		try {
			database = db.getReadableDatabase();
			// 查询后的结果返回的是一个游标,查寻出所有数据
			Cursor cursor = database.query("xinren",
					new String[] { "packageName" }, "state=?",
					new String[] { "0" }, null, null, null);
			while (cursor.moveToNext()) {
				// map里边存放的是一行的值
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					// 列名及其对应的值
					row.put(cursor.getColumnName(i), cursor.getString(i));
				}
				uu.add(row);
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();

			}
		}

		return uu;
	}

	// 查询出某一列的值
	public List<Map<String, Object>> ll(Context context) {
		shujuku db = new shujuku(context);
		List<Map<String, Object>> uu = new ArrayList<Map<String, Object>>();
		SQLiteDatabase database = null;
		try {
			database = db.getReadableDatabase();
			Cursor cursor = database.query("xinren", new String[] { "state" },
					null, null, null, null, null);
			while (cursor.moveToNext()) {
				// map里边存放的是一行的值
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					// 列名及其对应的值
					row.put(cursor.getColumnName(i), cursor.getString(i));
				}
				uu.add(row);
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (database != null) {
				database.close();

			}
		}

		return uu;

	}

	// 删除表中所有信息的方法
	public boolean deleteUser(Context context) {
		boolean flag = false;
		shujuku db = new shujuku(context);
		String sql = "delete from xinren";
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

	public boolean deleteUser(Object[] prams, Context context) {
		// TODO Auto-generated method stub
		shujuku db = new shujuku(context);
		boolean flag = false;
		String sql = "delete from xinren where packageName=?";
		SQLiteDatabase database = null;
		try {
			database = db.getWritableDatabase();
			database.execSQL(sql, prams);
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
