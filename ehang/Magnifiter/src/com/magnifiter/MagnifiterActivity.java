package com.magnifiter;

import java.util.Iterator;
import java.util.List;

import com.magnifiter.R;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;


public class MagnifiterActivity extends Activity implements SurfaceHolder.Callback {
	private static final String TAG = "Magnifier";
	private SurfaceView mSurfaceView;
	private TextView mTextView;
	private SeekBar mSeekBar;
	private int value = 0;
	private Camera.Parameters parameters;
	private Camera mCamera;
	private SurfaceHolder holder;
	private boolean mFlag = false;
	private int mCameraId = 0;
	private static final int BACK_CAMERA = 0;
	private static final int ROTATION = 90;
	private static final int REVERT = 180;
	private int mZoomMax;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//title settings
		mTextView = (TextView) findViewById(R.id.textView1);
		mTextView.setText("1.0X");
		setTitle(getResources().getText(R.string.app_name));
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		mSeekBar = (SeekBar) findViewById(R.id.seekBar);

		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.i(TAG, "stop tracking touch");
				parameters.setZoom(value);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.i(TAG, "start tracking touch");
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				Log.i(TAG, "progress changed");
				value = progress;
				mTextView.setText(value + 1 + ".0X");
			}
		});
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		startCamera();	
	}

	private void startCamera() {
		// TODO Auto-generated method stub
		if(mFlag){
			if(mCamera != null){
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
		
		try{
			mCamera = Camera.open(mCameraId);
		}catch(RuntimeException e){
			e.printStackTrace();
			mCamera = null;
		}
		
		if(mCamera != null){
			mCamera.setDisplayOrientation(mCameraId == BACK_CAMERA ? ROTATION : ROTATION);
			parameters = mCamera.getParameters();

			int PreviewWidth = 0;  
			int PreviewHeight = 0;

			List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes(); 
			int lengthList = previewSizes.size();
			Log.i(TAG, "startCamera sizeList.size = " + lengthList);
 
			if (lengthList > 1) {  
				//
				/*for (int i = 0; i < lengthList; i++) {  
					Log.i(TAG,"SupportedPreviewSizes : " + previewSizes.get(i).width + "x" + previewSizes.get(i).height);  
					} 
				*/
				Iterator<Camera.Size> itor = previewSizes.iterator();  
				while (itor.hasNext()) {  
					Camera.Size cur = itor.next();  
					if (cur.width >= PreviewWidth  && cur.height >= PreviewHeight) {  
						PreviewWidth = cur.width;  
						PreviewHeight = cur.height;  
						break;  
					}  
				}  
			}  
			parameters.setPreviewSize(PreviewWidth, PreviewHeight);  
			parameters.set("orientation", "portrait");
			parameters.setRotation(mCameraId == BACK_CAMERA ? ROTATION : REVERT + ROTATION);
			
			mZoomMax = value;
			parameters.setZoom(mZoomMax);
			mCamera.setParameters(parameters);
			
			try{
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
				mFlag = true;
			}catch(Exception e){
				mCamera.release();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		startCamera();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera.stopPreview();  
		mCamera.release();  
		mCamera=null; 
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.main, menu);
	// return true;
	// }
}
