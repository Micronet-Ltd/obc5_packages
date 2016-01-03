package com.tools.mynotes;

import com.tools.mynotes.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class NotesAddActivity extends Activity {
	private Intent intent = null;
	private String page = null;
	private ImageButton btnBack = null;
	private Button btnSure = null;
	private Button btnCancel = null;
	private EditText editDetails = null;
	private TextView txtParameter = null;
	private TextView txtTitle = null;
	public static final String PAGE_EDIT = "EditNote";
	public static final String PAGE_ADD = "AddNote";
	public static final String PAGE_CALL = "Incall";
	public static final String PAGE_NOTES = "Notes";
	public static final String PAGE_FOLDER = "Folders";
	/**
	 * get Page source
	 */
	public static final String PAGE_FROM = "Page";
	
	private static final String FOLDER_CALLING = "-1";		//"Calling" folder
	private static final String FOLDER_OTHERS = "0";		//"Others" folder
	
	private String folderId = "0";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_note);
		intent = this.getIntent();
		page = intent.getStringExtra(PAGE_FROM);
		folderId = intent.getStringExtra(FoldersListActivity.FOLDER_ID);
		editDetails = (EditText)findViewById(R.id.editDetails);
		txtParameter = (TextView)findViewById(R.id.txtParameter);
		txtTitle = (TextView)findViewById(R.id.txtTitle);
		txtTitle.setText(getResources().getString(R.string.new_note));
		btnSure = (Button)findViewById(R.id.btnSure);
		btnCancel = (Button)findViewById(R.id.btnCancel);
		btnSure.setOnClickListener(new DoneListenr());
		btnCancel.setOnClickListener(new CancelListenr());
		
		if (PAGE_EDIT.equals(page)) {
			//show base info 
			txtParameter.setText(intent.getStringExtra("noteId"));
			txtTitle.setText(getResources().getString(R.string.edit_note));
			showNote(txtParameter.getText().toString());
		}	
		btnBack = (ImageButton)findViewById(R.id.imgBtnBack);
		btnBack.setOnClickListener(new CancelListenr());
		MyApplication.getInstance().addActivity(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		editDetails.requestFocus();
	}
	
	class DoneListenr implements OnClickListener {
    	public void onClick(View v) {
    		// judge input in note
    		if (!"".equals(editDetails.getText().toString())) {
	    		DBHelper dbHelper = new DBHelper(NotesAddActivity.this);
	    		String details = editDetails.getText().toString();
				if (PAGE_EDIT.equals(page)) {
					//edit note 
		    		dbHelper.updateNote(details, folderId, txtParameter.getText().toString());
		    		//Return to the specified folder
					Intent intentTo = new Intent(NotesAddActivity.this, NotesListActivity.class);
					intentTo.putExtra(FoldersListActivity.FOLDER_ID, folderId);
		    		startActivity(intentTo);
				} else {
					//increase new note
		    		if (PAGE_CALL.equals(page)) {
		    			folderId = FOLDER_CALLING;
			    		dbHelper.insertNote(details, folderId);
		    			//Return to call interface
//		    			finish();
			    		MyApplication.getInstance().exit();
		    		} else if (PAGE_FOLDER.equals(page)) {
		    			folderId = FOLDER_OTHERS;
		    			dbHelper.insertNote(details, folderId);
		        		startActivity(new Intent(NotesAddActivity.this, FoldersListActivity.class));
	    			} else {
		    			dbHelper.insertNote(details, folderId);
		    			//Jump to the specified folder,display Notes list
			    		Intent intentTo = new Intent(NotesAddActivity.this, NotesListActivity.class);
			    		intentTo.putExtra(FoldersListActivity.FOLDER_ID, folderId);
			        	startActivity(intentTo);
		    		}
				}
    		} else {
    			finish();
    		}
    	}
    }
	
	class CancelListenr implements OnClickListener {
    	public void onClick(View v) {
    		if (PAGE_CALL.equals(page)) {
				//Jump to the dial-up interface
    			MyApplication.getInstance().exit();
			} else if (PAGE_FOLDER.equals(page)) {
				//Jump to Folders interface
				startActivity(new Intent(NotesAddActivity.this, FoldersListActivity.class));
			}
    		else {
				//Jump to Folders Notes list
				Intent intentTo = new Intent(NotesAddActivity.this, NotesListActivity.class);
				intentTo.putExtra(FoldersListActivity.FOLDER_ID, folderId);
        		startActivity(intentTo);
			}
    	}
    }
	
	private void showNote(String id) {
		String[] noteId = {id};
		DBHelper dbHelper = new DBHelper(NotesAddActivity.this);
	    SQLiteDatabase db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DBHelper.TABLE_NOTES, null, DBHelper.FIELD_ID + "=?", noteId, null, null, null);
	    if (cursor.moveToFirst()) {
	    	editDetails.setText(cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DETAILS)));
        }
		cursor.close();
		db.close();
	}

//add this to fix HQ00745735 zhongqijiang start
@Override	
public void onBackPressed() {

	super.onBackPressed();
	if(PAGE_CALL.equals(page)){
		MyApplication.getInstance().exit();
	}
}

//add this to fix HQ00745735 zhongqijiang end

	
}
