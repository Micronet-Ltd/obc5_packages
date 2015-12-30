package com.vendor.setprop;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.net.Uri;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: jiayugang
 * Date: 20140624
 */
public class SetPropActivity extends Activity {

    /*modify by jiayugang add savelog (general) 20140714 begin*/
    private static final String TAG = "SetPropActivity";
    private static final String ACTION_MEDIA_SCANNER_SCAN_ALL ="com.android.fileexplorer.action.MEDIA_SCANNER_SCAN_ALL";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || getIntent().getAction() == null) {
            finish();
            return;
        }
        int resId = 0;
        String prop ="";
        String inputString = getIntent().getStringExtra("inputString");
        if(inputString.equals(this.getString(R.string.set_adb_prop_display))) {
            resId = R.string.adb_enabled_msg;
            prop = "persist.sys.adb.enable";
            showDialog(resId, prop);
        } else if (inputString == null || inputString.length() == 0) {
            finish();
            return;
        } else if (inputString.equals(getString(R.string.set_aplog_code))) {
            setTitle(R.string.log_activity_name);

            new AlertDialog.Builder(SetPropActivity.this)
                    .setTitle(R.string.log_activity_name)
                    .setPositiveButton(R.string.log_activity_enabled, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                int i) {
                            SystemProperties.set("persist.sys.debug.getaplog", "1");
                            showDialog(true);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.log_activity_disabled, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                int i) {
                            SystemProperties.set("persist.sys.debug.getaplog", "0");
                            updateMTPDB("/storage/sdcard0/aplog");
                            showDialog(false);
                            dialogInterface.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else if (inputString.equals(getString(R.string.set_qxdmlog_code))) {
            setTitle(R.string.qxdmlog_activity_name);
            new AlertDialog.Builder(SetPropActivity.this)
                    .setTitle(R.string.qxdmlog_activity_name)
                    .setPositiveButton(R.string.log_activity_enabled, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                int i) {
                            SystemProperties.set("persist.sys.debug.getqxdmlog", "1");
                            Toast.makeText(SetPropActivity.this, R.string.log_activity_pass_name, Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.log_activity_disabled, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                int i) {
                            SystemProperties.set("persist.sys.debug.getqxdmlog", "0");
                            updateMTPDB("/storage/sdcard0/qxdm_log");
                            Toast.makeText(SetPropActivity.this, R.string.log_activity_pass_name, Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else if (inputString.equals(getString(R.string.set_usb_sn_code))) {
            setTitle(R.string.set_unique_sn_activity_name);

            new AlertDialog.Builder(SetPropActivity.this)
                    .setTitle(R.string.set_unique_sn_activity_name)
                    .setPositiveButton(R.string.log_activity_enabled, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                int i) {
                            writeUniqSerialNum(true);
                            Toast.makeText(SetPropActivity.this, R.string.log_activity_pass_name, Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.log_activity_disabled, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface,
                                int i) {
                            writeUniqSerialNum(false);
                            Toast.makeText(SetPropActivity.this, R.string.log_activity_pass_name, Toast.LENGTH_SHORT).show();
                            dialogInterface.dismiss();
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            finish();
        }
    }

    /**
     * Name: writeUniqSerialNum
     * Author: xingle
     * Date: 20140510
     * Purpose: Write USB serial number
     */
    private void writeUniqSerialNum(boolean unique) {
        /*modify by jiayugang add serialno (ql1700) 201400808 begin*/
        /*modify by jiayugang add serialno (genearl) 201400819 begin*/
        String sns = unique ? "1" : "0";
        FileOutputStream fouts = null;
        try {
            fouts = new FileOutputStream("/persist/serialno");
            byte[] bytess = sns.getBytes();
            fouts.write(bytess);
            fouts.flush();
            SystemProperties.set("persist.sys.serialno", unique ? "1" : "0");
        } catch (Exception e){
            Log.e(TAG, "write error " + unique, e);
        } finally {
            if (fouts != null) {
                try {
                    fouts.close();
                } catch (IOException e) {
                    fouts = null;
                }
            }
        }
        /*modify by jiayugang add serialno (genearl) 201400819 end*/
        /*modify by jiayugang add serialno (ql1700) 20140808 end*/
    }

    /***********************************************************
    Name: showDialog
    Author: xingle
    Date: 2013/07/29
    Purpose: show dialog to tell user to reboot
    ***********************************************************/
    private void showDialog(boolean enabled) {
        int resId = enabled ? R.string.dump_enabled_msg : R.string.dump_disabled_msg;
        new AlertDialog.Builder(SetPropActivity.this)
                .setMessage(resId)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,
                            int i) {
                        Toast.makeText(SetPropActivity.this, R.string.log_activity_pass_name, Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Name: updateMTPDB
     * Author: yuyang
     * Date: 20140714
     * Purpose: update MTP DB
     */
    private void updateMTPDB(String path){
        Uri uri = Uri.parse("file://"+path);
        Log.d(TAG,"updateMTPDB  file: "+uri);
        this.sendBroadcast(new Intent( ACTION_MEDIA_SCANNER_SCAN_ALL, uri));
    }

    private void showDialog(int resId, final String prop) {
        new AlertDialog.Builder(SetPropActivity.this)
                .setMessage(resId)
                .setPositiveButton(R.string.activity_enabled, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,
                            int i) {
                        SystemProperties.set(prop, "true");
                        Toast.makeText(SetPropActivity.this, R.string.activity_pass_name, Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(R.string.activity_disabled, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface,
                            int i) {
                        SystemProperties.set(prop, "false");
                        Toast.makeText(SetPropActivity.this, R.string.activity_pass_name, Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
    /*modify by jiayugang add savelog (general) 20140714 end*/
}