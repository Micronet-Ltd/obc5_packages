package com.ehang.dbutil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class dbImplement {
	
		boolean flag;
		public boolean addData(Object[] param, Context context) {
			String sql = "insert into TSwitch(state,packageName)values(?,?) order by state desc";
			dbHelper db = new dbHelper(context);
			SQLiteDatabase database = null;
			try {
				database = db.getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put("state", (Integer) param[0]);
				values.put("packageName", (String) param[1]);
				database.insert("TSwitch", null, values);
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
		public boolean updateData(Object[] param, Context context) {
			boolean flag = false;
			SQLiteDatabase database = null;
			String sql = "update TSwitch set state=? where packageName=?";
			dbHelper db = new dbHelper(context);
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
		
	
		public List<Map<String, Object>> queryPackageName(Context context) {
			List<Map<String, Object>> uu = new ArrayList<Map<String, Object>>();
			dbHelper db = new dbHelper(context);
			SQLiteDatabase database = null;
			try {
				database = db.getReadableDatabase();
				
				Cursor cursor = database.query("TSwitch",
						new String[] { "packageName" }, "state=?",
						new String[] { "1" }, null, null, null);
				while (cursor.moveToNext()) {
					
					Map<String, Object> row = new HashMap<String, Object>();
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						
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
		
		public List<Map<String, Object>> queryPackageNames(Context context) {
			List<Map<String, Object>> us = new ArrayList<Map<String, Object>>();
			dbHelper db = new dbHelper(context);
			SQLiteDatabase database = null;
			try {
				database = db.getReadableDatabase();
				
				Cursor cursor = database.query("TSwitch",
						new String[] { "packageName" }, "state=?",
						new String[] { "0" }, null, null, null);
				while (cursor.moveToNext()) {
					
					Map<String, Object> rows = new HashMap<String, Object>();
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						
						rows.put(cursor.getColumnName(i), cursor.getString(i));
					}
					us.add(rows);
				}
			} catch (Exception e) {
				// TODO: handle exception
			} finally {
				if (database != null) {
					database.close();

				}
			}

			return us;
		}
		
		public boolean deleteData(Object[] prams, Context context) {
		// TODO Auto-generated method stub
		dbHelper db = new dbHelper(context);
		boolean flag = false;
		String sql = "delete from TSwitch where packageName=?";
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
	
	public int getPackageEnable(Context context,String packageName) {
		dbHelper db = new dbHelper(context);
		SQLiteDatabase database= null;
		int state = 0;
		try{
		database = db.getReadableDatabase();
		Cursor c = database.rawQuery("select state from TSwitch where packageName=?",new String[]{packageName});  
        while (c.moveToNext()) {  
           state = c.getInt(c.getColumnIndex("state")); 
		   break;
        }  
        c.close();
		} catch(Exception e) {
			e.printStackTrace();
		}finally {
			if (database != null) {
				database.close();
			}
		}
	return state;
	}
		
	// 查询出状态值及包名
	public List<List<Map<String, Object>>> listAll(Context context) {
		List<List<Map<String, Object>>> lists = new ArrayList<List<Map<String, Object>>>();
		List<Map<String, Object>> uu = new ArrayList<Map<String, Object>>();// zt
		
		dbHelper db = new dbHelper(context);
		SQLiteDatabase database = null;
		try {
			database = db.getReadableDatabase();
			// 查询后的结果返回的是一个游标,查寻出所有数据
			Cursor cursor = database.query("TSwitch", null, null, null, null,
					null, null, null);
			while (cursor.moveToNext()) {
				Map<String, Object> uu2 = new HashMap<String, Object>();// zt
				int id = cursor.getInt(cursor.getColumnIndex("state"));
				uu2.put("state", id);
				uu.add(uu2);
			}
			lists.add(uu);

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
