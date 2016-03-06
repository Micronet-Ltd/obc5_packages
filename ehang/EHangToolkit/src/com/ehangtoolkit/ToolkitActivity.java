package com.ehangtoolkit;

import com.ehangtoolkit.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class ToolkitActivity extends Activity {
	/** Called when the activity is first created. */
	private static final String TAG = "ToolkitActivity";
	private static Button mImgbtnLight;
	private Button mImgbtnSOS;
	private Button mImgbtnAltimeter;
	private Button mImgbtnControlrecorder;
	private Button mImgbtnMagnifiter;
	private Button mImgbtnMypedometer;
	private Button mImgbtnCompass;
	private Button mImgbtnAbout;

	public static boolean torchSwitch = false; 
	public static boolean torchSwitchLocalFlag = false;

	private final static String broadCastSystemUI = "com.android.SystemUI.FlashlightTile";
	private final static String broadCastDeskWidget = "qualcomm.android.LEDWidget";
	private final static String broadCastNameSystemUI = "systemui_led";
	private final static String broadCastNameDeskWidget = "deskwidget_led";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mImgbtnLight = (Button) findViewById(R.id.imagebutton1);
		mImgbtnSOS = (Button) findViewById(R.id.imagebutton2);
		mImgbtnAltimeter = (Button) findViewById(R.id.imagebutton3);
		mImgbtnControlrecorder = (Button) findViewById(R.id.imagebutton4);
		mImgbtnMagnifiter = (Button) findViewById(R.id.imagebutton5);
		mImgbtnMypedometer = (Button) findViewById(R.id.imagebutton6);
		mImgbtnCompass = (Button) findViewById(R.id.imagebutton7);
		mImgbtnAbout = (Button) findViewById(R.id.imagebutton8);


		mImgbtnLight.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!torchSwitch) {
					torchSwitch = true;
					torchSwitchLocalFlag = true;
				} else {
					torchSwitch = false;
					torchSwitchLocalFlag = false;
				}

				sendBroadcastSend();
				updateLightStatus(ToolkitActivity.this);
			}
		});
		
		updateLightStatus(ToolkitActivity.this);		
		
		mImgbtnSOS.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// start SOS
				doStartApplicationWithPackageName("com.ehang.sos",
						"com.ehang.sos.MainUI");
			}
		});

		mImgbtnAltimeter.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// start altimeter
				doStartApplicationWithPackageName("com.yihang.altimeter",
						"com.yihang.altimeter.altimeter");
			}

		});

		mImgbtnControlrecorder.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// start controlrecorder
				doStartApplicationWithPackageName("com.ehang.controlrecorder",
						"com.ehang.show.ShowActivity");
			}
		});

		mImgbtnMagnifiter.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				// open magnifiter
				doStartApplicationWithPackageName("com.magnifiter",
						"com.magnifiter.MagnifiterActivity");
			}
		});

		mImgbtnMypedometer.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// start mypedometer
				doStartApplicationWithPackageName("com.yihang.mypedometer",
						"com.yihang.mypedometer.MainActivity");
			}
		});

		mImgbtnCompass.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// start compass
				doStartApplicationWithPackageName("com.ehangcompass",
						"com.ehangcompass.CompassActivity");
			}
		});

		mImgbtnAbout.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ToolkitActivity.this,
						AboutView.class);
				startActivity(intent);
			}

		});
	}

	private void doStartApplicationWithPackageName(String packageName,
			String className) {
		Intent intent = new Intent(Intent.ACTION_MAIN);

		ComponentName cn = new ComponentName(packageName, className);
		intent.setComponent(cn);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG,"onResume torchSwitch = " + torchSwitch);
		
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG,"onStop torchSwitch = " + torchSwitch);
	}
	
	@Override
	protected void onDestroy() {
		if (torchSwitchLocalFlag && torchSwitch) {
			torchSwitch = false;
			Log.i(TAG,"onDestroy torchSwitch = " + torchSwitch);
			sendBroadcastSend();
		}
		super.onDestroy();
	}

	// update torch status
	private static void updateLightStatus(Context context) {

		Drawable drawableOn = context.getResources().getDrawable(
				R.drawable.icon_on_fight);
		Drawable drawableOff = context.getResources().getDrawable(
				R.drawable.icon_off_fight);

		if(mImgbtnLight == null){
			return;
		}
		if (torchSwitch) {
			mImgbtnLight.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
					drawableOn, null, null);
		} else {
			mImgbtnLight.setCompoundDrawablesRelativeWithIntrinsicBounds(null,
					drawableOff, null, null);
		}
	}

	public void dialog_Exit(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setMessage(R.string.text_close);
		builder.setTitle(R.string.title_hint);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setPositiveButton(R.string.text_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		builder.setNegativeButton(R.string.text_cancel,
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN
					&& event.getRepeatCount() == 0) {
				dialog_Exit(ToolkitActivity.this);
			}

			return true;
		}

		return super.dispatchKeyEvent(event);
	}

	public static class TorchStatusBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (broadCastSystemUI.equals(action)) {

				boolean fromSysKey = intent.getBooleanExtra(
						broadCastNameSystemUI, false);
				torchSwitch = fromSysKey;
				Log.i(TAG, "TorchStatusBroadcastReceiver fromSysKey = "
						+ fromSysKey);
			} else if (broadCastDeskWidget.equals(action)) {

				boolean fromDeskWidgetKey = intent.getBooleanExtra(
						broadCastNameDeskWidget, false);
				torchSwitch = fromDeskWidgetKey;
				Log.i(TAG,
						"TorchStatusBroadcastReceiver broadCastNameDeskWidget = "
								+ broadCastNameDeskWidget);
			}

			updateLightStatus(context);
		}
	};

	public void sendBroadcastSend() {
		Intent intent1 = new Intent();
		intent1.setAction(broadCastDeskWidget);
		intent1.putExtra(broadCastNameDeskWidget, torchSwitch);
		Log.i(TAG, "setOnClickListener torchSwitch = " + torchSwitch);
		ToolkitActivity.this.sendBroadcast(intent1);

		Intent intent2 = new Intent();
		intent2.setAction(broadCastSystemUI);
		intent2.putExtra(broadCastNameSystemUI, torchSwitch);
		ToolkitActivity.this.sendBroadcast(intent2);
	}

}