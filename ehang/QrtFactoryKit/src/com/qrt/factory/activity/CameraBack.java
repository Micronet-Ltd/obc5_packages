/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.qrt.factory.activity;

import com.qrt.factory.R;
import com.qrt.factory.TestSettings;
import com.qrt.factory.util.Utilities;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraBack extends AbstractActivity
        implements SurfaceHolder.Callback {

    private final static String TAG = "Camera Back Test";

    private static final String FLASHLIGHT_NODE = "/sys/class/leds/flashlight/brightness";

    private Camera mCamera = null;

    private Button takeButton, passButton, failButton;

    private SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (0 == msg.what) {
                //Del By Wangwenlong to change flashligth method issue (general) HQ00000000 2013-09-14
                /*writeFlashlight(true)*/

                if (!isFinishing()) {
                    showPassOrFailDialog(CameraBack.this,
                            getString(R.string.flashlight_confirm),
                            getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mResultBuffer.append("flashlight Test pass");
                                    mFlashlightPass = true;
                                }
                            },
                            getString(R.string.no),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    mResultBuffer.append("flashlight Test fail");
                                    mFlashlightPass = false;
                                }
                            });
                }
            }
        }
    };

    private boolean mFlashlightPass;
    private int mBackCameraId;

    @Override
    public void finish() {
        logd("finish");
        mSurfaceHolder.removeCallback(CameraBack.this);
        stopCamera();
        //Del By Wangwenlong to change flashligth method issue (general) HQ00000000 2013-09-14
        //writeFlashlight(false);
        mFlashlightPass = false;
        super.finish();
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);    //delete by bwq set Orientation change to manifest SW00072018 20140815
        setContentView(R.layout.camera_back);
        mFlashlightPass = false;
        bindView();
        logd("create");
    }

    @Override
    protected void onResume() {
        /* SurfaceHolder set */
        mBackCameraId = -1;
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(CameraBack.this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        super.onResume();
        logd("resume");
    }

    void bindView() {

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
//                        mCamera.autoFocus(new AutoFocusCallback());
////                        takePicture();
//                    } else {
//                        mResultBuffer.append("Camera not found");
//                        fail();
//                    }
//                } catch (Exception e) {
//                    loge(e);
//                }
//            }
//        });

//        takeButton.setVisibility(View.GONE);
        passButton.setVisibility(View.VISIBLE);
        failButton.setVisibility(View.VISIBLE);

        passButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View arg0) {
                mResultBuffer.append("\nCameraBack Test  pass");
                if (mFlashlightPass) {
                    pass();
                } else {
                    fail();
                }
            }
        });
        failButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View arg0) {
                mResultBuffer.append("\nCameraBack Test fail");
                fail();
            }
        });

    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {

        logd("surfaceCreated");
        try {
            /*Add By Wangwenlong to find camera id issue (general)  2013-08-28 begin*/
            int mNumberOfCameras = Camera.getNumberOfCameras();
            CameraInfo[] mInfo = new CameraInfo[mNumberOfCameras];
            for (int i = 0; i < mNumberOfCameras; i++) {
                mInfo[i] = new CameraInfo();
                android.hardware.Camera.getCameraInfo(i, mInfo[i]);
            }

            for (int i = 0; i < mNumberOfCameras; i++) {
                if (mBackCameraId == -1 && mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                    mBackCameraId = i;
                }
            }

            if (mBackCameraId == -1) {
                showToast(getString(R.string.cameraback_fail_open));
                mCamera = null;
            } else {
                mCamera = Camera.open(mBackCameraId);
            }
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
        startCamera();
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

    private void startCamera() {

        logd("startCamera");
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
                //parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                setPreviewSize(parameters);
                mCamera.setParameters(parameters);
//                mCamera.setDisplayOrientation(180);//deleted by tianfangzhou for preview ,SW00015286,2013.10.11
                mCamera.startPreview();
            } catch (Exception e) {
                loge(e);
                fail();
            }
            mHandler.sendEmptyMessageDelayed(0, 500);
        }
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        List<Camera.Size> supportedPreviewSizes = parameters
                .getSupportedPreviewSizes();
        for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
            if (mDisplayMetrics.widthPixels >= supportedPreviewSize.width &&
                    mDisplayMetrics.heightPixels
                            >= supportedPreviewSize.height) {
                parameters.setPreviewSize(supportedPreviewSize.width,
                        supportedPreviewSize.height);
                return;
            }
        }
    }

    private void stopCamera() {
        logd("stopCamera");
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
                mCamera.release();
            } catch (Exception e) {
                loge(e);
            }
        }
    }

    //Del By Wangwenlong to change flashligth method issue (general) HQ00000000 2013-09-14 Begin
    /*private void writeFlashlight(boolean on) {
        FileOutputStream flashlight = null;
        try {
            flashlight = new FileOutputStream(FLASHLIGHT_NODE);
            if (on) {
            flashlight.write(TestSettings.LIGHT_ON);
        } else {
            flashlight.write(TestSettings.LIGHT_OFF);
        }
        
        flashlight.close();
        } catch (Exception e) {
            loge(e);
            if (flashlight != null) {
                try {
                    flashlight.close();
                } catch (IOException e1) {
                    flashlight = null;
                }
            }
        }
    }*/
    //Del By Wangwenlong to change flashligth method issue (general) HQ00000000 2013-09-14 End
}
