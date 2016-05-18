
package com.ehangcompass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.Locale;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class CompassActivity extends Activity {

	private static final String TAG = "Compass";
	private final float MAX_ROATE_DEGREE = 1.0f;
	private SensorManager mSensorManager;
	private Sensor mOrientationSensor;
	private Sensor aSensor = null;
	private Sensor mSensor = null;

	float[] mAccelerometerValues = new float[3];
	float[] mMagneticFieldValues = new float[3];
	float[] mMatrix = new float[9];
	float[] mValues = new float[3];

	private LocationClient mLocationClient;
	private LocationMode mTempMode = LocationMode.Hight_Accuracy;
	private String mTempcoor = "gcj02";
	private final int mSpan = 1000;
	public static long mStartTime;
	
	private float mDirection;
	private float mTargetDirection;
	private AccelerateInterpolator mInterpolator;
	protected final Handler mHandler = new Handler();
	private boolean mStopDrawing;
	private boolean mChinease;

	View mCompassView;
	CompassView mPointer;
	TextView mLocationTextView;
	View mLocationLayout;
	LinearLayout mDirectionLayout;
	LinearLayout mAngleLayout;

	protected Runnable mCompassViewUpdater = new Runnable() {
		@Override
		public void run() {
			if (mPointer != null && !mStopDrawing) {
				if (mDirection != mTargetDirection) {

					// calculate the short routine
					float to = mTargetDirection;
					if (to - mDirection > 180) {
						to -= 360;
					} else if (to - mDirection < -180) {
						to += 360;
					}

					// limit the max speed to MAX_ROTATE_DEGREE
					float distance = to - mDirection;
					if (Math.abs(distance) > MAX_ROATE_DEGREE) {
						distance = distance > 0 ? MAX_ROATE_DEGREE
								: (-1.0f * MAX_ROATE_DEGREE);
					}

					// need to slow down if the distance is short
					mDirection = normalizeDegree(mDirection
							+ ((to - mDirection) * mInterpolator
									.getInterpolation(Math.abs(distance) > MAX_ROATE_DEGREE ? 0.4f
											: 0.3f)));
					mPointer.updateDirection(mDirection);
				}

				updateDirection();

				mHandler.postDelayed(mCompassViewUpdater, 20);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initResources();
		initServices();
		
		// baidu location manager
		mLocationTextView.setMovementMethod(ScrollingMovementMethod
				.getInstance());
        ((LocationApplication)getApplication()).mLocationResult = mLocationTextView;
        
        initLocation();
		mLocationClient.start();
		mStartTime = System.currentTimeMillis();
        Log.e(TAG, "mLocationClient.start");
		
		if (getSharedPreferences("RUNONSTART", 0).getInt("RUNONSTART", 0) != 0)
			return;
		dialog_Calibration(CompassActivity.this);
	}

	private void initResources() {
		mDirection = 0.0f;
		mTargetDirection = 0.0f;
		mInterpolator = new AccelerateInterpolator();
		mStopDrawing = true;
		//language
		mChinease = TextUtils.equals(Locale.getDefault().getLanguage(), "zh");

		mCompassView = findViewById(R.id.view_compass);
		mPointer = (CompassView) findViewById(R.id.compass_pointer);
		mLocationClient = ((LocationApplication)getApplication()).mLocationClient;
		mLocationTextView = (TextView)findViewById(R.id.textview_location);
		mLocationLayout = findViewById(R.id.location_layout);
		mDirectionLayout = (LinearLayout) findViewById(R.id.layout_direction);
		mAngleLayout = (LinearLayout) findViewById(R.id.layout_angle);

		mPointer.setImageResource(R.drawable.compass);
	}
	private void initServices() {
		// sensor manager
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		mSensorManager.registerListener(mOrientationSensorEventListener,
				aSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mOrientationSensorEventListener,
				mSensor, SensorManager.SENSOR_DELAY_GAME);

		//TYPE_ORIENTATION   This constant is deprecated. use SensorManager.getOrientation() instead.
		// mOrientationSensor = mSensorManager
		// .getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(mTempMode);
        option.setCoorType(mTempcoor);
        option.setScanSpan(mSpan);
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setLocationNotify(false);
        option.setIgnoreKillProcess(true);
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
        
        Log.e(TAG, "initLocation");
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		mLocationClient.start();
		// sensor manager
		if (mOrientationSensor != null) {
			mSensorManager.registerListener(mOrientationSensorEventListener,
					mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
		}
		mStopDrawing = false;
		mHandler.postDelayed(mCompassViewUpdater, 20);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mStopDrawing = true;
		if (mOrientationSensor != null) {
			mSensorManager.unregisterListener(mOrientationSensorEventListener);
		}
	}

   @Override
    protected void onStop() {
        // stop 
    mLocationClient.stop();
    super.onStop();
   }
   
	private void updateDirection() {
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		mDirectionLayout.removeAllViews();
		mAngleLayout.removeAllViews();

		ImageView east = null;
		ImageView west = null;
		ImageView south = null;
		ImageView north = null;
		float direction = normalizeDegree(mTargetDirection * -1.0f);
		if (direction > 22.5f && direction < 157.5f) {
			// east
			east = new ImageView(this);
			east.setImageResource(mChinease ? R.drawable.e_cn : R.drawable.e);
			east.setLayoutParams(lp);
		} else if (direction > 202.5f && direction < 337.5f) {
			// west
			west = new ImageView(this);
			west.setImageResource(mChinease ? R.drawable.w_cn : R.drawable.w);
			west.setLayoutParams(lp);
		}

		if (direction > 112.5f && direction < 247.5f) {
			// south
			south = new ImageView(this);
			south.setImageResource(mChinease ? R.drawable.s_cn : R.drawable.s);
			south.setLayoutParams(lp);
		} else if (direction < 67.5 || direction > 292.5f) {
			// north
			north = new ImageView(this);
			north.setImageResource(mChinease ? R.drawable.n_cn : R.drawable.n);
			north.setLayoutParams(lp);
		}

		if (mChinease) {
			// east/west should be before north/south
			if (east != null) {
				mDirectionLayout.addView(east);
			}
			if (west != null) {
				mDirectionLayout.addView(west);
			}
			if (south != null) {
				mDirectionLayout.addView(south);
			}
			if (north != null) {
				mDirectionLayout.addView(north);
			}
		} else {
			// north/south should be before east/west
			if (south != null) {
				mDirectionLayout.addView(south);
			}
			if (north != null) {
				mDirectionLayout.addView(north);
			}
			if (east != null) {
				mDirectionLayout.addView(east);
			}
			if (west != null) {
				mDirectionLayout.addView(west);
			}
		}

		int direction2 = (int) direction;
		boolean show = false;
		if (direction2 >= 100) {
			mAngleLayout.addView(getNumberImage(direction2 / 100));
			direction2 %= 100;
			show = true;
		}
		if (direction2 >= 10 || show) {
			mAngleLayout.addView(getNumberImage(direction2 / 10));
			direction2 %= 10;
		}
		mAngleLayout.addView(getNumberImage(direction2));

		ImageView degreeImageView = new ImageView(this);
		degreeImageView.setImageResource(R.drawable.degree);
		degreeImageView.setLayoutParams(lp);
		mAngleLayout.addView(degreeImageView);
	}

	private ImageView getNumberImage(int number) {
		ImageView image = new ImageView(this);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		switch (number) {
		case 0:
			image.setImageResource(R.drawable.number_0);
			break;
		case 1:
			image.setImageResource(R.drawable.number_1);
			break;
		case 2:
			image.setImageResource(R.drawable.number_2);
			break;
		case 3:
			image.setImageResource(R.drawable.number_3);
			break;
		case 4:
			image.setImageResource(R.drawable.number_4);
			break;
		case 5:
			image.setImageResource(R.drawable.number_5);
			break;
		case 6:
			image.setImageResource(R.drawable.number_6);
			break;
		case 7:
			image.setImageResource(R.drawable.number_7);
			break;
		case 8:
			image.setImageResource(R.drawable.number_8);
			break;
		case 9:
			image.setImageResource(R.drawable.number_9);
			break;
		}
		image.setLayoutParams(lp);
		return image;
	}

	private SensorEventListener mOrientationSensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				mMagneticFieldValues = event.values;
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mAccelerometerValues = event.values;
			}

			SensorManager.getRotationMatrix(mMatrix, null,
					mMagneticFieldValues, mAccelerometerValues);
			SensorManager.getOrientation(mMatrix, mValues);

			mValues[0] = (float) Math.toDegrees(mValues[0]);

			float direction = mValues[0] * -1.0f;
			mTargetDirection = normalizeDegree(direction);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	private float normalizeDegree(float degree) {
		return (degree + 720) % 360;
	}

	private void dialog_Calibration(Context context) {
		SharedPreferences localSharedPreferences = getSharedPreferences(
				"RUNONSTART", 0);
		int i = localSharedPreferences.getInt("RUNONSTART", 0);
		LinearLayout localLinearLayout = new LinearLayout(context);
		localLinearLayout.setOrientation(LinearLayout.VERTICAL);
		View localView1 = LayoutInflater.from(context).inflate(
				R.layout.calibration_info, null);
		View localView2 = LayoutInflater.from(context).inflate(
				R.layout.runonstart, null);
		localLinearLayout.addView(localView1);
		localLinearLayout.addView(localView2);
		CheckBox localCheckBox = (CheckBox) localView2
				.findViewById(R.id.checkbox);

		if (i > 0) {
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

		new AlertDialog.Builder(context).setTitle(R.string.title_calibration)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setPositiveButton(android.R.string.ok, null)
				.setView(localLinearLayout).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// add options of menu
		switch (item.getItemId()) {
		case R.id.menu_mylocation:
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
			break;
		case R.id.menu_calibration:
			dialog_Calibration(CompassActivity.this);
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
