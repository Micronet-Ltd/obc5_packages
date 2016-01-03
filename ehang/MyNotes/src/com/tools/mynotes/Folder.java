package com.tools.mynotes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Folder {
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getColorId() {
		return colorId;
	}
	public void setColorId(int colorId) {
		this.colorId = colorId;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getFolderId() {
		return folderId;
	}
	
	private String folderId;
	private String name;
	private String date;
	private int colorId;
	
	public Folder (String folderId) {
		this.folderId = folderId;
	}
	
	/**
	 * select DB get folder info,return data like type of "folder name(number of Notes )"
	 * @param context
	 * @param folderId
	 * @return
	 */
	public String getFolderInfo(Context context, String folderId) {
		String folderInfo = null;
		String[] foldersID = {folderId};
		DBHelper dbHelper = new DBHelper(context);
	    SQLiteDatabase db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DBHelper.TABLE_FOLDERS, null, DBHelper.FIELD_ID + "=?", foldersID, null, null, null);
	    if(cursor.moveToNext()) {
	    	this.name = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_NAME));
	    	this.date = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DATE));
	    	this.colorId = cursor.getInt(cursor.getColumnIndex(DBHelper.FIELD_COLOR_ID));
	    }
	    cursor.close();
	    dbHelper.close();
	    if(name.indexOf("%") >-1){
	    	name = name.replaceAll("%", "%%");
	    }
	    folderInfo = String.format(name + " (%d) ", dbHelper.getNotesNum(folderId));
	    return folderInfo;
	}
}
