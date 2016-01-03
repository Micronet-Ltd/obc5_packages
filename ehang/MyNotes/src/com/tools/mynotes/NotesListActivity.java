package com.tools.mynotes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.tools.mynotes.R;

import android.app.Activity;
//import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class NotesListActivity extends Activity {
	private ListView listNotes = null;
	private TextView txtParameter = null;
	private TextView txtTitle = null;
	private ImageButton btnAddNote = null;
	private ImageButton btnBack = null;
	private ImageButton btnDelete = null;
	private ImageView imgDelDisable = null;
	private String folderId = "0";
	private static boolean deleteState = false;
	public static final int ITEM_NEW_NOTE = Menu.FIRST;
	public static final int ITEM_DELETE = Menu.FIRST + 1;
	public static final int ITEM_NEW_FOLDER = Menu.FIRST + 2;
	public static final int ITEM_MOVE = Menu.FIRST + 3;
	public static final int ITEM_DELETE_NOTE = Menu.FIRST + 4;
	public static final int ITEM_COPY_NOTE = Menu.FIRST + 5;
	
	public static final String ID = "ID"; 
	public static final String DETAILS = "details";
	public static final String DATE = "date";
	public static final String TYPE_EDIT = "edit_note";
	public static final String NOTE_ID = "noteId";
	
	public static final String TAG = "NotesListActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.notes_main);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        txtParameter = (TextView)findViewById(R.id.txtParameter);
        txtTitle = (TextView)findViewById(R.id.txtTitle);
        folderId = this.getIntent().getStringExtra(FoldersListActivity.FOLDER_ID);        
        listNotes = (ListView)findViewById(R.id.listViewNotes);
        listViewNotesBind(listNotes, R.layout.list_note);
        listNotes.setOnItemClickListener(new NoteClickListenr());
        registerForContextMenu(listNotes);
        btnAddNote = (ImageButton)findViewById(R.id.imgBtnAdd);
        btnBack = (ImageButton)findViewById(R.id.imgBtnBack);
        btnDelete = (ImageButton)findViewById(R.id.imgBtnDelete);
        imgDelDisable = (ImageView)findViewById(R.id.imgDeleteDisable);
        
        btnBack.setOnClickListener(new BackNoteListenr());
        btnAddNote.setOnClickListener(new AddNoteListenr());
        btnDelete.setOnClickListener(new DeleteNoteListenr());
        MyApplication.getInstance().addActivity(this);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, ITEM_NEW_NOTE, 0, getResources().getString(R.string.add_note));
        menu.add(0, ITEM_DELETE, 0, getResources().getString(R.string.delete));
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case ITEM_NEW_NOTE:
            Intent intentTo = new Intent(NotesListActivity.this, NotesAddActivity.class);
            intentTo.putExtra("Page", NotesAddActivity.PAGE_ADD);
            intentTo.putExtra(FoldersListActivity.FOLDER_ID, folderId);
            startActivity(intentTo);
            break;
        case ITEM_DELETE:
        	if (listNotes.getCount() == 0) {
        		showToast(R.string.no_selected);
        		break;
        	} else {
	    		enableDelete(true);
	    		listViewNotesBind(listNotes);
	        	break; 
        	}
        case ITEM_NEW_FOLDER:
            break;
        case ITEM_MOVE:
            break;
        }
        return super.onMenuItemSelected(featureId, item);
    }
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View source, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, ITEM_DELETE_NOTE, 0, getResources().getString(R.string.delete));
        menu.add(0, ITEM_COPY_NOTE, 0, getResources().getString(R.string.copy_note));
        menu.setHeaderTitle(getResources().getString(R.string.note_option));
	}
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
		int selectedPosition = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>)listNotes.getItemAtPosition(selectedPosition);
        switch (item.getItemId()) {
        	case ITEM_DELETE_NOTE:
        		txtParameter.setText(map.get(ID).toString());
        		listViewNotesBind(listNotes, R.layout.list_note);
        		showDeleteConfirm(getResources().getString(R.string.delete_warning));
        		break;
        	case ITEM_COPY_NOTE:
        		Util.copy(map.get(DETAILS).toString(), NotesListActivity.this);
        		break;
        }
        return true;
	}
	
	@Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
			case KeyEvent.KEYCODE_BACK:
				if (deleteState) {
	        		enableDelete(false);
	        		listViewNotesBind(listNotes, R.layout.list_note);
	        		return true;
	        	} else {
	        		startActivity(new Intent(NotesListActivity.this, FoldersListActivity.class));
	        		break;
	        	}
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	 }

    class NoteClickListenr implements AdapterView.OnItemClickListener {
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    		@SuppressWarnings("unchecked")
    		HashMap<String, Object> map = (HashMap<String, Object>)arg0.getItemAtPosition(arg2);
    		Intent intent = new Intent(NotesListActivity.this, NotesAddActivity.class);
    		intent.putExtra(NOTE_ID, map.get(ID).toString());
    		intent.putExtra("Page", NotesAddActivity.PAGE_EDIT);
    		intent.putExtra(FoldersListActivity.FOLDER_ID, folderId);
    		startActivity(intent);
    		return;
    	}
    }
	
    /**
     * delete dialog
     */
    private void showDeleteConfirm(String msg) {
    	new CustomDialog.Builder(NotesListActivity.this)
        .setTitle(R.string.app_name)
        .setCancelable(false)//Cancel the function block button
        .setMessage(msg)
        .setIcon(R.drawable.icon_warning)
        .setPositiveButton(R.string.delete,
            new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialoginterface, int i) {
            	   String[] args = txtParameter.getText().toString().split(",");
            	   for (String s : args) {
	            	   //delete
	            	   DBHelper dbHelper = new DBHelper(NotesListActivity.this);
	            	   dbHelper.deleteNote(s);
            	   }
            	   listViewNotesBind(listNotes, R.layout.list_note);
            	   enableDelete(false);
               }
            })
         .setNegativeButton(getResources().getString(R.string.confirm_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
         })
        .show();
	}
    
    class BackNoteListenr implements OnClickListener {
    	@Override
		public void onClick(View v) {
			startActivity(new Intent(NotesListActivity.this, FoldersListActivity.class));
		}
    }
    
    class AddNoteListenr implements OnClickListener {
    	public void onClick(View v) {
    		Intent intentTo = new Intent(NotesListActivity.this, NotesAddActivity.class);
    		intentTo.putExtra("Page", NotesAddActivity.PAGE_NOTES);
    		intentTo.putExtra(FoldersListActivity.FOLDER_ID, folderId);
            startActivity(intentTo);
    	}
    }
    
    class DeleteNoteListenr implements OnClickListener {
    	public void onClick(View v) {
    		txtParameter.setText("");
    		for (int i = 0; i < listNotes.getCount(); i++) {
    			if (NotesListAdapter.isSelected.get(i)) {
    				@SuppressWarnings("unchecked")
					HashMap<String, Object> item = (HashMap<String, Object>) listNotes.getAdapter().getItem(i);
    				txtParameter.setText(txtParameter.getText() + "," + item.get(ID).toString());
    				Log.d(TAG, txtParameter.getText().toString());
    			}
    		}
    		if (!"".equals(txtParameter.getText())) {
    			int selectedCount = txtParameter.getText().toString().split(",").length - 1;
    			if (selectedCount == 1) {
    				showDeleteConfirm(getResources().getString(R.string.delete_one_warning));    				
    			} else {
    				showDeleteConfirm(selectedCount+ " " + getResources().getString(R.string.delete_some_warning));
    			}
    		} else {
    			showToast(R.string.no_selected);
    		}
    	}
    }    
    
	private void listViewNotesBind(ListView listView, int layout) {
		String[] foldersID = {folderId};
	    DBHelper dbHelper = new DBHelper(NotesListActivity.this);
	    SQLiteDatabase db = dbHelper.getReadableDatabase();
	    
	    Cursor cursor = db.query(DBHelper.TABLE_NOTES, null, DBHelper.FIELD_FOLDER_ID + "=?", foldersID, null, null, DBHelper.FIELD_DATE + " DESC");
	    ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
            boolean is24HourMode = Util.get24HourMode(NotesListActivity.this);
	    while(cursor.moveToNext()) {
	    	HashMap<String, Object> item = new HashMap<String, Object>();
	    	item.put(ID, cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_ID)));
	    	item.put(DETAILS, cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DETAILS)));
	    	Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DATE))));
	   // if(FeatureOption.HQ_AW849_LAVA_MYNOTE_DATE_FORMAT)	{
//	    	 item.put(DATE, android.text.format.DateFormat.getDateFormat(this).format(date)+" "+android.text.format.DateFormat.getTimeFormat(this).format(date));
	    	item.put(DATE, Util.convertDate(NotesListActivity.this, date ,is24HourMode));
	    	//}else{
		 // item.put(DATE, Util.convertDate(date));
	    	//}
			listItem.add(item);
	    }
	    cursor.close();
	    dbHelper.close();
	    SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,
	    		layout,
	    		new String[] {DETAILS, DATE},    
	    		new int[] {R.id.txtDetails, R.id.txtDate}   
	    		);
	    listView.setAdapter(listItemAdapter);
	    if (FoldersListActivity.FOLDER_CALL.equals(folderId)) {
	    	txtTitle.setText(getResources().getString(R.string.folder_call)+"("+ dbHelper.getNotesNum(folderId) +")");
	    } else if (FoldersListActivity.FOLDER_OTHERS.equals(folderId)) {
	    	txtTitle.setText(getResources().getString(R.string.folder_others)+"("+ dbHelper.getNotesNum(folderId) +")");
	    } else {
	    	txtTitle.setText(new Folder(folderId).getFolderInfo(NotesListActivity.this, folderId));
	    }
	}
	
	private void listViewNotesBind(ListView listView) {
		String[] foldersID = {folderId};
		DBHelper dbHelper = new DBHelper(NotesListActivity.this);
	    SQLiteDatabase db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DBHelper.TABLE_NOTES, null, DBHelper.FIELD_FOLDER_ID + "=?", foldersID, null, null, DBHelper.FIELD_DATE + " DESC");
	    ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
            boolean is24HourMode = Util.get24HourMode(NotesListActivity.this);
	    while(cursor.moveToNext()) {
	    	HashMap<String, Object> item = new HashMap<String, Object>();
	    	item.put(ID, cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_ID)));
	    	item.put(DETAILS, cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DETAILS)));
	    	Date date = new Date(Long.parseLong(cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_DATE))));
	   
	  //  if(FeatureOption.HQ_AW849_LAVA_MYNOTE_DATE_FORMAT)
	   // {
	    	item.put(DATE, Util.convertDate(NotesListActivity.this, date ,is24HourMode));
//	    item.put(DATE, android.text.format.DateFormat.getDateFormat(this).format(date)+" "+android.text.format.DateFormat.getTimeFormat(this).format(date));
	   // }else{
	    	// item.put(DATE, Util.convertDate(date));
	    	//}
			listItem.add(item);
	    }
	    cursor.close();
	    dbHelper.close();
		NotesListAdapter adapter = new NotesListAdapter(this, listItem);
		listView.setAdapter(adapter);
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}
	
	/**
	 * 
	 * @param flag
	 */
	private void enableDelete(boolean flag) {
		if (flag) {
			btnDelete.setVisibility(View.VISIBLE);
    		imgDelDisable.setVisibility(View.GONE);
    		deleteState = true;
    		
		} else {
			deleteState = false;
			btnDelete.setVisibility(View.GONE);
    		imgDelDisable.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * Toast display
	 * @param str
	 */
	public void showToast(int strId) {
    	Toast.makeText(NotesListActivity.this, getResources().getString(strId), Toast.LENGTH_SHORT).show();
    }
}
