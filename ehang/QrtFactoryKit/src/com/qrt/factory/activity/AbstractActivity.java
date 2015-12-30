package com.qrt.factory.activity;

import com.qrt.factory.util.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

/**
 * Created by IntelliJ IDEA. User: wangwenlong Date: 12-1-11 Time: 上午10:54 To
 * change this template use File | Settings | File Templates.
 */
abstract public class AbstractActivity extends Activity {

    private static final int[] DIALOG_BUTTON_ID = {AlertDialog.BUTTON_POSITIVE,
            AlertDialog.BUTTON_NEGATIVE, AlertDialog.BUTTON_NEUTRAL};

    protected StringBuffer mResultBuffer = new StringBuffer();

    long startTime,endTime;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
    Float useTime;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
    protected final DialogInterface.OnShowListener mDialogOnShowListener
            = new DialogInterface.OnShowListener() {

        @Override
        public void onShow(DialogInterface dialog) {

            for (int buttonId : DIALOG_BUTTON_ID) {
                float fPx = TypedValue
                        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80,
                                getResources().getDisplayMetrics());

                int iPx = Math.round(fPx);

                Button button = ((AlertDialog) dialog)
                        .getButton(buttonId);

                if (button != null) {
                    button.setHeight(iPx);
                }
            }
        }
    };

    protected abstract String getTag();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
    }

    @Override
    public void finish() {
        mResultBuffer = new StringBuffer();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        fail();
        super.onBackPressed();
    }

    protected void fail() {
        if (!isFinishing()) {
            endTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            useTime = Float.parseFloat(String.valueOf(endTime - startTime)) / 1000.0f;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            Intent intent = new Intent();
            intent.putExtra("TAG", getTag());
            intent.putExtra("RESULT", mResultBuffer.toString());
            intent.putExtra("TIME", useTime.toString());//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }

    protected void pass() {
        if (!isFinishing()) {
            endTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            useTime = Float.parseFloat(String.valueOf(endTime - startTime)) / 1000.0f;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            Intent intent = new Intent();
            intent.putExtra("TAG", getTag());
            intent.putExtra("RESULT", mResultBuffer.toString());
            intent.putExtra("TIME", useTime.toString());//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    protected void retest() {
        if (!isFinishing()) {
//            showToast(Utilities.RESULT_PASS);
            endTime = System.currentTimeMillis();//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            useTime = Float.parseFloat(String.valueOf(endTime - startTime)) / 1000.0f;//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            Intent intent = new Intent();
            intent.putExtra("TAG", getTag());
            intent.putExtra("RESULT", mResultBuffer.toString());
            intent.putExtra("TIME", useTime.toString());//Add by zhangkaikai for QW810 Factorylog 2014-10-17
            setResult(2, intent);
            finish();
        }
    }

    protected void showToast(Object s) {
        if (s != null && !isFinishing()) {
            Toast.makeText(this, s.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    protected void logd(Object d) {
        Utilities.logd(getTag(), d);
    }

    protected void loge(Object e) {
        Utilities.loge(getTag(), e);
    }

    protected AlertDialog createConfirmDialog(Context context, String title,
            String positiveButtonString,
            DialogInterface.OnClickListener positiveButtonListener,
            String negativeButtonString,
            DialogInterface.OnClickListener negativeButtonListener) {
//        AlertDialog alertDialog = new MyAlertDialog.Builder(context)
//                .setTitle(title)
//                .setPositiveButton(
//                        positiveButtonString,
//                        positiveButtonListener)
//                .setNegativeButton(negativeButtonString,
//                        negativeButtonListener).create();

        AlertDialog alertDialog = new MyAlertDialog(context);
        alertDialog.setTitle(title);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonString,
                positiveButtonListener);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButtonString,
                negativeButtonListener);

        alertDialog.setOnShowListener(mDialogOnShowListener); 
        alertDialog.setCanceledOnTouchOutside(false);//add by tianfangzhou ,20140116
        return alertDialog;
    }

/*    protected void showWarningDialog(Context context, String title) {

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {

                            }
                        }).create();
        alertDialog.setOnShowListener(mDialogOnShowListener);
        alertDialog.show();
    }*/

    protected void showPassOrFailDialog(Context context, String title,
            String positiveButtonString,
            DialogInterface.OnClickListener positiveButtonListener,
            String negativeButtonString,
            DialogInterface.OnClickListener negativeButtonListener) {
        AlertDialog alertDialog = createConfirmDialog(context, title,
                positiveButtonString,
                positiveButtonListener, negativeButtonString,
                negativeButtonListener);
        alertDialog.setCancelable(false);		
		alertDialog.setCanceledOnTouchOutside(false); //add by tianfangzhou ,20140116
        alertDialog.show();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    protected class MyProgressDialog extends ProgressDialog {

        public MyProgressDialog(Context context) {
            super(context);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
                    || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    protected class MyAlertDialog extends AlertDialog {

        protected MyAlertDialog(Context context) {
            super(context);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
                    || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                return true;
            }
            return super.onKeyUp(keyCode, event);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }
    }
}