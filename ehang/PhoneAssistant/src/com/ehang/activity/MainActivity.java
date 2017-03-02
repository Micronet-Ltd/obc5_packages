package com.ehang.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import com.ehang.dbutil.PackageMethod;
import com.ehang.dbutil.dbHelper;
import com.ehang.dbutil.listviewItemEntity;
import com.ehang.dbutil.dbImplement;
import com.ehang.phoneassistant.R;
import android.R.integer;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.ClipData.Item;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ActivityManager;
import android.app.AppOpsManager;

public class MainActivity extends Activity {

	private String TAG = "MainActivity";
	private ArrayList localArrayList;
	private PackageManager mpackagemanager;
	List<listviewItemEntity> listEntity;
	listviewItemEntity listviewitem;
	ListView listview;
	Handler mhandler;
	ToggleButton mswitch;
	PackageMethod pm;
	ProgressDialog dialog;
	SharedPreferences sp;
	int count_protect = 0;
	BaseAdapter mAdapter;
	dbImplement dbimplement;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		dbimplement = new dbImplement();
		final TextView textview = (TextView) findViewById(R.id.install);
		final String cp = getResources().getString(R.string.install);
		File file = getApplicationContext().getDatabasePath("TSwitch.db");
		localArrayList = new ArrayList();
		listview = (ListView) super.findViewById(R.id.listview);
		mswitch = (ToggleButton) findViewById(R.id.mswitch);
		listEntity = new ArrayList<listviewItemEntity>();
		mpackagemanager = getPackageManager();
		pm = new PackageMethod(MainActivity.this);
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setMessage(getResources().getString(R.string.pdialog));
		dialog.show();
		mhandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == 0) {
					if (mAdapter == null) {
						mAdapter = new base();
						listview.setAdapter(mAdapter);
					} else {
						
						mAdapter.notifyDataSetChanged();
					}
					dialog.dismiss();

				} else if (msg.what == 1) {
					textview.setText(String.format(cp, count_protect));
				} else if (msg.what == 3) {
					// mswitch.setChecked(true);
				}
				super.handleMessage(msg);
			}

		};

		if (!file.exists()) {

			List list2 = pm.getAppInfos();

			for (int i = 0; i < list2.size(); i++) {
				ApplicationInfo localapplicationInfo = (ApplicationInfo) list2
						.get(i);
				String packagename = localapplicationInfo.packageName;
				String[] items = getResources().getStringArray(
						R.array.white_list);

				int state = 0;
				Object param[] = { state, packagename };
				boolean flag = dbimplement.addData(param, MainActivity.this);
				for (int j = 0; j < items.length; j++) {
					if (packagename.equals(items[j])) {
						int states = 1;
						Object params[] = { states, packagename };
						boolean flags = dbimplement
								.updateData(params, MainActivity.this);
					}

				}
			}
		}

		List<Map<String, Object>> oo = dbimplement.queryPackageName(MainActivity.this);
		for (Map<String, Object> m : oo) {
			for (String k : m.keySet()) {
				count_protect++;
			}

		}

		textview.setText(String.format(cp, count_protect));

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				TextView protectname = (TextView) arg1
						.findViewById(R.id.protect);
				mswitch = (ToggleButton) arg1.findViewById(R.id.mswitch);
				if (mswitch.isChecked()) {
					protectname.setText(getResources().getString(
							R.string.unprotectedname));
					mswitch.setChecked(false);
					listEntity.get(arg2).setEnable(0);
					String packagename = listEntity.get(arg2).getPackagename();
					int state = 0;
					Object param[] = { state, packagename };
					boolean falg = dbimplement.updateData(param, MainActivity.this);
					count_protect--;
					mhandler.sendEmptyMessage(1);

				} else {
					protectname.setText(getResources().getString(
							R.string.protectedname));
					mswitch.setChecked(true);
					listEntity.get(arg2).setEnable(1);
					String packagename = listEntity.get(arg2).getPackagename();
					int state = 1;
					Object param[] = { state, packagename };
					boolean falg = dbimplement.updateData(param, MainActivity.this);
					count_protect++;
					mhandler.sendEmptyMessage(1);
				}
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	protected void onResume() {
		listEntity.clear();
		refreshApps();
		mhandler.sendEmptyMessage(0);
		super.onResume();
	}

	private void refreshApps() {
		List list = pm.getAppInfos();
		for (int i = 0; i < list.size(); i++) {
			listviewitem = new listviewItemEntity();
			ApplicationInfo localapplicationInfo = (ApplicationInfo) list
					.get(i);
			String packagename = localapplicationInfo.packageName;
			String apkname = null;
			String versionnumber = null;
			try {
				apkname = mpackagemanager.getApplicationLabel(
						mpackagemanager.getApplicationInfo(packagename,
								PackageManager.GET_META_DATA)).toString();
				PackageInfo pi = mpackagemanager.getPackageInfo(packagename, 0);
				versionnumber = pi.versionName;
			} catch (NameNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Drawable picture = localapplicationInfo.loadIcon(mpackagemanager);
			listviewitem.setApkname(apkname);
			listviewitem.setPackagename(packagename);
			listviewitem.setPicture(picture);
			listviewitem.setVersionnumber(versionnumber);
			int states = dbimplement.getPackageEnable(MainActivity.this,
					packagename);
			listviewitem.setEnable(states);
			listEntity.add(listviewitem);
		}

		Collections.sort(listEntity, new Comparator<listviewItemEntity>() {
			public int compare(listviewItemEntity p1, listviewItemEntity p2) {
				return p2.getEnable() - p1.getEnable();
			}
		});
	}
	class findotherapp extends Thread {
		@Override
		public void run() {
			List list = pm.getAppInfos();
			for (int i = 0; i < list.size(); i++) {
				listviewitem = new listviewItemEntity();
				ApplicationInfo localapplicationInfo = (ApplicationInfo) list
						.get(i);

				String packagename = localapplicationInfo.packageName;
				String apkname = null;
				String versionnumber = null;
				try {
					apkname = mpackagemanager.getApplicationLabel(
							mpackagemanager.getApplicationInfo(packagename,
									PackageManager.GET_META_DATA)).toString();
					PackageInfo pi = mpackagemanager.getPackageInfo(
							packagename, 0);
					versionnumber = pi.versionName;
				} catch (NameNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Drawable picture = localapplicationInfo
						.loadIcon(mpackagemanager);
				listviewitem.setApkname(apkname);
				listviewitem.setPackagename(packagename);
				listviewitem.setPicture(picture);
				listviewitem.setVersionnumber(versionnumber);
				listEntity.add(listviewitem);
			}

			mhandler.sendEmptyMessage(0);
			super.run();
		}

	}
	class base extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listEntity.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			LayoutInflater inflate = LayoutInflater.from(MainActivity.this);
			View view = inflate.inflate(R.layout.list_details, null);
			TextView packagename = (TextView) view
					.findViewById(R.id.packagename);
			TextView protectname = (TextView) view.findViewById(R.id.protect);
			TextView apkname = (TextView) view.findViewById(R.id.apkname);
			// packagename.setText(listEntity.get(position).getPackagename());
			mswitch = (ToggleButton) view.findViewById(R.id.mswitch);
			apkname.setText(listEntity.get(position).getApkname());
			ImageView picture = (ImageView) view.findViewById(R.id.picture);
			picture.setImageDrawable(listEntity.get(position).getPicture());

			mswitch.setChecked(listEntity.get(position).getEnable() == 1);
			if (mswitch.isChecked()) {

				protectname.setText(getResources().getString(
						R.string.protectedname));

			} else {

				protectname.setText(getResources().getString(
						R.string.unprotectedname));

			}

			// TODO Auto-generated method stub
			return view;
		}

	}

	/*
	 * public void showDetails(View v){ Builder b = new
	 * AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
	 * b.setTitle(R.string.help); b.setMessage(R.string.description); Dialog
	 * dialog = b.create();
	 * 
	 * dialog.show(); }
	 */

	public void back(View v) {
		
		MainActivity.this.finish();

	}

}
