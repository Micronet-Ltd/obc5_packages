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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private String TAG = "MainActivity";
	private ArrayList<HashMap<String, Object>> listData;
	private Profile profile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ToggleButton mTogglebtn = (ToggleButton)findViewById(R.id.mToggleBtn);
		ListView mListview = (ListView)findViewById(R.id.mListView);
		profile = Profile.getInstance(getApplicationContext());
		int switchstate = profile.getSwitchstate();
		
		if(switchstate == 1) {
			mTogglebtn.setChecked(true);
			
		}else {
			mTogglebtn.setChecked(false);
		}
		Log.e(TAG, "switchState = " + profile.getSwitchstate());
		
		mTogglebtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
			@Override
			public void onCheckedChanged(CompoundButton buttonview, boolean isChecked) {
				if(isChecked) {
					Log.e(TAG, "===========mtogglebtn is checked============");
					profile.updateSwitchstate(1);
					Intent service = new Intent(getBaseContext(), com.yihang.gesture.service.GestureService.class);
					getBaseContext().startService(service);
					ServerManager.getInstance().Opentp();
					Log.e(TAG, "switchState = " + profile.getSwitchstate()+"ServerManager.Open_tp");
					
				}else {
					Log.e(TAG, "===========mtogglebtn isn't checked============");
					profile.updateSwitchstate(0);
					Intent service = new Intent(getBaseContext(), com.yihang.gesture.service.GestureService.class);
					getBaseContext().stopService(service);
					ServerManager.getInstance().Closetp();
					Log.e(TAG, "switchState = " + profile.getSwitchstate()+"ServerManager.Close_tp");
				}
			} 
		}); 
		
		listData = profile.getAll();
		
		ListViewAdapter mylistAdapter = new ListViewAdapter(MainActivity.this, listData, R.layout.listview_item,
										new String[]{"id", "action", "img"}, 
										new int[]{R.id.myitem_id, R.id.myitem_action, R.id.myitem_img});
		mListview.setAdapter(mylistAdapter);
		
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
			mInflater = (LayoutInflater) getSystemService(context.LAYOUT_INFLATER_SERVICE);
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
}
