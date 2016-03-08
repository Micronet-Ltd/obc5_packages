package com.yihang.gesture.activity;

import com.yihang.gesture.R;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.yihang.gesture.service.ExitApp;

public class ShowGesture extends Activity {
	private ImageView gesture;
	private String TAG = "ShowGesture";
	private Integer code = 0;
	private boolean isRunningTop = false;
	AnimationDrawable anim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		code = intent.getIntExtra("code", 0);
		isRunningTop = intent.getBooleanExtra("isRunningTop", false);
		Log.v(TAG, "code = " + code);
		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.showgesture_bg);
		gesture = (ImageView) findViewById(R.id.gesture_img);
		ExitApp.getInstance().addActivity(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AlphaAnimation aas = new AlphaAnimation(1.0f, 1.0f);
		aas.setDuration(600);
		aas.setRepeatCount(1);
		aas.setAnimationListener(mListener);		
		gesture.startAnimation(aas);
	}
	
	AnimationListener mListener = new AnimationListener() {
		@Override
		public void onAnimationEnd(Animation arg0) {
			gesture.setVisibility(View.GONE);
			anim.stop();
			Intent i;
			KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
			boolean isSecure = mKeyguardManager.isKeyguardSecure();
			Bundle bundle = new Bundle();
			if (code == 52) {
				if (isSecure) {
					i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
					bundle.putBoolean("OpenWithGesture", true);
					i.putExtras(bundle);
				} else {
					i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
				}
				i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
			if (code == 50) {
				i = new Intent();
				i.setClassName("com.android.calculator2", "com.android.calculator2.Calculator");
				i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if (isSecure) {
					bundle.putBoolean("OpenWithGesture", true);
					i.putExtras(bundle);
				}
				startActivity(i);
			}
			if(isRunningTop){
				finish();
			}else{
				ExitApp.getInstance().exit();
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationStart(Animation animation) {
			if (code == 52) {
				gesture.setBackgroundResource(R.anim.draw_circle);
			} else if (code == 50) {
				gesture.setBackgroundResource(R.anim.draw_m);
			} else {
				finish();
				return;
			}

			anim = (AnimationDrawable) gesture.getBackground();
			anim.start();
		}
	};
}
