/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package com.android.packageinstaller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.ManifestDigest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.VerificationParams;
import android.graphics.drawable.LevelListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * This activity corresponds to a download progress screen that is displayed 
 * when the user tries
 * to install an application bundled as an apk file. The result of the application install
 * is indicated in the result code that gets set to the corresponding installation status
 * codes defined in PackageManager. If the package being installed already exists,
 * the existing package is replaced with the new one.
 */
public class InstallAppProgress extends Activity implements View.OnClickListener, OnCancelListener {
    private final String TAG="InstallAppProgress";
    private boolean localLOGV = false;
    static final String EXTRA_MANIFEST_DIGEST =
            "com.android.packageinstaller.extras.manifest_digest";
    static final String EXTRA_INSTALL_FLOW_ANALYTICS =
            "com.android.packageinstaller.extras.install_flow_analytics";
    private ApplicationInfo mAppInfo;
    private Uri mPackageURI;
    private InstallFlowAnalytics mInstallFlowAnalytics;
    private ProgressBar mProgressBar;
    private View mOkPanel;
    private TextView mStatusTextView;
    private TextView mExplanationTextView;
    private Button mDoneButton;
    private Button mLaunchButton;
    private final int INSTALL_COMPLETE = 1;
    private Intent mLaunchIntent;
    private static final int DLG_OUT_OF_SPACE = 1;
    private CharSequence mLabel;

    private static final int APP_INSTALL_AUTO = 0;
    private static final int APP_INSTALL_DEVICE = 1;
    private static final int APP_INSTALL_SDCARD = 2;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INSTALL_COMPLETE:
                    mInstallFlowAnalytics.setFlowFinishedWithPackageManagerResult(msg.arg1);
                    if (getIntent().getBooleanExtra(Intent.EXTRA_RETURN_RESULT, false)) {
                        Intent result = new Intent();
                        result.putExtra(Intent.EXTRA_INSTALL_RESULT, msg.arg1);
                        setResult(msg.arg1 == PackageManager.INSTALL_SUCCEEDED
                                ? Activity.RESULT_OK : Activity.RESULT_FIRST_USER,
                                        result);
                        finish();
                        return;
                    }
                    // Update the status text
                    mProgressBar.setVisibility(View.INVISIBLE);
                    // Show the ok button
                    int centerTextLabel;
                    int centerExplanationLabel = -1;
                    LevelListDrawable centerTextDrawable =
                            (LevelListDrawable) getDrawable(R.drawable.ic_result_status);
                    if (msg.arg1 == PackageManager.INSTALL_SUCCEEDED) {
                        mLaunchButton.setVisibility(View.VISIBLE);
                        centerTextDrawable.setLevel(0);
                        centerTextLabel = R.string.install_done;
                        // Enable or disable launch button
                        mLaunchIntent = getPackageManager().getLaunchIntentForPackage(
                                mAppInfo.packageName);
                        boolean enabled = false;
                        if(mLaunchIntent != null) {
                            List<ResolveInfo> list = getPackageManager().
                                    queryIntentActivities(mLaunchIntent, 0);
                            if (list != null && list.size() > 0) {
                                enabled = true;
                            }
                        }
                        if (enabled) {
                            mLaunchButton.setOnClickListener(InstallAppProgress.this);
                        } else {
                            mLaunchButton.setEnabled(false);
                        }
                    } else if (msg.arg1 == PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE){
                        showDialogInner(DLG_OUT_OF_SPACE);
                        return;
                    } else {
                        // Generic error handling for all other error codes.
                        centerTextDrawable.setLevel(1);
                        centerExplanationLabel = getExplanationFromErrorCode(msg.arg1);
                        centerTextLabel = R.string.install_failed;
                        mLaunchButton.setVisibility(View.INVISIBLE);
                    }
                    if (centerTextDrawable != null) {
                    centerTextDrawable.setBounds(0, 0,
                            centerTextDrawable.getIntrinsicWidth(),
                            centerTextDrawable.getIntrinsicHeight());
                        mStatusTextView.setCompoundDrawablesRelative(centerTextDrawable, null,
                                null, null);
                    }
                    mStatusTextView.setText(centerTextLabel);
                    if (centerExplanationLabel != -1) {
                        mExplanationTextView.setText(centerExplanationLabel);
                        mExplanationTextView.setVisibility(View.VISIBLE);
                    } else {
                        mExplanationTextView.setVisibility(View.GONE);
                    }
                    mDoneButton.setOnClickListener(InstallAppProgress.this);
                    mOkPanel.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    
    private int getExplanationFromErrorCode(int errCode) {
        Log.d(TAG, "Installation error code: " + errCode);
        switch (errCode) {
            case PackageManager.INSTALL_FAILED_INVALID_APK:
                return R.string.install_failed_invalid_apk;
            case PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES:
                return R.string.install_failed_inconsistent_certificates;
            case PackageManager.INSTALL_FAILED_OLDER_SDK:
                return R.string.install_failed_older_sdk;
            case PackageManager.INSTALL_FAILED_CPU_ABI_INCOMPATIBLE:
                return R.string.install_failed_cpu_abi_incompatible;
            default:
                return -1;
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        mAppInfo = intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        mInstallFlowAnalytics = intent.getParcelableExtra(EXTRA_INSTALL_FLOW_ANALYTICS);
        mInstallFlowAnalytics.setContext(this);
        mPackageURI = intent.getData();

        final String scheme = mPackageURI.getScheme();
        if (scheme != null && !"file".equals(scheme) && !"package".equals(scheme)) {
            mInstallFlowAnalytics.setFlowFinished(
                    InstallFlowAnalytics.RESULT_FAILED_UNSUPPORTED_SCHEME);
            throw new IllegalArgumentException("unexpected scheme " + scheme);
        }
        //qj delete this showAppInstallLocationSettingDlg();
        initView();//qj add
    }
/*qj
    private void showAppInstallLocationSettingDlg() {
        final int selectedLocation = Settings.Global.getInt(getContentResolver(),
                Settings.Global.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        final String[] items =getResources().getStringArray(R.array.app_install_location_entries);
        new AlertDialog.Builder(this).setTitle(R.string.app_install_location_title)
                .setCancelable(false)
                .setSingleChoiceItems(items, selectedLocation,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if(APP_INSTALL_DEVICE == item) {
                                    Settings.Global.putInt(getContentResolver(),
                                            Settings.Global.DEFAULT_INSTALL_LOCATION,
                                                    APP_INSTALL_DEVICE);
                                } else if (APP_INSTALL_SDCARD == item) {
                                    Settings.Global.putInt(getContentResolver(),
                                            Settings.Global.DEFAULT_INSTALL_LOCATION,
                                                    APP_INSTALL_SDCARD);
                                } else if (APP_INSTALL_AUTO== item) {
                                    Settings.Global.putInt(getContentResolver(),
                                            Settings.Global.DEFAULT_INSTALL_LOCATION,
                                                    APP_INSTALL_AUTO);
                                } else {
                                // Should not happen, default to prompt.
                                    Settings.Global.putInt(getContentResolver(),
                                            Settings.Global.DEFAULT_INSTALL_LOCATION,
                                                    APP_INSTALL_AUTO);
                                }
                                dialog.cancel();
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        initView();
                                    }
                                });
                            }
                        }
                ).show();
    }*/

    @Override
    public Dialog onCreateDialog(int id, Bundle bundle) {
        switch (id) {
        case DLG_OUT_OF_SPACE:
            String dlgText = getString(R.string.out_of_space_dlg_text, mLabel);
            return new AlertDialog.Builder(this)
                    .setTitle(R.string.out_of_space_dlg_title)
                    .setMessage(dlgText)
                    .setPositiveButton(R.string.manage_applications, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //launch manage applications
                            Intent intent = new Intent("android.intent.action.MANAGE_PACKAGE_STORAGE");
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "Canceling installation");
                            finish();
                        }
                    })
                    .setOnCancelListener(this)
                    .create();
        }
       return null;
   }

    private void showDialogInner(int id) {
        removeDialog(id);
        showDialog(id);
    }

    class PackageInstallObserver extends IPackageInstallObserver.Stub {
        public void packageInstalled(String packageName, int returnCode) {
            Message msg = mHandler.obtainMessage(INSTALL_COMPLETE);
            msg.arg1 = returnCode;
            mHandler.sendMessage(msg);
        }
    }

    public void initView() {
        setContentView(R.layout.op_progress);
        int installFlags = 0;
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(mAppInfo.packageName, 
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if(pi != null) {
                installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
            }
        } catch (NameNotFoundException e) {
        }
        if((installFlags & PackageManager.INSTALL_REPLACE_EXISTING )!= 0) {
            Log.w(TAG, "Replacing package:" + mAppInfo.packageName);
        }

        final PackageUtil.AppSnippet as;
        if ("package".equals(mPackageURI.getScheme())) {
            as = new PackageUtil.AppSnippet(pm.getApplicationLabel(mAppInfo),
                    pm.getApplicationIcon(mAppInfo));
        } else {
            final File sourceFile = new File(mPackageURI.getPath());
            as = PackageUtil.getAppSnippet(this, mAppInfo, sourceFile);
        }
        mLabel = as.label;
        PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);
        mStatusTextView = (TextView)findViewById(R.id.center_text);
        mStatusTextView.setText(R.string.installing);
        mExplanationTextView = (TextView) findViewById(R.id.center_explanation);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);
        // Hide button till progress is being displayed
        mOkPanel = (View)findViewById(R.id.buttons_panel);
        mDoneButton = (Button)findViewById(R.id.done_button);
        mLaunchButton = (Button)findViewById(R.id.launch_button);
        mOkPanel.setVisibility(View.INVISIBLE);

        String installerPackageName = getIntent().getStringExtra(
                Intent.EXTRA_INSTALLER_PACKAGE_NAME);
        Uri originatingURI = getIntent().getParcelableExtra(Intent.EXTRA_ORIGINATING_URI);
        Uri referrer = getIntent().getParcelableExtra(Intent.EXTRA_REFERRER);
        int originatingUid = getIntent().getIntExtra(Intent.EXTRA_ORIGINATING_UID,
                VerificationParams.NO_UID);
        ManifestDigest manifestDigest = getIntent().getParcelableExtra(EXTRA_MANIFEST_DIGEST);
        VerificationParams verificationParams = new VerificationParams(null, originatingURI,
                referrer, originatingUid, manifestDigest);
        PackageInstallObserver observer = new PackageInstallObserver();

        if ("package".equals(mPackageURI.getScheme())) {
            try {
                pm.installExistingPackage(mAppInfo.packageName);
                observer.packageInstalled(mAppInfo.packageName,
                        PackageManager.INSTALL_SUCCEEDED);
            } catch (PackageManager.NameNotFoundException e) {
                observer.packageInstalled(mAppInfo.packageName,
                        PackageManager.INSTALL_FAILED_INVALID_APK);
            }
        } else {
            pm.installPackageWithVerificationAndEncryption(mPackageURI, observer, installFlags,
                    installerPackageName, verificationParams, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View v) {
        if(v == mDoneButton) {
            if (mAppInfo.packageName != null) {
                Log.i(TAG, "Finished installing "+mAppInfo.packageName);
            }
            finish();
        } else if(v == mLaunchButton) {
            mLaunchIntent = getPackageManager().getLaunchIntentForPackage(
                    mAppInfo.packageName);
            if (mLaunchIntent != null) {
                startActivity(mLaunchIntent);
            }
            finish();
        }
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
