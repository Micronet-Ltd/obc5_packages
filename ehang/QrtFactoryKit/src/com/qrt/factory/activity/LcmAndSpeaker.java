/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.activity;

import com.qrt.factory.R;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LcmAndSpeaker extends AbstractActivity {

    private static final String TAG = "LCM Test";

    private int brightnessState = 0, imgId = 0, mImgResId = -1;

    private float mBrightness = 1.0f;

    private WindowManager.LayoutParams mLayoutParams;

    private boolean ifLocked = false;

    private PowerManager.WakeLock mWakeLock;

    private PowerManager mPowerManager;

    private LinearLayout mLinearLayout;

    private Bitmap mBm;

	private float MaxVol= 1.0f;
	
	private float MinVol= 0.0f;

//    private int[] mTestImg = {
//            R.drawable.lcm_red, R.drawable.lcm_green, R.drawable.lcm_blue,
//            R.drawable.lcm_white,
//            R.drawable.lcm_black, R.drawable.lcm_stripe,
//            R.drawable.lcm_black_white_lump,
//            R.drawable.lcm_color_lump
//    };

    private int[] mTestImg = null;


    private MediaPlayer mMediaPlayer = new MediaPlayer();

    boolean isPlaying = false;

    private AudioManager mAudioManager;

    private Context mContext;

    private int mDefaultMode;

    private Map<Integer, Integer> mDefaultAudioVolume
            = new HashMap<Integer, Integer>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
				 /*qrt added by xuegang for Double Speaker 20150407 begin*/
				if(1 == getDoubleSpeakerType())
				{
					lcmTestFinishForDSpeaker(true,true);
				}
				else
				{
					lcmTestFinish(true);
				}  
				 /*qrt added by xuegang for Double Speaker 20150407 end*/
            } else if (msg.what == 1) {
                /*qrt added by xuegang for Double Speaker 20150407 begin*/
				if(1 == getDoubleSpeakerType())
				{
					lcmTestFinishForDSpeaker(false,true);
				}
				else
				{
					lcmTestFinish(false);
				}   
            } else if (msg.what == 2) {
                lcmTestFinishForDSpeaker(true,false);
            }
			 /*qrt added by xuegang for Double Speaker 20150407 end*/
        }
    };

    private boolean lcmTestPass;
    private boolean leftSpeakerTestPass;
    private boolean rightSpeakerTestPass;
    private boolean testFinished;

    private void lcmTestFinish(boolean isPass) {
        mResultBuffer.append("lcm Test " + (isPass ? "pass" : "fail"));
        lcmTestPass = isPass;

        if (!isFinishing()) {
            AlertDialog alertDialog = createConfirmDialog(LcmAndSpeaker.this,
                    getString(R.string.speaker_confirm),
                    getString(R.string.pass),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            speakerTestFinish(true);
                        }
                    }, getString(R.string.fail),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            speakerTestFinish(false);
                        }
                    }
            );
            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i,
                        KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        int keyCode = keyEvent.getKeyCode();
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            dialogInterface.dismiss();
                            speakerTestFinish(false);
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            dialogInterface.dismiss();
                            speakerTestFinish(true);
                            return true;
                        }
                    }
                    return false;
                }
            });
            alertDialog.show();
        }
    }

    /*qrt added by xuegang for Double Speaker 20150407 begin*/
    private void lcmTestFinishForDSpeaker(boolean isPass,boolean isLeft) {
        if (!isFinishing()&&isLeft) {
			mResultBuffer.append("lcm Test " + (isPass ? "pass" : "fail"));
            lcmTestPass = isPass;
			
            AlertDialog alertDialog = createConfirmDialog(LcmAndSpeaker.this,
                    getString(R.string.left_speaker_confirm),
                    getString(R.string.pass),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            DspeakerTestFinish(true,true);
							mHandler.sendEmptyMessage(2);
                        }
                    }, getString(R.string.fail),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            DspeakerTestFinish(false,true);
							mHandler.sendEmptyMessage(2);
                        }
                    }
            );
            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i,
                        KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        int keyCode = keyEvent.getKeyCode();
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            dialogInterface.dismiss();
                            DspeakerTestFinish(false,true);
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            dialogInterface.dismiss();
                            DspeakerTestFinish(true,true);
                            return true;
                        }
                    }
                    return false;
                }
            });
            alertDialog.show();
        }
		else {
			testFinished = true;
            AlertDialog alertDialog = createConfirmDialog(LcmAndSpeaker.this,
                    getString(R.string.right_speaker_confirm),
                    getString(R.string.pass),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            DspeakerTestFinish(true,false);
                        }
                    }, getString(R.string.fail),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface,
                                int i) {
                            DspeakerTestFinish(false,false);
                        }
                    }
            );
            alertDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i,
                        KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        int keyCode = keyEvent.getKeyCode();
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            dialogInterface.dismiss();
                            DspeakerTestFinish(false,false);
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            dialogInterface.dismiss();
                            DspeakerTestFinish(true,false);
                            return true;
                        }
                    }
                    return false;
                }
            });
            alertDialog.show();
        }
			
    }
	/*qrt added by xuegang for Double Speaker 20150407 end*/


    private void speakerTestFinish(boolean isPass) {
        mResultBuffer.append("\nSpeaker Test " + (isPass ? "pass" : "fail"));

        if (isPass && lcmTestPass) {
            pass();
        } else {
            fail();
        }
    }
	/*qrt added by xuegang for Double Speaker 20150407 begin*/
    private void DspeakerTestFinish(boolean isPass,boolean isLeft) {
		if(isLeft)
		{
            mResultBuffer.append("Left Speaker Test " + (isPass ? "pass" : "fail"));
			if(isPass)
			{
			    leftSpeakerTestPass = true;
			}
			else 
				leftSpeakerTestPass = false;

			mMediaPlayer.setVolume(MinVol,MaxVol);
		}
		else
		{
            mResultBuffer.append("Right Speaker Test " + (isPass ? "pass" : "fail"));
			if(isPass) rightSpeakerTestPass = true;else rightSpeakerTestPass = false;
		}

        if (isPass) {
			if(leftSpeakerTestPass && rightSpeakerTestPass && lcmTestPass)
			{
                pass();
			}
			else if(testFinished && !(leftSpeakerTestPass && rightSpeakerTestPass && lcmTestPass))
			{
                fail();
			}	
        }
		else
		{
            if(testFinished && !(leftSpeakerTestPass && rightSpeakerTestPass && lcmTestPass))
			{
                fail();
			}			
		}
    }
	/*qrt added by xuegang for Double Speaker 20150407 end*/


    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.lcm);

        mLayoutParams = getWindow().getAttributes();
        mLayoutParams.screenBrightness = 1;
        getWindow().setAttributes(mLayoutParams);

        mLinearLayout = (LinearLayout) findViewById(R.id.myLinearLayout1);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "BackLight");

        String lcmTestModel = getLcmTestMode();

        if ("0".equals(lcmTestModel)) {
            mTestImg = new int[]{R.drawable.color_all};
        } else if ("1".equals(lcmTestModel)) {
        /*begin : modified by tianfangzhou for yunsun test RQ,2013.10.11*/
            //Del By Wangwenlong to for yusun test RQ (825) HQ00000000 2013-10-24
            /*mTestImg = new int[]{R.drawable.lcm_red,R.drawable.lcm_white,
            		R.drawable.lcm_yellow, R.drawable.lcm_green , R.drawable.lcm_black};*/
            //Add By Wangwenlong to for yusun test RQ (825) HQ00000000 2013-10-24
            mTestImg = new int[]{R.drawable.lcm_red,R.drawable.lcm_white,
                    R.drawable.lcm_blue, R.drawable.lcm_green , R.drawable.lcm_black};
         /*end : modified by tianfangzhou for yunsun test RQ,2013.10.11*/	    
        }

        start();

        mContext = this;
        isPlaying = false;
        getService();
        getDefaultAudioVolume();
        startPlay();
    }

    private String getLcmTestMode() {
        int resId = 0;
        if ("1".equals(SystemProperties.get("ro.ftmtestmode"))) {
           resId = R.string.default_lcd_for_pcba;
        } else {
            resId = R.string.default_lcd;
        }

        return getString(resId);
    }

    private int getDoubleSpeakerType() {
        int resId = 0;
        if ("1".equals(SystemProperties.get("ro.factory.doublespeaker"))) {
            resId = 1;
        } else {
            resId = 0;
        }

        return resId;
    }


    public void start() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 0);
    }

    private Runnable mRunnable = new Runnable() {

        public void run() {

            if (brightnessState == 0) {
                mBrightness = 0.01f;
                brightnessState = 1;
            } else {
                mBrightness = 1.0f;
                brightnessState = 0;
            }

            Resources resources = getResources();
            try {
                if (mImgResId != imgId && imgId < mTestImg.length
                        && resources != null) {

                    Bitmap bm = getBitpmap(resources, mTestImg[imgId]);
                    BitmapDrawable drawable = new BitmapDrawable(resources,
                            bm);
                    mLinearLayout.setBackgroundDrawable(drawable);

                    mImgResId = imgId;

                    bitmapRecucle();
                    mBm = bm;

                }
            } catch (Exception e) {
                loge(e);
            }

            mLayoutParams = getWindow().getAttributes();
            mLayoutParams.screenBrightness = mBrightness;
            getWindow().setAttributes(mLayoutParams);
            if (brightnessState == 0) {
                if (imgId >= mTestImg.length) {
                    imgId = 0;
                    mImgResId = -1;
                    if (!isFinishing()) {
                        showDialog();
                    }
                }
            }

            mHandler.postDelayed(mRunnable, 1000);
            System.gc();
        }
    };

    private void bitmapRecucle() {
        if (mBm != null) {
            if (!mBm.isRecycled()) {
                mBm.recycle();
                mBm = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bitmapRecucle();
        System.gc();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        final int mAction = event.getAction();
        if ((mAction == MotionEvent.ACTION_UP)) {
            imgId++;
        }
        return true;
    }

    private void showDialog() {
        showPassOrFailDialog(LcmAndSpeaker.this,
                getString(R.string.lcm_confirm),
                getString(R.string.pass),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialoginterface,
                            int i) {
                        mHandler.sendEmptyMessage(0);
                    }
                }, getString(R.string.fail),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialoginterface,
                            int i) {
                        mHandler.sendEmptyMessage(1);
                    }
                }
        );
    }

    @Override
    public void finish() {
        mHandler.removeCallbacks(mRunnable);
        stop();
        setDefaultAudioVolume();
        super.finish();
    }

    @Override
    protected void onResume() {
        wakeLock();
        registerReceiver(headsetRecevier,
                new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        super.onResume();
    }

    @Override
    protected void onPause() {

        wakeUnlock();
        mHandler.removeCallbacks(mRunnable);
        unregisterReceiver(headsetRecevier);
        super.onPause();
    }

    private void wakeLock() {

        if (!ifLocked) {
            ifLocked = true;
            mWakeLock.acquire();
        }
    }

    private void wakeUnlock() {

        if (ifLocked) {
            mWakeLock.release();
            ifLocked = false;
        }
    }


    private Bitmap getBitpmap(Resources resources, int resId) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);
        options.inSampleSize = computeSampleSize(options, 400);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        Bitmap sourceBitmap = BitmapFactory
                .decodeResource(resources, resId, options);
        return sourceBitmap;
    }

    private int computeSampleSize(BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0) {
            return 1;
        }
        if (candidate > 1) {
            if ((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if (candidate > 1) {
            if ((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }

        return candidate;
    }


    private void startPlay() {
        setAudio();
        try {
            play();
        } catch (Exception e) {
            loge(e);
        }
    }

    private final BroadcastReceiver headsetRecevier = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                int state = intent.getIntExtra("state", 0);

                if (state != 1) { // 插入耳机
                    startPlay();
                }
            }
        }
    };

    void play() throws IllegalArgumentException, IllegalStateException,
            IOException {

        isPlaying = true;

        try {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.reset();
                mMediaPlayer = MediaPlayer.create(mContext, R.raw.bordeaux_2);
                mMediaPlayer.setLooping(true);
				 /*qrt added by xuegang for Double Speaker 20150407 begin*/
				if(1 == getDoubleSpeakerType())mMediaPlayer.setVolume(MaxVol,MinVol);
				 /*qrt added by xuegang for Double Speaker 20150407 end*/
                mMediaPlayer.start();
            }
        } catch (Exception e) {
            loge(e);
        }
    }

    void stop() {
        if (isPlaying == true) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            isPlaying = false;
        }
    }

    private void setDefaultAudioVolume() {
        mAudioManager.setMode(mDefaultMode);
        for (Map.Entry<Integer, Integer> entry : mDefaultAudioVolume
                .entrySet()) {
            mAudioManager.setStreamVolume(entry.getKey(), entry.getValue(), 0);
        }
    }

    private void getDefaultAudioVolume() {

        mDefaultMode = mAudioManager.getMode();

        mDefaultAudioVolume.clear();
        mDefaultAudioVolume.put(AudioManager.STREAM_ALARM, mAudioManager
                .getStreamVolume(AudioManager.STREAM_ALARM));
        mDefaultAudioVolume.put(AudioManager.STREAM_MUSIC, mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));
        mDefaultAudioVolume.put(AudioManager.STREAM_VOICE_CALL, mAudioManager
                .getStreamVolume(AudioManager.STREAM_VOICE_CALL));
        mDefaultAudioVolume.put(AudioManager.STREAM_DTMF, mAudioManager
                .getStreamVolume(AudioManager.STREAM_DTMF));
        mDefaultAudioVolume.put(AudioManager.STREAM_NOTIFICATION, mAudioManager
                .getStreamVolume(AudioManager.STREAM_NOTIFICATION));
        mDefaultAudioVolume.put(AudioManager.STREAM_RING, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_RING));
        mDefaultAudioVolume.put(AudioManager.STREAM_SYSTEM, mAudioManager
                .getStreamVolume(AudioManager.STREAM_SYSTEM));
    }


    public void setAudio() {

        mAudioManager.setMode(AudioManager.MODE_RINGTONE);

        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_DTMF), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                mAudioManager
                        .getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
                0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_SYSTEM), 0);
    }

    void getService() {

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }
}
