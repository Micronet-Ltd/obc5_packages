package com.yihang.gesture.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class gestureProvider extends ContentProvider {
	
	private dbHelper dbhelper;
	private SQLiteDatabase db; 
	private static String AUTHORITY = "com.yihang.gesture.provider";
	private String TABLENAME = "switchstate";
	public static final Uri GESTURE_URI = Uri.parse("content://"+AUTHORITY+"/switchstate");
    private String TAG = "gestureProvider";
  
	@Override
	public boolean onCreate() {
		dbhelper = new dbHelper(this.getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrders) {
			Log.e(TAG, "query  uri = " + uri);
			db = dbhelper.getWritableDatabase();             
	        Cursor c;   
	        if(uri.equals(GESTURE_URI)) {   
	            c = db.query(TABLENAME, projection, selection, selectionArgs, null, null, null);      
	        }else {  
	            Log.e(TAG, "query  ====error uri==== " + uri);   
	            throw new IllegalArgumentException("Unknown URI"+uri);   
	        }   
	        c.setNotificationUri(getContext().getContentResolver(), uri);   
	        return c;  
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}
}
