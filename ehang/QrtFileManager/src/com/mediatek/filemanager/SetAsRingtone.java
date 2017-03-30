package com.mediatek.filemanager;

import com.mediatek.filemanager.utils.FileUtils;
import com.mediatek.filemanager.utils.RingtoneUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.telephony.TelephonyManager;
import android.os.SystemProperties;
import com.android.internal.telephony.PhoneConstants;
/**
 * @author joez
 *
 */
public class SetAsRingtone extends Activity implements DialogInterface.OnCancelListener,
        DialogInterface.OnDismissListener {

    private static final String TAG = "FileManager.SetAsRingtone";

    private static final int DIALOG_CHOICE = 1;
    
    private int mType = RingtoneManager.TYPE_RINGTONE;
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        showDialog(DIALOG_CHOICE);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CHOICE: {
                Resources res = getResources();
                final int[] types;
                final CharSequence[] typeTitles;
				if(TelephonyManager.getDefault().isMultiSimEnabled()){				   
                    types = new int[]{
                             RingtoneManager.TYPE_RINGTONE,
                             RingtoneUtils.SIM2,
                             RingtoneManager.TYPE_NOTIFICATION,
                             RingtoneManager.TYPE_ALARM
                     };
                    typeTitles = new CharSequence[] {
                             res.getString(R.string.type_phone1),
                             res.getString(R.string.type_phone2),
                             res.getString(R.string.type_notification),
                             res.getString(R.string.type_alarm)
                    };
                }else{
					    types = new int[]{
                                RingtoneManager.TYPE_RINGTONE,
                                RingtoneManager.TYPE_NOTIFICATION,
                                RingtoneManager.TYPE_ALARM
                        };
                        typeTitles = new CharSequence[] {
                                res.getString(R.string.type_phone),
                                res.getString(R.string.type_notification),
                                res.getString(R.string.type_alarm)
                        };

				}
                AlertDialog dialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setTitle(getString(R.string.setas_ringtone)) // Modified by sunyaxi to apply a default theme for dialog (general) SW00062461 2014-07-09
                .setSingleChoiceItems(typeTitles, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mType = types[which];
                        done();
                    }
                }).create();
                dialog.setOnCancelListener(this);
                dialog.setOnDismissListener(this);
                
                return dialog;
            }
        }
        
        return null;
    }
    
    private void done() {
        Uri data = getIntent().getData();
        
        Log.d(TAG, "done:{ uri:" + data + ", type:" + mType + " }");
        
        Uri uri = RingtoneUtils.setAsRingtone(this, FileUtils.getFileFromUri(data), mType);
		
		if (uri != null) { 
            if (mType == RingtoneUtils.SIM2) {
				RingtoneManager.setActualRingtoneUriBySubId(this, PhoneConstants.SUB2, uri);
			} else {
				RingtoneManager.setActualDefaultRingtoneUri(this, mType, uri);
			}
        }		
        finish();
    }
    
    public void onCancel(DialogInterface dialog) {
        finish();
    }
    
    public void onDismiss(DialogInterface dialog) {
        if (!isFinishing()) {
            finish();
        }        
    }
    
    @Override
    public boolean onSearchRequested() {
        return false;
    }
}
