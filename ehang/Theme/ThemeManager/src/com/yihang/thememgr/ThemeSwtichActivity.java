package com.yihang.thememgr;

import com.yihang.thememgr.R;
import com.yihang.thememgr.helper.ThemeInfo;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast; 
  
public class ThemeSwtichActivity extends Activity {

	static final String TAG = "ThemeSwtichActivity";
	private static final int MSG_APPLY_THEME_TIMEOUT = 202;
	private static final long APPLY_THEME_TIMEOUT = 1000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.theme_swtich);
		swtichThemes();
		finish();
	}
	private Dialog	mDialog;
	private void swtichThemes() {
		// TODO Auto-generated method stub
		ThemeManager themeMgr = ThemeManager.getInstance(this);
	    ThemeInfo info = themeMgr.getNextThemeInfo(ThemeUtils.CATEGORY_THEME);
	    Log.e(TAG, "swtichThemes, info pkg:"+info);
	    if(info != null){
	        themeMgr.applyTheme(ThemeUtils.CATEGORY_THEME, info); 
	    }
	}

}