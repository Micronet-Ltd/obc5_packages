package com.security.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;

public class UserDaoImpl {
	boolean flag = false;

	// 添加信息或者往数据中写入信息
	public boolean addUser(Object[] param, Context context) {
		String sql = "insert into user(state,packageName)values(?,?)";
		// 获取sqliteDatabase对象，并且操作数据库
		// 获取数据库对象
		DBUtil db = new DBUtil(context);
		SQLiteDatabase database = null;
		try {
			database = db.getWritableDatabase();
			// 利用contentValues来存放参数
			ContentValues values = new ContentValues();
			values.put("state", (Integer) param[0]);
			values.put("packageName", (String) param[1]);
			// 调用Android自己的插入的方法
			database.insert("user", null, values);
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

	// 查询数据的方法，查询出state是1的应用
	public List<Map<String, Object>> list(Context context) {
		List<Map<String, Object>> uu = new ArrayList<Map<String, Object>>();
		DBUtil db = new DBUtil(context);
		SQLiteDatabase database = null;
		try {
			database = db.getReadableDatabase();
			// 查询后的结果返回的是一个游标,查寻出所有数据
			Cursor cursor = database.query("user",
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
			// 执行完之后要关闭database
			if (database != null) {
				database.close();
			}
		}

		return uu;
	}

	// 查询数据的方法，查询出state是3的应用
	public List<Map<String, Object>> list2(Context context) {
		List<Map<String, Object>> uu = new ArrayList<Map<String, Object>>();
		DBUtil db = new DBUtil(context);
		SQLiteDatabase database = null;
		try {
			database = db.getReadableDatabase();
			// 查询后的结果返回的是一个游标,查寻出所有数据
			Cursor cursor = database.query("user",
					new String[] { "packageName" }, "state=?",
					new String[] { "3" }, null, null, null);
			while (cursor.moveToNext()) {
				// map里边存放的是一行的值
				Map<String, Object> row = new HashMap<String, Object>();
				for (int i = 0; i < cursor.getColumnCount(); i++) {
					// 获取state值是3的。
					row.put(cursor.getColumnName(i), cursor.getString(i));
				}
				uu.add(row);
			}

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			// 执行完之后要关闭database
			if (database != null) {
				database.close();
			}
		}

		return uu;
	}

	// 删除表中所有信息的方法
	public boolean deleteUser(Context context) {
		boolean flag = false;
		DBUtil db = new DBUtil(context);
		String sql = "delete from user";
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
		DBUtil db = new DBUtil(context);
		boolean flag = false;
		String sql = "delete from user where packageName=?";
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

	// 查询出状态值及包名
	public List<List<Map<String, Object>>> listall(Context context) {
		List<List<Map<String, Object>>> lists = new ArrayList<List<Map<String, Object>>>();
		List<Map<String, Object>> uu = new ArrayList<Map<String, Object>>();// zt
		List<Map<String, Object>> pkg = new ArrayList<Map<String, Object>>();// pkg
		DBUtil db = new DBUtil(context);
		SQLiteDatabase database = null;
		try {
			database = db.getReadableDatabase();
			// 查询后的结果返回的是一个游标,查寻出所有数据
			Cursor cursor = database.query("user", null, null, null, null,
					null, null, null);
			while (cursor.moveToNext()) {
				Map<String, Object> uu2 = new HashMap<String, Object>();// zt
				Map<String, Object> pkg2 = new HashMap<String, Object>();// pkg

				int id = cursor.getInt(cursor.getColumnIndex("state"));
				String name = cursor.getString(cursor
						.getColumnIndex("packageName"));

				// Log.e("main","name= "+name+" id= "+id);

				uu2.put("state", id);
				pkg2.put("pkg", name);

				uu.add(uu2);
				pkg.add(pkg2);

				/*
				 * //map里边存放的是一行的值 Map<String, Object> row=new HashMap<String,
				 * Object>(); for (int i = 0; i < cursor.getColumnCount(); i++)
				 * { //列名及其对应的值 row.put(cursor.getColumnName(i),
				 * cursor.getString(i)); } uu.add(row);
				 */
			}

			lists.add(uu);
			lists.add(pkg);

		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			// 执行完之后要关闭database
			if (database != null) {
				database.close();
			}
		}

		return lists;
	}

}
