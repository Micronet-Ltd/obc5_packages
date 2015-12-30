/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.util;


import android.content.Context;
import android.media.AudioManager;
import android.content.Intent;
import android.util.Log;
import android.os.Handler;
import qcom.fmradio.FmConfig;
import qcom.fmradio.FmReceiver;
import qcom.fmradio.FmRxEvCallbacksAdaptor;
import qcom.fmradio.FmTransceiver;
import android.media.AudioSystem;
import android.os.*;

public class FmManager {

    private static String TAG = "FM";



    private boolean mFmOn = false;

    Context mContext = null;

    private Handler mHandler;
    private static final String FMRADIO_DEVICE_FD_STRING = "/dev/radio0";
    private FmReceiver mReceiver;

    private boolean mOverA2DP = false;

    public FmManager(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }


    private FmConfig getFmDefConfig() {

        FmConfig mFmConfig = new FmConfig();
        mFmConfig.setRadioBand(FmTransceiver.FM_USER_DEFINED_BAND);
        mFmConfig.setEmphasis(FmTransceiver.FM_DE_EMP50);
        mFmConfig.setChSpacing(FmTransceiver.FM_CHSPACE_100_KHZ);
        mFmConfig.setRdsStd(FmTransceiver.FM_RDS_STD_NONE);
        mFmConfig.setLowerLimit(87500);
        mFmConfig.setUpperLimit(108000);
        return mFmConfig;
    }

    private int frequency;

    FmRxEvCallbacksAdaptor mFmRxEvCallbacksAdaptor
            = new FmRxEvCallbacksAdaptor() {
        public void FmRxEvSearchComplete(int freq) {
            FmManager.this.frequency = freq;
            mHandler.sendEmptyMessage(5);
        }
        
        public void FmRxEvRadioTuneStatus(int freq) {
            FmManager.this.frequency = freq;
            mHandler.sendEmptyMessage(5);
        }
    };
	/*mod by baiwuqiang for after fmtest receiver no voice SW00065030 20140716 begin*/
    public boolean isFmOn() {

        return mFmOn;
    }

    public boolean searchUP() {
        /*Add by danghao for resolve nullpointException (x310) hq00000000 Begin*/
        if (mReceiver != null) {
            return mReceiver.searchStations(FmReceiver.FM_RX_SRCH_MODE_SEEK,
                    FmReceiver.FM_RX_DWELL_PERIOD_1S,
                    FmReceiver.FM_RX_SEARCHDIR_UP);
        }
        return false;
        /*Add by danghao for resolve nullpointException (x310) hq00000000 End*/
    }

    public int getFreq() {
        return frequency;
    }

    public boolean setFreq(int freq) {
        if (mReceiver != null) {
            return mReceiver.setStation(freq);
        }
        return false;
    }


    private void startFM() {
        Log.d(TAG, "In startFM");
        AudioManager audioManager = (AudioManager) mContext.getSystemService(
                Context.AUDIO_SERVICE);

        Log.d(TAG, "FMRadio: Requesting to start FM");
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_FM,
                AudioSystem.DEVICE_STATE_AVAILABLE, "");
        AudioSystem
                .setForceUse(AudioSystem.FOR_MEDIA, AudioSystem.FORCE_SPEAKER);
    }

    private void stopFM() {
        Log.d(TAG, "FMRadio: Requesting to stop FM");
        AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_FM,
                AudioSystem.DEVICE_STATE_UNAVAILABLE, "");

        AudioManager audioManager = (AudioManager) mContext.getSystemService(
                Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(null);
    }

    /*
     * Turn ON FM: Powers up FM hardware, and initializes the FM module
     *                                                                                 .
     * @return true if fm Enable api was invoked successfully, false if the api failed.
     */
    public boolean fmOn() {
        boolean bStatus = false;

        if (mReceiver == null) {
            try {
                mReceiver = new FmReceiver(FMRADIO_DEVICE_FD_STRING, mFmRxEvCallbacksAdaptor);
            } catch (InstantiationException e) {
                throw new RuntimeException("FmReceiver service not available!");
            }
        }

        if (mReceiver != null) {
            if (isFmOn()) {
                /* FM Is already on,*/
                bStatus = true;
                Log.d(TAG, "mReceiver.already enabled");
            } else {
                bStatus = mReceiver.enable(getFmDefConfig());
                Log.d(TAG, "mReceiver.enable done, Status :" + bStatus);
            }

            if (bStatus) {
                startFM(); // enable FM Audio only when Call is IDLE
                mFmOn = true;
                AudioManager audioManager = (AudioManager) mContext
                        .getSystemService(
                                Context.AUDIO_SERVICE);
                audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            } else {
                mReceiver = null;
            }
        }
        return bStatus;
    }

    /*
     * Turn OFF FM Operations: This disables all the current FM operations             .
     */
    private void fmOperationsOff() {
        AudioSystem
                .setForceUse(AudioSystem.FOR_MEDIA, AudioSystem.FORCE_NONE);
        stopFM();
    }

    public boolean fmOff() {
        Log.d(TAG, "call fmOff");
        boolean bStatus = false;

        fmOperationsOff();

        // This will disable the FM radio device
        if (mReceiver != null) {
            bStatus = mReceiver.disable();
            mReceiver = null;
            mFmOn = false;
        }
        return bStatus;
    }
/*mod by baiwuqiang for after fmtest receiver no voice SW00065030 20140716 end*/
 
}
