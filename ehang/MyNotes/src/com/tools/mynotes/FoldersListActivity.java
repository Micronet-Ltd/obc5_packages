package com.tools.mynotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import com.tools.mynotes.R;

public class FoldersListActivity extends Activity {
	ImageSwitcher imgSwitch = null;
	ImageButton btnAdd = null;
	private TextView txtParameter = null;
	public static final String FOLDER_ID = "folderId";
	public static final String IMAGE_ID = "imageId";
	public static final String FOLDER_NAME = "folderName";
	//modify by wenjs for del FOLDER_CALL option begin
	public static final String FOLDER_ADD_FOLDER = "-1";
	public static final String FOLDER_CALL = "-2";
	//modify by wenjs for del FOLDER_CALL option end
	public static final String FOLDER_OTHERS = "0";
	private static final int ITEM_EDIT_NAME = Menu.FIRST;
	private static final int ITEM_DELETE_FOLDER = Menu.FIRST + 1;
	private static final String TAG = "FoldersListActivity";
	
	/* Can be used when the file of pictures */
	int[] imagesId = new int[] {
		R.drawable.folder_01, R.drawable.folder_02, 
		R.drawable.folder_03, R.drawable.folder_04
	};
	/*  Three fixed folder: new folder, call logs folder, other folders */
	//modify by wenjs for del FOLDER_CALL option begin
	String[] specialFolderId = new String[] {
		FOLDER_ADD_FOLDER, FOLDER_OTHERS
	};
	int[] specialFolderName = new int[] {
		R.string.add_folder,
		//R.string.folder_call,
		R.string.folder_others
	};
	int[] specialFolderColor = new int[] {
		R.drawable.folder_add_normal,
		//R.drawable.folder_01,
		R.drawable.folder_others
	};
	//modify by wenjs for del FOLDER_CALL option end
	
	private GridView gridFolders = null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.folders_main);
		txtParameter = (TextView)findViewById(R.id.txtParameter);
		gridFolders = (GridView) findViewById(R.id.gridFolders);
		btnAdd = (ImageButton)findViewById(R.id.imgBtnAdd);
		btnAdd.setOnClickListener(new AddNoteListenr());
		gridViewFoldersBind();
		gridFolders.setOnItemClickListener(new FolderClickListener());
		gridFolders.setOnItemLongClickListener(new FolderLongClickListener());
		registerForContextMenu(gridFolders);
		MyApplication.getInstance().addActivity(this);
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View source, ContextMenu.ContextMenuInfo menuInfo) {
		String folderId = txtParameter.getText().toString();
		if (FOLDER_ADD_FOLDER.equals(folderId) 
				|| FOLDER_CALL.equals(folderId) 
				|| FOLDER_OTHERS.equals(folderId)) {
			return;
		} else {
			menu.add(0, ITEM_EDIT_NAME, 0, getResources().getString(R.string.edit_folder_name));
	        menu.add(0, ITEM_DELETE_FOLDER, 0, getResources().getString(R.string.delete));
	        menu.setHeaderTitle(getResources().getString(R.string.folder_option));
		}
	}
	
	@Override
    public boolean onContextItemSelected(MenuItem item) {
		int selectedPosition = ((AdapterContextMenuInfo)item.getMenuInfo()).position;
		@SuppressWarnings("unchecked")
		HashMap<String, Object> map = (HashMap<String, Object>)gridFolders.getItemAtPosition(selectedPosition);
		txtParameter.setText(map.get(FOLDER_ID).toString());
        switch (item.getItemId()) {
        	case ITEM_EDIT_NAME:
        		editFolderName();
        		break;
        	case ITEM_DELETE_FOLDER:
        		showDeleteConfirm();
        		break;
        	default:
        		break;
        }
        return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
			case KeyEvent.KEYCODE_BACK:
				MyApplication.getInstance().exit();
	        	return true;
			default:
				break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void gridViewFoldersBind() {
		String id = null; //folder id
		DBHelper dbHelper = new DBHelper(FoldersListActivity.this);
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> item = new HashMap<String, Object>();
		for (int i = 0; i < specialFolderId.length; i++) {
			item = new HashMap<String, Object>();
			item.put(FOLDER_ID, specialFolderId[i]);
			item.put(IMAGE_ID, specialFolderColor[i]);
			if (i == 0) {
				item.put(FOLDER_NAME, getResources().getString(specialFolderName[i]));
			} else {
				item.put(FOLDER_NAME, String.format("%s (%d)", getResources().getString(specialFolderName[i]), dbHelper.getNotesNum(specialFolderId[i])));
			}
	    	listItem.add(item);
		}
	    SQLiteDatabase db = dbHelper.getReadableDatabase();
	    Cursor cursor = db.query(DBHelper.TABLE_FOLDERS, null, null, null, null, null, null);
	    while(cursor.moveToNext()) {
	    	item = new HashMap<String, Object>();
	    	id = cursor.getString(cursor.getColumnIndex(DBHelper.FIELD_ID));
	    	item.put(FOLDER_ID, id);
	    	item.put(IMAGE_ID, imagesId[cursor.getInt(cursor.getColumnIndex(DBHelper.FIELD_COLOR_ID))]);
	    	item.put(FOLDER_NAME, new Folder(id).getFolderInfo(FoldersListActivity.this, id));
	    	listItem.add(item);
	    }
	    cursor.close();
	    dbHelper.close();
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem,
	    		R.layout.list_folder,
	    		new String[] {IMAGE_ID, FOLDER_NAME},    
	    		new int[] {R.id.imgFolder1, R.id.txtFolder1}   
	    		);
        
		gridFolders.setAdapter(listItemAdapter);
//		gridFolders.setSelector(this.getResources().getDrawable(R.drawable.folder_01));
	}
	
	class AddNoteListenr implements OnClickListener {
    	public void onClick(View v) {
    		Intent intentTo = new Intent(FoldersListActivity.this, NotesAddActivity.class);
    		intentTo.putExtra("Page", "Folders");
            startActivity(intentTo);
    	}
    }
	
	class FolderClickListener implements AdapterView.OnItemClickListener {
    	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    		@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>)arg0.getItemAtPosition(arg2);
			if (FOLDER_ADD_FOLDER.equals(map.get(FOLDER_ID).toString())) {
				//show add new file dialog
				final EditText editName = new EditText(FoldersListActivity.this);
				setInputStyle(editName);
				new CustomDialog.Builder(FoldersListActivity.this)
				.setTitle(getResources().getString(R.string.add_folder))
				.setCancelable(true)
				.setContentView(editName)
				.setPositiveButton(getResources().getString(R.string.confirm_done), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (!"".equals(editName.getText().toString())) {
							DBHelper dbHelper = new DBHelper(FoldersListActivity.this);
							dbHelper.insertFolder(editName.getText().toString(), 
									new Random().nextInt(imagesId.length));
							gridViewFoldersBind();
						}
						return;
					}
		         })  
				.setNegativeButton(getResources().getString(R.string.confirm_cancel), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
				})  
				.show();
			} else {
	    		Intent intent = new Intent(FoldersListActivity.this, NotesListActivity.class);
	    		intent.putExtra(FOLDER_ID, map.get(FOLDER_ID).toString());
	    		intent.putExtra(FOLDER_NAME, map.get(FOLDER_NAME).toString());
	    		startActivity(intent);
			}
			Log.d(TAG, map.get(FOLDER_ID).toString() + map.get(FOLDER_NAME).toString());
    		return;
    	}
    }
	
	class FolderLongClickListener implements AdapterView.OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>)arg0.getItemAtPosition(arg2);
			txtParameter.setText(map.get(FOLDER_ID).toString());
			return false;
		}
	}
	
	private void editFolderName() {
		//show edit folder name dialog 
		Folder folder = new Folder(txtParameter.getText().toString());
		folder.getFolderInfo(FoldersListActivity.this, txtParameter.getText().toString());
		final EditText editName = new EditText(FoldersListActivity.this);
		setInputStyle(editName);
		//modify for fix HQ00731196 &  HQ00735120 zhongqijiang
		editName.setText(folder.getName().replaceAll("%%", "%"));
		
		new CustomDialog.Builder(FoldersListActivity.this)  
		.setTitle(getResources().getString(R.string.edit_folder_name))  
		.setCancelable(false)
		.setContentView(editName)
		.setPositiveButton(getResources().getString(R.string.confirm_done), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (!"".equals(editName.getText().toString())) {
					DBHelper dbHelper = new DBHelper(FoldersListActivity.this);
					dbHelper.updateFolder(txtParameter.getText().toString(), editName.getText().toString());
					gridViewFoldersBind();
				}
				return;
			}
         })  
		.setNegativeButton(getResources().getString(R.string.confirm_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		})
		.show();
	}
	
	private void showDeleteConfirm() {
		new CustomDialog.Builder(FoldersListActivity.this)
		.setTitle(R.string.app_name)
		.setMessage(R.string.delete_folder_warning)
		.setIcon(R.drawable.add_note_normal)
		.setCancelable(false)
		.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialoginterface, int i) {
            	   DBHelper dbHelper = new DBHelper(FoldersListActivity.this);
            	   dbHelper.deleteFolder(txtParameter.getText().toString());
            	   gridViewFoldersBind();
               }
            })
        .setNegativeButton(R.string.confirm_cancel, new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialoginterface, int i) {   
            	   return;
               }
            })
		.show();
	}
	
	/**
	 * Set the style of input on dialog
	 * @param editText
	 */
    private void setInputStyle(EditText editText) {
        editText.setBackgroundResource(R.drawable.input);
        //		editName.setBackgroundColor(android.graphics.Color.GRAY);
        editText.setWidth(200);
        editText.setMaxHeight(85);
        editText.setTextColor(android.graphics.Color.WHITE);
        editText.setTextSize(18);
        editText.setSingleLine();
        editText.setCursorVisible(true);
        editText.requestFocus();

        new Timer().schedule(new TimerTask() { 
            @Override
            public void run() {
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 500);
    }
}
