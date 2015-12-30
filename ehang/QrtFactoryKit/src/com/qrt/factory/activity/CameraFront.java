/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.TestSettings;

import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.os.SystemProperties;

import java.io.IOException;
import java.util.List;

public class CameraFront extends AbstractActivity
        implements SurfaceHolder.Callback {

    private Camera mCamera = null;

    private Button takeButton, passButton, failButton;

    private SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;

    private final static String TAG = "Camera Front Test";
    private int mFrontCameraId;

    @Override
    public void finish() {

        mSurfaceHolder.removeCallback(CameraFront.this);
        stopCamera();
        super.finish();
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                initSurfaceView();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    //    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  //delete by bwq set Orientation change to manifest SW00076602 20140902

        bindView();
    }

    @Override
    protected void onResume() {
        mFrontCameraId = -1;
        initSurfaceView();
        super.onResume();
    }

    private void initSurfaceView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);

//        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mSurfaceView.getLayoutParams();
//        lp.width = 640;
//        lp.height = 480;
//        mSurfaceView.setLayoutParams(lp);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(CameraFront.this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    void bindView() {
        setContentView(R.layout.camera_front);

//        takeButton = (Button) findViewById(R.id.take_picture);
        passButton = (Button) findViewById(R.id.camera_pass);
        failButton = (Button) findViewById(R.id.camera_fail);
//        takeButton.setOnClickListener(new Button.OnClickListener() {
//
//            public void onClick(View arg0) {
//
//                takeButton.setVisibility(View.GONE);
//                try {
//                    if (mCamera != null) {
//                        // mCamera.autoFocus(new AutoFocusCallback());
//                        takePicture();
//                    } else {
//                        fail();
//                    }
//                } catch (Exception e) {
//                    loge(e.toString());
//                }
//            }
//        });
        passButton.setVisibility(View.VISIBLE);
        failButton.setVisibility(View.VISIBLE);

        passButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View arg0) {
                pass();
            }
        });
        failButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View arg0) {
                fail();
            }
        });
    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {

        logd("surfaceCreated");

        /*Add By Wangwenlong to find camera id issue (general)  2013-08-28 begin*/
        try {
//            int mNumberOfCameras = Camera.getNumberOfCameras();
//            Camera.CameraInfo[] mInfo = new Camera.CameraInfo[mNumberOfCameras];
//            for (int i = 0; i < mNumberOfCameras; i++) {
//                mInfo[i] = new Camera.CameraInfo();
//                android.hardware.Camera.getCameraInfo(i, mInfo[i]);
//            }
//
//            for (int i = 0; i < mNumberOfCameras; i++) {
//                if (mFrontCameraId == -1 && mInfo[i].facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                    mFrontCameraId = i;
//                }
//            }
//
//            if (mFrontCameraId == -1) {
//                showToast(getString(R.string.cameraback_fail_open));
//                mCamera = null;
//            } else {
                mCamera = Camera.open(1);
//            }
            /*Add By Wangwenlong to find camera id issue (general)  2013-08-28 end*/
        } catch (Exception exception) {
            showToast(getString(R.string.cameraback_fail_open));
            mCamera = null;
        }

        if (mCamera == null) {
            fail();
        } else {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);


                setPreviewSize(parameters);



                mCamera.setParameters(parameters);
                mCamera.startPreview();

            } catch (Exception exception) {
                mCamera.release();
                mCamera = null;
                fail();
            }
        }
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w,
            int h) {
        logd("surfaceChanged");
        //startCamera(w, h);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {

        logd("surfaceDestroyed");
        stopCamera();
    }

    private void takePicture() {

        logd("takePicture");
        if (mCamera != null) {
            try {
                mCamera.takePicture(mShutterCallback, rawPictureCallback,
                        jpegCallback);
            } catch (Exception e) {
                fail();
            }
        } else {
            fail();
        }
    }

    private ShutterCallback mShutterCallback = new ShutterCallback() {

        public void onShutter() {

            logd("onShutter");
        }
    };

    private PictureCallback rawPictureCallback = new PictureCallback() {

        public void onPictureTaken(byte[] _data, Camera _camera) {

        }
    };

    private PictureCallback jpegCallback = new PictureCallback() {

        public void onPictureTaken(byte[] _data, Camera _camera) {

            try {
                takeButton.setVisibility(View.GONE);
                passButton.setVisibility(View.VISIBLE);
                failButton.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                loge(e);
            }
        }
    };

    public final class AutoFocusCallback
            implements Camera.AutoFocusCallback {

        public void onAutoFocus(boolean focused, Camera camera) {

            if (focused) {
                takePicture();
            }
        }
    }

    private void startCamera(int w, int h) {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);

                setPreviewSize(parameters);

                mCamera.setParameters(parameters);
                mCamera.startPreview();
//                mCamera.setDisplayOrientation(180);
            } catch (Exception e) {
                loge(e);
                fail();
            }
        }
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int w = mDisplayMetrics.widthPixels;
        int h = mDisplayMetrics.heightPixels;
        logd("surfaceSize = " + w + " * " + h);
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
            logd("supportedPreviewSize = " + supportedPreviewSize.width + " * "
                    + supportedPreviewSize.height);
            if (w >= supportedPreviewSize.width &&
                    h >= supportedPreviewSize.height){
                setLayoutParams(parameters, supportedPreviewSize.height, supportedPreviewSize.width);
                return;
            } else if(w >= supportedPreviewSize.height &&
                    h >= supportedPreviewSize.width){

                setLayoutParams(parameters, supportedPreviewSize.width, supportedPreviewSize.height);
                return;
            }
        }
    }

    private void setLayoutParams(Camera.Parameters parameters, int w, int h) {
        parameters.setPreviewSize(h,
                w);
        ViewGroup.LayoutParams layoutParams = mSurfaceView.getLayoutParams();

		/*qrt added by xuegang for P4 factory test 20150409 begin*/

		{
		    layoutParams.width = h;
            layoutParams.height = w;
		}
		/*qrt added by xuegang for P4 factory test 20150409 end*/

		

        mSurfaceView.setLayoutParams(layoutParams);
    }

    private void stopCamera() {

        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
            } catch (Exception e) {
                loge(e);
            }
        }
    }
}
