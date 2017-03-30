package com.yihang.altimeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class altimeter extends Activity implements SensorEventListener {

	private static String TAG = "altimeter";
	private Sensor mSensor;
	private SensorManager mSensorManager;
	private TextView altitude;
	private TextView barometer;
	private TextView relative_height;

	private static boolean is_setting_reference = false;
	private static double reference_value = 0.0D;
	private static final String runOnStart = "RUNONSTART";
	private double current_altitude = 0.0D;
	private int current_temperature = 20;
	private boolean first_init = false;
	private boolean is_settings_changed = true;
	private TextView reference_height_value;
	private int sealevel_barometer = 101325;
	private TextView temperature;
	private boolean isSetting = false;
	private int old_barometer = 0;
	private int sensor_value = 0;
	
	private static final int SENSOR_COUNT_MAX = 5;
	private int sensor_count = SENSOR_COUNT_MAX-2;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		Log.e(TAG, "onCreate");
		mSensorManager = ((SensorManager) getSystemService("sensor"));
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		barometer = ((TextView) findViewById(R.id.barometer_value));
		altitude = ((TextView) findViewById(R.id.altitude_value));
		relative_height = ((TextView) findViewById(R.id.relative_height_value));

		if (getSharedPreferences("RUNONSTART", 0).getInt("RUNONSTART", 0) != 0)
			return;
		about_dialog(this);

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public boolean onCreateOptionsMenu(Menu paramMenu) {
		getMenuInflater().inflate(R.menu.main, paramMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
		int i = paramMenuItem.getItemId();

		if (i == R.id.action_settings) {
			isSetting = true;
			settings_Dialog(this);
			return true;
		}

		if (i == R.id.action_setting_reference) {
			isSetting = true;
			setting_reference_dialog(this);
			return true;
		}

		if (i == R.id.action_about) {
			about_dialog(this);
			return true;
		}
		return super.onOptionsItemSelected(paramMenuItem);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float f = event.values[0];
		DecimalFormat localDecimalFormat = new DecimalFormat("0.0");
		int sensor_value = 0;
		
		SimpleDateFormat sd = new SimpleDateFormat();
		
		sensor_value = (int) (100.0F * f);
		
		sensor_count++;
		if(sensor_count >= SENSOR_COUNT_MAX){
			sensor_count = 0;
		}else{
			return;
		}
		
		Log.e(TAG, "onSensorChanged:" + sensor_value);
		if (sensor_value <= 0) {
			Log.e(TAG, "sensor_value <= 0::" + sensor_value);
			return;
		}
		barometer.setText(String.valueOf(sensor_value));
		double d = 18400.0
				* ((double) 1.0 + (double) current_temperature / (double) 273.0)
				* Math.log10((double) sealevel_barometer / (double) sensor_value);
		current_altitude = d;
		if (is_settings_changed) {
			is_settings_changed = false;
			reference_value = current_altitude;
		}
		altitude.setText(String.valueOf(localDecimalFormat.format(d)));
		Log.e(TAG, "onSensorChanged:" + is_setting_reference
				+ "; reference_value:" + reference_value);

		if (is_setting_reference) {
			is_setting_reference = false;
			reference_value = current_altitude;
		}
		relative_height.setText(String.valueOf(localDecimalFormat
				.format(current_altitude - reference_value)));

		if (!first_init) {
			first_init = true;
			reference_value = current_altitude;
		}
		relative_height.setText(String.valueOf(localDecimalFormat
				.format(current_altitude - reference_value)));
	}

	protected void onPause() {
		Log.e(TAG, "onPause");
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	protected void onRestart() {
		Log.e(TAG, "onRestart");
		super.onRestart();
	}

	protected void onResume() {
		Log.e(TAG, "onResume");
		super.onResume();
		mSensorManager.registerListener(this, this.mSensor, 3);
	}

	public void onConfigurationChanged(Configuration paramConfiguration) {
		super.onConfigurationChanged(paramConfiguration);
		Log.e(TAG, "onConfigurationChanged");
	}

	protected void onStart() {
		Log.e(TAG, "onStart");
		super.onStart();
	}

	protected void onStop() {
		Log.e(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();
	}

	private void about_dialog(Context context) {
		SharedPreferences localSharedPreferences = getSharedPreferences(
				"RUNONSTART", 0);
		int runOnStartState = localSharedPreferences.getInt("RUNONSTART", 0);
		LinearLayout localLinearLayout = new LinearLayout(context);
		localLinearLayout.setOrientation(LinearLayout.VERTICAL);
		View localView1 = LayoutInflater.from(context).inflate(
				R.layout.about_infos, null);
		View localView2 = LayoutInflater.from(context).inflate(
				R.layout.copyright, null);
		View localView3 = LayoutInflater.from(context).inflate(
				R.layout.runonstart, null);
		localLinearLayout.addView(localView1);
		localLinearLayout.addView(localView3);
		localLinearLayout.addView(localView2);
		// ((TextView)localView3.findViewById(R.id.textview));
		CheckBox localCheckBox = (CheckBox) localView3
				.findViewById(R.id.checkbox);

		if (runOnStartState > 0) {
			localCheckBox.setChecked(true);
		}
		localCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences localSharedPreferences = getSharedPreferences(
						"RUNONSTART", 0);
				Editor editor = localSharedPreferences.edit();

				if (isChecked == true) {
					editor.putInt("RUNONSTART", 1);
				} else {
					editor.putInt("RUNONSTART", 0);
				}
				editor.commit();
			}

		});

		new AlertDialog.Builder(context).setTitle(R.string.text_about)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton(android.R.string.ok, null)
				.setView(localLinearLayout).show();
	}

	private void setting_reference_dialog(Context paramContext) {
		final Button okButton;

		AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramContext);
		localBuilder.setTitle(R.string.text_alert).setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(R.string.text_set_reference)
				.setPositiveButton(R.string.text_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
//						is_setting_reference = true;
					}
				}).setNegativeButton(R.string.text_cancel, null);

		final AlertDialog dialog = localBuilder.create();

		dialog.show();
		okButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
		dialog.setCanceledOnTouchOutside(false);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				is_setting_reference = true;
				dialog.dismiss();
				isSetting = false;

			}
		});
	}

	private void settings_Dialog(final Context paramContext) {
		View localView = LayoutInflater.from(paramContext).inflate(
				R.layout.alert_dialog_settings, null);

		final EditText sealevel_text = (EditText) localView
				.findViewById(R.id.sealevel_edit);
		final EditText temperature_text = (EditText) localView
				.findViewById(R.id.temperature_edit);
		final Button okButton;
		final Button cancelButton;

		sealevel_text.setText(String.valueOf(sealevel_barometer));
		temperature_text.setText(String.valueOf(current_temperature));

		AlertDialog.Builder localBuilder = new AlertDialog.Builder(paramContext);

		localBuilder.setTitle(R.string.text_settings);
		localBuilder.setIcon(android.R.drawable.ic_dialog_dialer);
		localBuilder.setView(localView);
		localBuilder.setPositiveButton(R.string.text_ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						// if((sealevel_text.getText().toString().length() == 0)
						// || (temperature_text.getText().toString().length() ==
						// 0)){
						// Toast.makeText(paramContext, R.string.text_barometer_and_temperature_is_empty, 0).show();
						// return;
						// }
						//
						// sealevel_barometer =
						// Integer.parseInt(sealevel_text.getText().toString());
						// current_temperature =
						// Integer.parseInt(temperature_text.getText().toString());
					}

				});
		localBuilder.setNegativeButton(R.string.text_cancel, null);

		final AlertDialog dialog = localBuilder.create();

		dialog.show();
		okButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
		cancelButton = dialog.getButton(Dialog.BUTTON_NEGATIVE);
		dialog.setCanceledOnTouchOutside(false);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if ((sealevel_text.getText().toString().length() == 0)
						&& (temperature_text.getText().toString().length() == 0)) {
					Toast.makeText(paramContext, R.string.text_barometer_and_temperature_is_empty,
							Toast.LENGTH_LONG).show();
					return;
				} else if (sealevel_text.getText().toString().length() == 0) {
					Toast.makeText(paramContext, R.string.text_barometer_is_empty, Toast.LENGTH_LONG)
							.show();
					return;
				} else if (temperature_text.getText().toString().length() == 0) {
					Toast.makeText(paramContext, R.string.text_temperature_is_empty, Toast.LENGTH_LONG)
							.show();
					return;
				}

				sealevel_barometer = Integer.parseInt(sealevel_text.getText()
						.toString());
				current_temperature = Integer.parseInt(temperature_text
						.getText().toString());
				is_setting_reference = true;
				dialog.dismiss();
				isSetting = false;

			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.e(TAG, "cancelButton");
				dialog.dismiss();
				isSetting = false;

			}
		});

	}
}
