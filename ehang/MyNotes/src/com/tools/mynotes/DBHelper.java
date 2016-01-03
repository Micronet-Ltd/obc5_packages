package com.tools.mynotes;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	/*	Database	*/
	private static final String DATABASE_NAME = "note.db";
    private static final int DATABASE_VERSION = 1;
    /*	Tables	*/
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_FOLDERS = "folders";
    /*	Columns	*/
    public static final String FIELD_ID = "_id";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_DETAILS = "details";
    public static final String FIELD_FOLDER_ID = "folder_id";
    public static final String FIELD_COLOR_ID = "color_id";
    
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
	
	public DBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		//Create Table 'files'
    	String sql = String.format("CREATE TABLE %s " +
    						"(%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER, %s INTEGER)"
    						,TABLE_NOTES
    						,FIELD_ID
    						,FIELD_DETAILS
    						,FIELD_DATE
    						,FIELD_FOLDER_ID);
        db.execSQL(sql);
        //Create Table 'folders'
    	sql = String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER, %s INTEGER)"
		    				,TABLE_FOLDERS
		        			,FIELD_ID
		        			,FIELD_NAME
		        			,FIELD_DATE
		        			,FIELD_COLOR_ID);
    	db.execSQL(sql);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = " DROP TABLE IF EXISTS " + TABLE_NOTES;
        db.execSQL(sql);
        sql = " DROP TABLE IF EXISTS " + TABLE_FOLDERS;
        db.execSQL(sql);
        onCreate(db);
	}
	
	public void insertNote(String details, String folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
    	values.put(FIELD_DETAILS, details);
    	values.put(FIELD_FOLDER_ID, folderId);
    	values.put(FIELD_DATE, System.currentTimeMillis());
        db.insert(TABLE_NOTES, null, values);
        db.close();
    }
	
	public void insertFolder(String name, int colorId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
    	values.put(FIELD_NAME, name);
    	values.put(FIELD_DATE, System.currentTimeMillis());
    	values.put(FIELD_COLOR_ID, colorId);
        db.insert(TABLE_FOLDERS, null, values);
        db.close();
    }
	
	public void updateNote(String details, String folderId, String noteId) {
		SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
    	values.put(FIELD_DETAILS, details);
    	values.put(FIELD_FOLDER_ID, folderId);
    	values.put(FIELD_DATE, System.currentTimeMillis());
    	String[] id = {noteId};
        db.update(TABLE_NOTES, values, FIELD_ID + "=?", id);
        db.close();
	}
	
	public void updateFolder(String folderId, String folderName) {
		SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
    	values.put(FIELD_NAME, folderName);
    	values.put(FIELD_DATE, System.currentTimeMillis());
    	String[] id = {folderId};
        db.update(TABLE_FOLDERS, values, FIELD_ID + "=?", id);
        db.close();
	}
	
	public void deleteNote(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = FIELD_ID + "=?";
        String[] whereValue = {id};//{Integer.toString(id)};
        db.delete(TABLE_NOTES, where, whereValue);
        db.close();
    }
	
	public void deleteFolder(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = FIELD_ID + "=?";
        String[] whereValue = {id};//{Integer.toString(id)};
        db.delete(TABLE_FOLDERS, where, whereValue);
        //delete notes of folder 
        where = FIELD_FOLDER_ID + "=?";
        db.delete(TABLE_NOTES, where, whereValue);
        db.close();
        
    }
	
	public int getNotesNum(String folderId) {
		int num = 0;
		String[] foldersID = {folderId};
	    SQLiteDatabase db = this.getReadableDatabase();
	    Cursor cursor = db.query(DBHelper.TABLE_NOTES, null, DBHelper.FIELD_FOLDER_ID + "=?", foldersID, null, null, null);
	    num = cursor.getCount();
	    cursor.close();
	    return num;
	}
}
