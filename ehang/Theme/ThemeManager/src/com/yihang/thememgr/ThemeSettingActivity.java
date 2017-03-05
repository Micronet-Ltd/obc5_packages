package com.yihang.thememgr;

import java.util.ArrayList;

import com.yihang.thememgr.R;
import com.yihang.thememgr.helper.ExitApp;
import com.yihang.thememgr.helper.ThemeInfo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView; //Add By liyichong to modified GUI  (QL100x) SW00071151 2014-08-22
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class ThemeSettingActivity extends Activity implements OnItemClickListener {

	static final String TAG = "ThemeSettingActivity";
	private static final int REQUEST_THEME_ITEM = 60;
	private static final int REQUEST_THEME_CUSTOM = 70;
	
	private GridView mThemeGrid;
	
	ThemeManager mThemeMgr;
	ThemeShowAdapter mAdapter;
	ArrayList<ThemeInfo> mArrayListTheme;
	String mThemeName;
	boolean mbForceUpdate;
	private ImageView imageview; //Add By liyichong to modified GUI  (QL100x) SW00071151 2014-08-22

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.theme_main);
		initViews();
		/* Add By liyichong to modified GUI  (QL100x) SW00071151 2014-08-22 begin */
		imageview = (ImageView) findViewById(R.id.icon_mytheme);
		String nThemeName = new String(mThemeMgr.getCurrentTheme(ThemeUtils.CATEGORY_THEME));
		if(nThemeName.contains("color")){
			imageview.setImageResource(R.drawable.color);
		}else if(nThemeName.contains("earlysummer")){
			imageview.setImageResource(R.drawable.earlysummer);
		}else if(nThemeName.contains("lightgeometry")){
			imageview.setImageResource(R.drawable.lightgeometry);
		}else if(nThemeName.contains("simple")){
			imageview.setImageResource(R.drawable.simple);
		}else if(nThemeName.contains("summertime")){
			imageview.setImageResource(R.drawable.summertime);
		}else if(nThemeName.contains("sunshine")){
			imageview.setImageResource(R.drawable.sunshine);
		}else{
			imageview.setImageResource(R.drawable.icon_mytheme);
		}
		Log.i(TAG, "onCreate nThemeName="+nThemeName);
		/* Add By liyichong to modified GUI  (QL100x) SW00071151 2014-08-22 end */
		Log.i(TAG, "onCreate");
		ExitApp.getInstance().addActivity(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		String theme = mThemeMgr.getCurrentTheme(ThemeUtils.CATEGORY_THEME);
		Log.i(TAG, "onResume mThemeName="+mThemeName+", theme="+theme);
		if(mbForceUpdate || !theme.equals(mThemeName)){
			mThemeName = new String(theme);
			mArrayListTheme = mThemeMgr.getAllTheme(ThemeUtils.CATEGORY_THEME);
			mAdapter = new ThemeShowAdapter(this, mArrayListTheme, ThemeUtils.THUMBIMG_THEME); 
			mThemeGrid.setAdapter(mAdapter);
			mbForceUpdate = false;
		}
		/* Add By liyichong to modified GUI  (QL100x) SW00071151 2014-08-22 begin */
		if(mThemeName.contains("color")){
			imageview.setImageResource(R.drawable.color);
		}else if(mThemeName.contains("earlysummer")){
			imageview.setImageResource(R.drawable.earlysummer);
		}else if(mThemeName.contains("lightgeometry")){
			imageview.setImageResource(R.drawable.lightgeometry);
		}else if(mThemeName.contains("simple")){
			imageview.setImageResource(R.drawable.simple);
		}else if(mThemeName.contains("summertime")){
			imageview.setImageResource(R.drawable.summertime);
		}else if(mThemeName.contains("sunshine")){
			imageview.setImageResource(R.drawable.sunshine);
		}else{
			imageview.setImageResource(R.drawable.icon_mytheme);
		}
		/* Add By liyichong to modified GUI  (QL100x) SW00071151 2014-08-22 begin */
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == ThemeUtils.ACTIVITY_RESULT_DELETE){
			Log.w(TAG, "onActivityResult theme delete");
			mbForceUpdate = true;
		}
    }
    
	private void initViews() {
		// TODO Auto-generated method stub
		mThemeMgr = ThemeManager.getInstance(this);
		mThemeName = new String(mThemeMgr.getCurrentTheme(ThemeUtils.CATEGORY_THEME));
		mArrayListTheme = mThemeMgr.getAllTheme(ThemeUtils.CATEGORY_THEME);
		mAdapter = new ThemeShowAdapter(this, mArrayListTheme, ThemeUtils.THUMBIMG_THEME);
		mThemeGrid = (GridView) findViewById(R.id.theme_install);
		mThemeGrid.setAdapter(mAdapter);
		mThemeGrid.setOnItemClickListener(this);
		
		mbForceUpdate = false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// TODO Auto-generated method stub
		ThemeInfo info = (ThemeInfo) parent.getItemAtPosition(position);

		if(info.mPkgName != null){
			Intent intent = new Intent();
			intent.setAction(ThemeUtils.ACTION_APPLY_THEME);
			intent.setClass(this, ThemeApplyActivity.class);
			intent.putExtra(ThemeUtils.PACKAGE_NAME, info.mPkgName);
			intent.putExtra(ThemeUtils.THEME_TYPE, ThemeUtils.getThemeType(info.mThemeType));
			intent.putExtra(ThemeUtils.THEME_CATEGORY, ThemeUtils.CATEGORY_THEME);
			startActivitySafely(intent, REQUEST_THEME_ITEM);
		}
	}
	public void	startActivitySafely(Intent intent, int requestCode){
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.exit(0);
	}	
}