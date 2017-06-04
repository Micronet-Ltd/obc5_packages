package com.yihang.gesture.activity;

import java.util.ArrayList;
import java.util.HashMap;

import com.yihang.gesture.R;
import com.yihang.gesture.provider.Profile;
import com.yihang.gesture.service.ServerManager;
import com.yihang.gesture.service.ExitApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.Window;

public class MainActivity extends Activity {
	private String TAG = "MainActivity";
	
	private Profile profile;
	
	private ToggleButton mTogglebtnGlove;
	private ToggleButton mTogglebtn;
	private ArrayList<HashMap<String, Object>> listData;
	private TextView mTextView;
	private TextView mTextViewGlove; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mTogglebtn = (ToggleButton)findViewById(R.id.mToggleBtn);
		ListView mListview = (ListView)findViewById(R.id.mListView);
		mTextView = (TextView) findViewById(R.id.tv_titleblackGesture);
		mTextViewGlove = (TextView) findViewById(R.id.tv_titleglove);
		profile = Profile.getInstance(getApplicationContext());
		int switchstate = profile.getSwitchstate();
		
		if(switchstate == 1) {
			mTogglebtn.setChecked(true);
			mTextView.setText(R.string.open);
			
		}else {
			mTogglebtn.setChecked(false);
			mTextView.setText(R.string.close);
		}
		Log.e(TAG, "switchState = " + profile.getSwitchstate());
		
		mTogglebtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
			@Override
			public void onCheckedChanged(CompoundButton buttonview, boolean isChecked) {
				if(isChecked) {
					profile.updateSwitchstate(1);
					Intent service = new Intent(getBaseContext(), com.yihang.gesture.service.GestureService.class);
					getBaseContext().startService(service);
					ServerManager.getInstance().Opentp();
					Log.d(TAG, "switchState = " + profile.getSwitchstate()+",ServerManager.Open_tp");
					
				}else {
					profile.updateSwitchstate(0);
					Intent service = new Intent(getBaseContext(), com.yihang.gesture.service.GestureService.class);
					getBaseContext().stopService(service);
					ServerManager.getInstance().Closetp();
					Log.d(TAG, "switchState = " + profile.getSwitchstate()+",ServerManager.Close_tp");
				}
			} 
		}); 
		
		
		/*mListview.setOnItemClickListener(new OnItemClickListener() {  
			@Override
			public void onItemClick(AdapterView<?> adapterview, View view,
					int position, long id) {
				if(position == 0){
					Log.e(TAG, "=======camera is selected======");
				}
				else if(position == 1){
					Log.e(TAG, "=======lightscreen is selected======");
				}
			}  
	    }); */ 
		
		mTogglebtnGlove = (ToggleButton)findViewById(R.id.mToggleBtn_glove);
		
		int switchstateGlove = profile.getGloveSwitchstate();
		
		if(switchstateGlove == 1) {
			mTogglebtnGlove.setChecked(true);
			mTextViewGlove.setText(R.string.open);
		}else {
			mTogglebtnGlove.setChecked(false);
			mTextViewGlove.setText(R.string.close);
		}
		
		mTogglebtnGlove.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
			@Override
			public void onCheckedChanged(CompoundButton buttonview, boolean isChecked) {
				if(isChecked) {
					profile.updateGloveSwitchstate(1);
					ServerManager.getInstance().OpenGloveMode();
					Log.d(TAG, "OpenGloveMode");
					
				}else {
					profile.updateGloveSwitchstate(0);
					ServerManager.getInstance().CloseGloveMode();
					Log.d(TAG, "CloseGloveMode");
				}
			} 
		}); 		
		
		listData = profile.getAll();
		ListViewAdapter mylistAdapter = new ListViewAdapter(MainActivity.this, listData, R.layout.listview_item,
				new String[]{"id", "action", "img"}, 
				new int[]{R.id.myitem_id, R.id.myitem_action, R.id.myitem_img});
		mListview.setDividerHeight(0);
		mListview.setAdapter(mylistAdapter);
		
		
		ExitApp.getInstance().addActivity(this);
	}
	
	private class ListViewAdapter extends BaseAdapter {
		private ArrayList<HashMap<String, Object>> mData;
		private LayoutInflater mInflater;
		private int mLayout;
		private String[] mFrom;
		private int[] mTo;		

		public ListViewAdapter(Context context,
				ArrayList<HashMap<String, Object>> listData, int layout,
				String [] from, int[] to) {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mData = listData;
			mLayout = layout;
			mFrom = from;
			mTo = to;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public String getItem(int position) {
			return mData.get(position).toString();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			convertView = mInflater.inflate(mLayout, null);
			HashMap<String, Object> hm = (HashMap<String, Object>) mData.get(position);
			if(position==1){
				View viewline = convertView.findViewById(R.id.view_line);
				viewline.setBackgroundColor(MainActivity.this.getResources().getColor(R.color.viewline));
			}
			
			for(int i = 0; i < mTo.length; i++){
				View v = convertView.findViewById(mTo[i]);
				if(v instanceof ImageView) {
					((ImageView)v).setImageResource((Integer) hm.get("img"));
				}else if(v instanceof TextView){
					if(mFrom[i].equals("id")){
						//((TextView)v).setText(hm.get("id").toString());
					}else if(mFrom[i].equals("action")){
						((TextView)v).setText(getResources().getString((Integer) hm.get("action")));
					}	
				}
			}
			return convertView;
		}
	}
	
	public void gestureclick(View v){
		int switchstate = profile.getSwitchstate();
		
		if(switchstate == 1) {
			mTogglebtn.setChecked(false);
			mTextView.setText(R.string.close);
			
		}else {
			mTogglebtn.setChecked(true);
			mTextView.setText(R.string.open);
		}
	}
	public void gloveclick (View v){
		int switchstateGlove = profile.getGloveSwitchstate();
		
		if(switchstateGlove == 1) {
			mTogglebtnGlove.setChecked(false);
			mTextViewGlove.setText(R.string.close);
		}else {
			mTogglebtnGlove.setChecked(true);
			mTextViewGlove.setText(R.string.open);
		}
	}
	
	
	public void MainActivityBack(View v){
		MainActivity.this.finish();
	}
}
