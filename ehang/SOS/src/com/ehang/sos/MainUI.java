package com.ehang.sos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baidu.location.LocationClient;
import com.ehang.location.Location;
import com.ehang.location.LocationApplication;
import com.ehang.receiver.SOSReceiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings; 

public class MainUI extends Activity {

	LinearLayout setup_ll, numlist_ll, premms_ll, help_ll;
	TextView numList_, premms_;
	public static Button setup;
	Intent intent;
	static String txStr1, txStr2, txStr3, txStr4, txStr5, preMmsStr;
	LocationClient mLocationClient;
	public static Boolean isPreLoc = false, haveSetup = false;
	static String setmsg;
	TelephonyManager tm;
	static Vibrator vibrator;
	public static boolean isGpsOpen ;
	public static long startTime;
	public LocationManager lm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_ui);
		
		numlist_ll = (LinearLayout) findViewById(R.id.numlist_ll);
		premms_ll = (LinearLayout) findViewById(R.id.premms_ll);
		help_ll = (LinearLayout) findViewById(R.id.help_ll);
		numList_ = (TextView) findViewById(R.id.numlist_);
		premms_ = (TextView) findViewById(R.id.premms_);
		setup = (Button) findViewById(R.id.setup);

		setup.setOnClickListener(moduleClickListener);
		numlist_ll.setOnClickListener(moduleClickListener);
		premms_ll.setOnClickListener(moduleClickListener);
		help_ll.setOnClickListener(moduleClickListener);

		tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		intent = new Intent();
		setmsg = getString(R.string.setup);
		mLocationClient = ((LocationApplication) getApplicationContext()).mLocationClient;

		if (setup.getText().toString().equals(getString(R.string.setup))) {	
			isGpsOpen = Settings.Secure.isLocationProviderEnabled(getContentResolver(),LocationManager.GPS_PROVIDER);
		}

	}
	
	
	protected void onResume() {
		super.onResume();
		init();
		new Location(this).initLocation();
		Log.d("SOS", " --MainUI--onResume-- " );
		if (!haveSetup) {
			isPreLoc = true;
			openGps(this);
			setup.setText(getString(R.string.setup));
			setup.setClickable(true);
		} else {
			isPreLoc = false;
			setup.setText(getString(R.string.setuped));
			setup.setClickable(false);
		}
	}

	protected void onPause() {
		super.onPause();
		new Location(this).initLocation();
	}

	protected void onStop() {
		super.onStop(); 
		if (!haveSetup) {
			Log.d("SOS", " --MainUI--onStop-- " );
			closeGps(this);
			mLocationClient.stop();
		}
	}

	protected void onDestroy() {
		super.onDestroy();
	}

	protected OnClickListener moduleClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.setup:
				if(hasCard()){
					new Setup_Mod(MainUI.this).setup();
					setup.setText(getString(R.string.setuped));
					setup.setClickable(false);
				};
				break;
			case R.id.numlist_ll:
				intent.setClass(MainUI.this, NumList_Mod.class);
				startActivityForResult(intent, 1);
				break;
			case R.id.premms_ll:
				intent.setClass(MainUI.this, PreMms_Mod.class);
				startActivity(intent);
				break;
			case R.id.help_ll:
				new AlertDialog.Builder(new ContextThemeWrapper(MainUI.this,
						R.style.MyAlertDialog)).setTitle(R.string.help)
						.setMessage(R.string.help_detail).show();
				break;
			}

		}
	};

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {
				init();
			}
			break;
		default:
			break;
		}
	}

	private void init() {
		if (Storage.getInstance().getoutStorage(this, "num1Name") != "") {
			txStr1 = Storage.getInstance().getoutStorage(this, "num1Name");
		} else
			txStr1 = Storage.getInstance().getoutStorage(this, "num1");

		if (Storage.getInstance().getoutStorage(this, "num2Name") != "") {
			txStr2 = Storage.getInstance().getoutStorage(this, "num2Name");
		} else
			txStr2 = Storage.getInstance().getoutStorage(this, "num2");

		if (Storage.getInstance().getoutStorage(this, "num3Name") != "") {
			txStr3 = Storage.getInstance().getoutStorage(this, "num3Name");
		} else
			txStr3 = Storage.getInstance().getoutStorage(this, "num3");

		if (Storage.getInstance().getoutStorage(this, "num4Name") != "") {
			txStr4 = Storage.getInstance().getoutStorage(this, "num4Name");
		} else
			txStr4 = Storage.getInstance().getoutStorage(this, "num4");

		if (Storage.getInstance().getoutStorage(this, "num5Name") != "") {
			txStr5 = Storage.getInstance().getoutStorage(this, "num5Name");
		} else
			txStr5 = Storage.getInstance().getoutStorage(this, "num5");
		showNumMms();
	}

	private void showNumMms() {
		StringBuffer thestr = new StringBuffer();
		String a[] = { txStr1, txStr2, txStr3, txStr4, txStr5 };
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < a.length; i++) {
			if (!a[i].equals("")) {
				list.add(a[i]);
			}
		}
		if (list.size() != 0) {
			for (int j = 0; j < list.size() - 1; j++) {
				thestr = thestr.append(list.get(j)).append("\n");
			}
			thestr = thestr.append(list.get(list.size() - 1));
		}
		
		numList_.setText(thestr.toString());
		
		preMmsStr = Storage.getInstance().getoutStorage(this, "preMmsMsg");
		if (preMmsStr.equals("")) {
			premms_.setVisibility(View.GONE);
		} else {
			premms_.setText(preMmsStr);
			premms_.setVisibility(View.VISIBLE);
		}
	}

	public static Handler txHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				setup.setText(setmsg);
				setup.setClickable(true);
				break;
			default:
				break;
			}
		}
	};


	public static void openGps(Context context) {
		Log.d("SOS", "openGps()---isGpsOpen= " + isGpsOpen);
		if (isGpsOpen == false) {
			Log.d("SOS", "gpsTurnOn--openGps()");
			int mode = android.provider.Settings.Secure.LOCATION_MODE_HIGH_ACCURACY;
			Intent mIntent = new Intent("com.android.settings.location.MODE_CHANGING");
			mIntent.putExtra("NEW_MODE", mode);
			context.sendBroadcast(mIntent,android.Manifest.permission.WRITE_SECURE_SETTINGS);
			Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, mode);
		}
	}

	public static void closeGps(Context context) {
		Log.d("SOS", "closeGps()---isGpsOpen= " + isGpsOpen);
		if (isGpsOpen == false) {
			Log.d("SOS", "gpsTurnDown--closeGps()");
			int modeOff = android.provider.Settings.Secure.LOCATION_MODE_OFF;
	        Intent intent = new Intent("com.android.settings.location.MODE_CHANGING");
	        intent.putExtra("NEW_MODE", modeOff);
	        context.sendBroadcast(intent, android.Manifest.permission.WRITE_SECURE_SETTINGS);
	        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, modeOff);
		}
	}
	
	
	public void textShow(TextView txv, String message) {
		Spannable span = new SpannableString(message);
	    span.setSpan(new RelativeSizeSpan(0.8f), message.indexOf("("), message.indexOf(")")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    span.setSpan(new ForegroundColorSpan(R.color.sumFont), message.indexOf("("), message.indexOf(")")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    txv.setText(span);
	}
	
	
	public Boolean hasCard(){
		if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
			return true;
		} else if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {//no card
			Toast.makeText(MainUI.this, getString(R.string.nosim),Toast.LENGTH_SHORT).show();
			return false;
		} else {//error card
			Toast.makeText(MainUI.this, getString(R.string.errsim),Toast.LENGTH_SHORT).show();
		}
		return false;
	}
	
	
	
	public static void sendedVibrator(Context context) {
		Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		long [] pattern = {100,400,100,400};
        vibrator.vibrate(pattern,-1);
	}
	
	

	
	
	
}
