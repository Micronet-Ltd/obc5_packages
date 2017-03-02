/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.mediatek.filemanager;


import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.mediatek.filemanager.utils.MimeTypeParser;
import com.mediatek.filemanager.utils.MimeTypes;
import com.mediatek.filemanager.AlertDialogFragment.AlertDialogFragmentBuilder;
import com.mediatek.filemanager.AlertDialogFragment.ChoiceDialogFragment;
import com.mediatek.filemanager.AlertDialogFragment.ChoiceDialogFragmentBuilder;
import com.mediatek.filemanager.AlertDialogFragment.EditDialogFragmentBuilder;
import com.mediatek.filemanager.AlertDialogFragment.EditTextDialogFragment;
import com.mediatek.filemanager.AlertDialogFragment.EditTextDialogFragment.EditTextDoneListener;
import com.mediatek.filemanager.AlertDialogFragment.ResultDialogFragmentBuilder;
import com.mediatek.filemanager.AlertDialogFragment.ResultDialogFragment;
import com.mediatek.filemanager.service.FileManagerService;
import com.mediatek.filemanager.service.FileManagerService.OperationEventListener;
import com.mediatek.filemanager.service.ProgressInfo;
import com.mediatek.filemanager.utils.DrmManager;
import com.mediatek.filemanager.utils.FileUtils;
import com.mediatek.filemanager.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;
import android.media.MediaFile;

/**
 * This is main activity in File manager.
 */
public class FileManagerOperationActivity extends AbsBaseActivity implements
        OnMenuItemClickListener, AdapterView.OnItemLongClickListener {
    public static final String RENAME_EXTENSION_DIALOG_TAG = "rename_extension_dialog_fragment_tag";
    public static final String RENAME_DIALOG_TAG = "rename_dialog_fragment_tag";
    public static final String DELETE_DIALOG_TAG = "delete_dialog_fragment_tag";
    public static final String INTENT_EXTRA_SELECT_PATH = "select_path";
	public static final String ENCRYPT_DIALOG_TAG = "encrypt_dialog_fragment_tag";

    private static final String TAG = "FileManagerOperationActivity";
    private static final String NEW_FILE_PATH_KEY = "new_file_path_key";
    private static final String SAVED_SELECTED_PATH_KEY = "saved_selected_path";
    private static final String CURRENT_VIEW_MODE_KEY = "view_mode_key";
    private static final String CURRENT_POSTION_KEY = "current_postion_key";
    private static final String CURRENT_TOP_KEY = "current_top_key";
    private static final String PREF_SORT_BY = "pref_sort_by";
    private static final String PREF_SHOW_HIDEN_FILE = "pref_show_hiden_file";

    private static final int BACKGROUND_COLOR = 0xff848284;

	/* add by wenjs for iom begin */

	private static final String ACTION_MEDIA_SCANNER_SCAN_ALL = "com.android.fileexplorer.action.MEDIA_SCANNER_SCAN_ALL";

	public static final int REQUEST_CODE_IRIS_REGIST = 10;	// 注册请求代码
	public static final int REQUEST_CODE_IRIS_ENCRYPT = 12; // 虹膜加密请求代码
	public static final int REQUEST_CODE_IRIS_DECRYPT = 13; // 虹膜解密请求代码
	public static final int RESULT_CODE_IRIS_REGIST = 20; // 虹膜比对结果：注册处理代码
	public static final int RESULT_CODE_IRIS_ENCRYPT = 22; // 虹膜加密结果代码
	public static final int RESULT_CODE_IRIS_DECRYPT = 23; // 虹膜加密结果代码
	public static final String regOk = "regOk";		//注册成功
	public static final String regFail = "regFail";	//注册失败
	public static final String registerTimeout = "registerTimeout"; // 注册超时
	public static final String requestCodeVar = "requestCode"; // 请求代码变量
	public static final String responseInfos = "responseInfos"; // 返回结果信息
	public static final String encryptDone = "encryptDone"; // 加密过程完成
	public static final String decryptDone = "decryptDone"; // 解密过程完成
	public static final String existSameNameFiles = "existSameNameFiles"; // 加密文件存储列表中已经存在该名称的文件
	public static final String errorFiles = "errorFiles"; // 加密出现异常文件
	public static final String encryptedFiles = "encryptedFiles"; // 已加密过的文件
	public static final String unencryptedFiles = "unencryptedFiles"; // 未加密过的文件
	public static final String noExsitFiles = "noExsitFiles"; // 不存在的文件
	public static final String opeFilesBgColor = "opeFilesBgColor"; // 加解密进度UI背景色
	public static final String requestEncryptAction = "cn.com.eyesmart.encrypt"; // 请求加解密操作action
	public static final String opeFilePaths = "opeFilePaths";
	public static final String opeFilePathsIsNull = "opeFilePathsIsNull"; // 传入的文件列表为空
	public static final String outOfLengthFiles = "outOfLengthFiles"; // 文件大小超过1G的文件列表, 1024*1204*1024*1
	public static final String quitOperatingFiles = "quitOperatingListFiles"; // 操作中途退出，尚未操作完成的文件列表
	public static final String noIrisTemplate = "noIrisTemplate"; // 没有虹膜模板，请先注册虹膜信息
	public static final String pwdErr = "pwdErr"; // 解密所采集的虹膜信息不是加密时的虹膜信息
	public static final String matchTimeout = "matchTimeout"; //匹配超时
	public static final String requestIrisAction = "cn.com.eyesmart.getiris"; // 请求虹膜
	
	/* add by wenjs for iom end */
	
    private View mNavigationView = null;
    private RelativeLayout mEditBar = null;
    private Button mTextSelect = null;
    private PopupMenu mEditPopupMenu = null;
    private PopupMenu mNavigationPopupMenu = null;
    private boolean mSelectedAll = true;
    private boolean mIsConfigChanged = false;
    private int mOrientationConfig;
    private MimeTypes mMimeTypes;

    @Override
    public void onUnmounted(String mountPoint) {
        if (mCurrentPath.startsWith(mountPoint)
                || mMountPointManager.isRootPath(mCurrentPath)) {

            if (mAdapter != null
                    && mAdapter.getMode() == FileInfoAdapter.MODE_EDIT) {
                switchToNavigationView();
            }

            ProgressDialogFragment pf = (ProgressDialogFragment) getFragmentManager()
                    .findFragmentByTag(HeavyOperationListener.HEAVY_DIALOG_TAG);
            if (pf != null) {
                pf.dismissAllowingStateLoss();
            }

            // Restore the detail_dialog
            AlertDialogFragment af = (AlertDialogFragment) getFragmentManager()
                    .findFragmentByTag(DetailInfoListener.DETAIL_DIALOG_TAG);

            if (af != null) {
                af.dismissAllowingStateLoss();
            }

            // restore delete dialog
            af = (AlertDialogFragment) getFragmentManager().findFragmentByTag(
                    DELETE_DIALOG_TAG);
            if (af != null) {
                af.dismissAllowingStateLoss();
            }
            af = (AlertDialogFragment) getFragmentManager().findFragmentByTag(
                    RENAME_EXTENSION_DIALOG_TAG);
            if (af != null) {
                af.dismissAllowingStateLoss();
            }

            ChoiceDialogFragment sortDialogFragment = (ChoiceDialogFragment) getFragmentManager()
                    .findFragmentByTag(ChoiceDialogFragment.TAG);
            if (sortDialogFragment != null) {
                sortDialogFragment.dismissAllowingStateLoss();
            }

            EditTextDialogFragment renameDialogFragment = (EditTextDialogFragment) 
                    getFragmentManager().findFragmentByTag(RENAME_DIALOG_TAG);
            if (renameDialogFragment != null) {
                renameDialogFragment.dismissAllowingStateLoss();
            }
        }

        super.onUnmounted(mountPoint);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtils.d(TAG, "onCreate()");
        // get sort by
        mSortType = getPrefsSortBy();
        mOrientationConfig = this.getResources().getConfiguration().orientation;
        loadMimeTypes();
    }

    private void loadMimeTypes() {
        MimeTypeParser mtp = new MimeTypeParser();
    
        XmlResourceParser in = getResources().getXml(R.xml.mimetypes);
    
        try {
            mMimeTypes = mtp.fromXmlResource(in);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "loadMimeTypes: XmlPullParserException", e);
        } catch (IOException e) {
            Log.e(TAG, "loadMimeTypes: IOException", e);
        }
    }
    
    @Override
    protected void serviceConnected() {
        super.serviceConnected();
        if (mSavedInstanceState != null) {
            int mode = mSavedInstanceState.getInt(CURRENT_VIEW_MODE_KEY,
                    FileInfoAdapter.MODE_NORMAL);
            int position = mSavedInstanceState.getInt(CURRENT_POSTION_KEY, 0);
            int top = mSavedInstanceState.getInt(CURRENT_TOP_KEY, -1);
            LogUtils.d(TAG, "serviceConnected mode=" + mode);
            restoreViewMode(mode, position, top);
        }
        mService
                .setListType(
                        getPrefsShowHidenFile() ? FileManagerService.FILE_FILTER_TYPE_ALL
                                : FileManagerService.FILE_FILTER_TYPE_DEFAULT,
                        this.getClass().getName());
        mListView.setOnItemLongClickListener(this);
    }

    private void restoreViewMode(int mode, int position, int top) {
        if (mode == FileInfoAdapter.MODE_EDIT) {
            mEditBar.setVisibility(View.VISIBLE);
            mNavigationView.setVisibility(View.INVISIBLE);
            mListView.setFastScrollEnabled(false);
            mAdapter.changeMode(mode);
            updateEditBarWidgetState();
        } else {
            mNavigationView.setVisibility(View.VISIBLE);
            mEditBar.setVisibility(View.INVISIBLE);
            mListView.setFastScrollEnabled(true);
            mAdapter.changeMode(FileInfoAdapter.MODE_NORMAL);
        }
        mListView.setSelectionFromTop(position, top);
        invalidateOptionsMenu();
    }

    protected void restoreDialog() {
        // Restore the heavy_dialog : pasting deleting
        ProgressDialogFragment pf = (ProgressDialogFragment) getFragmentManager()
                .findFragmentByTag(HeavyOperationListener.HEAVY_DIALOG_TAG);
        if (pf != null) {
            if (!mService.isBusy(this.getClass().getName())) {
                pf.dismissAllowingStateLoss();
            } else {
                HeavyOperationListener listener = new HeavyOperationListener(
                        AlertDialogFragment.INVIND_RES_ID);
                mService.reconnected(this.getClass().getName(), listener);
                pf.setCancelListener(listener);
            }
        }

        String saveSelectedPath = mSavedInstanceState
                .getString(SAVED_SELECTED_PATH_KEY);
        FileInfo saveSelectedFile = null;
        if (saveSelectedPath != null) {
            saveSelectedFile = new FileInfo(saveSelectedPath);
        }

        // Restore the detail_dialog
        AlertDialogFragment af = (AlertDialogFragment) getFragmentManager()
                .findFragmentByTag(DetailInfoListener.DETAIL_DIALOG_TAG);

        if (af != null && saveSelectedFile != null && mService != null) {
            DetailInfoListener listener = new DetailInfoListener(
                    saveSelectedFile);
            if (mService.isBusy(this.getClass().getName())) {
                mService.reconnected(this.getClass().getName(), listener);
                af.setDismissListener(listener);
            } else {
                af.dismissAllowingStateLoss();
                mService.getDetailInfo(this.getClass().getName(), saveSelectedFile,
                        listener);
            }

        }

        // restore delete dialog
        af = (AlertDialogFragment) getFragmentManager().findFragmentByTag(
                DELETE_DIALOG_TAG);
        if (af != null) {
            af.setOnDoneListener(new DeleteListener());
        }
        // rename Ext Dialog
        af = (AlertDialogFragment) getFragmentManager().findFragmentByTag(
                RENAME_EXTENSION_DIALOG_TAG);
        if (af != null) {
            String newFilePath = af.getArguments().getString(NEW_FILE_PATH_KEY);
            if (newFilePath != null && saveSelectedFile != null) {
                af.setOnDoneListener(new RenameExtensionListener(
                        saveSelectedFile, newFilePath));
            }
        }

        ChoiceDialogFragment sortDialogFragment = (ChoiceDialogFragment) getFragmentManager()
                .findFragmentByTag(ChoiceDialogFragment.TAG);
        if (sortDialogFragment != null) {
            sortDialogFragment.setItemClickListener(new SortClickListner());
        }

        EditTextDialogFragment renameDialogFragment = (EditTextDialogFragment) getFragmentManager()
                .findFragmentByTag(RENAME_DIALOG_TAG);
        if (renameDialogFragment != null && saveSelectedFile != null) {
            renameDialogFragment
                    .setOnEditTextDoneListener(new RenameDoneListener(
                            saveSelectedFile));
        }
        super.restoreDialog();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mAdapter != null && mAdapter.getCheckedItemsCount() == 1) {
            FileInfo selectFileInfo = mAdapter.getCheckedFileInfoItemsList()
                    .get(0);
            if (selectFileInfo != null) {
                outState.putString(SAVED_SELECTED_PATH_KEY, selectFileInfo
                        .getFileAbsolutePath());
            }
        }
        int currentMode = (mAdapter != null) ? mAdapter.getMode()
                : FileInfoAdapter.MODE_NORMAL;
        outState.putInt(CURRENT_VIEW_MODE_KEY, currentMode);
        if (mListView.getChildCount() > 0) {
            View view = mListView.getChildAt(0);
            int position = (mListView.getPositionForView(view));
            int top = view.getTop();
            outState.putInt(CURRENT_POSTION_KEY, position);
            outState.putInt(CURRENT_TOP_KEY, top);
        }
    }

    @Override
    protected void setMainContentView() {
        setContentView(R.layout.main);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            LayoutInflater inflater = (LayoutInflater) 
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customActionBarView = inflater.inflate(R.layout.actionbar,
                    null);

            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                            | ActionBar.DISPLAY_SHOW_TITLE);

            mNavigationView = customActionBarView
                    .findViewById(R.id.bar_background);

            mEditBar = (RelativeLayout) customActionBarView
                    .findViewById(R.id.edit_bar);
            mEditBar.setBackgroundColor(BACKGROUND_COLOR);
            mEditBar.setVisibility(View.INVISIBLE);

            mTextSelect = (Button) customActionBarView
                    .findViewById(R.id.text_select);
            actionBar.setCustomView(customActionBarView);
            actionBar.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.title_bar_bg));
            actionBar.setSplitBackgroundDrawable(getResources().getDrawable(
                    R.drawable.actionbar_background));
        }
    }

    /**
     * This method switches edit view to navigation view
     */
    private void switchToNavigationView() {
        LogUtils.d(TAG, "Switch to navigation view");
        mNavigationView.setVisibility(View.VISIBLE);
        mEditBar.setVisibility(View.INVISIBLE);
        mListView.setFastScrollEnabled(true);

        if (mEditPopupMenu != null) {
            mEditPopupMenu.dismiss();
        }

        mAdapter.changeMode(FileInfoAdapter.MODE_NORMAL);
        invalidateOptionsMenu();
    }

    private void switchToEditView(int position, int top) {
        LogUtils.d(TAG, "switchToEditView position and top" + position + "/" + top);
        mAdapter.setChecked(position, true);
        mListView.setSelectionFromTop(position, top);
        switchToEditView();
    }

    private void switchToEditView() {
        LogUtils.d(TAG, "Switch to edit view");
        mEditBar.setVisibility(View.VISIBLE);
        mNavigationView.setVisibility(View.INVISIBLE);
        mListView.setFastScrollEnabled(false);
        mAdapter.changeMode(FileInfoAdapter.MODE_EDIT);
        updateEditBarWidgetState();
        invalidateOptionsMenu();
    }

    /**
     * The method shares the files/folders MMS: support only single files BT: support single and
     * multiple files
     */
    private void share() {
        Intent intent;
        boolean forbidden = false;
        List<FileInfo> files = null;
        ArrayList<Parcelable> sendList = new ArrayList<Parcelable>();

        if (mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {
            files = mAdapter.getCheckedFileInfoItemsList();
        } else {
            LogUtils.w(TAG, "Maybe dispatch events twice, view mode error.");
            return;
        }

        if (files.size() > 1) {
            // send multiple files
            LogUtils.d(TAG, "Share multiple files");
            for (FileInfo info : files) {
                if (info.isDrmFile()
                        && DrmManager.getInstance().isRightsStatus(
                                info.getFileAbsolutePath())) {
                    forbidden = true;
                    break;
                }

                sendList.add(info.getUri());
            }

            if (!forbidden) {
                intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType(FileUtils.getMultipleMimeType(mService,
                        mCurrentPath, files));
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                        sendList);

                try {
                    startActivity(Intent.createChooser(intent,
                            getString(R.string.send_file)));
                } catch (android.content.ActivityNotFoundException e) {
                    LogUtils.e(TAG, "Cannot find any activity", e);
                    // TODO add a toast to notify user; get a function from here and if(!forbidden)
                    // below
                }
            }
        } else {
            // send single file
            LogUtils.d(TAG, "Share a single file");
            FileInfo fileInfo = files.get(0);
            //String mimeType = fileInfo.getFileMimeType(mService);
            String mimeType = mMimeTypes.getMimeType(fileInfo.getFile().getName());
            if (fileInfo.isDrmFile()
                    && DrmManager.getInstance().isRightsStatus(
                            fileInfo.getFileAbsolutePath())) {
                forbidden = true;
            }

            if (mimeType == null || mimeType.startsWith("unknown")) {
                mimeType = FileInfo.MIMETYPE_UNRECOGNIZED;
            }

            if (!forbidden) {
                intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType(mimeType);
                Uri uri = Uri.fromFile(fileInfo.getFile());
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                LogUtils.d(TAG, "Share Uri file: " + uri);
                LogUtils.d(TAG, "Share file mimetype: " + mimeType);

                try {
                    startActivity(Intent.createChooser(intent,
                            getString(R.string.send_file)));
                } catch (android.content.ActivityNotFoundException e) {
                    LogUtils.e(TAG, "Cannot find any activity", e);
                    // TODO add a toast to notify user
                }
            }
        }

        if (forbidden) {
            showForbiddenDialog();
        } else {
            switchToNavigationView();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        if (mService != null && mService.isBusy(this.getClass().getName())) {
            return;
        }
        if (mAdapter.isMode(FileInfoAdapter.MODE_NORMAL)) {
            LogUtils.d(TAG, "Selected position: " + position);

            if (position >= mAdapter.getCount() || position < 0) {
                LogUtils.e(TAG, "click events error");
                LogUtils.e(TAG, "mFileInfoList.size(): " + mAdapter.getCount());
                return;
            }
            FileInfo selecteItemFileInfo = (FileInfo) mAdapter
                    .getItem(position);

            if (selecteItemFileInfo.isDirectory()) {
                int top = view.getTop();
                LogUtils.v(TAG, "fromTop = " + top);
                addToNavigationList(mCurrentPath, selecteItemFileInfo, top);
                showDirectoryContent(selecteItemFileInfo.getFileAbsolutePath());
            } else {
                // open file here
                boolean canOpen = true;
                //String mimeType = selecteItemFileInfo.getFileMimeType(mService);
                String mimeType = mMimeTypes.getMimeType(selecteItemFileInfo.getFile().getName());
                if (selecteItemFileInfo.isDrmFile()) {
                    mimeType = DrmManager.getInstance().getOriginalMimeType(
                            selecteItemFileInfo.getFileAbsolutePath());

                    if (TextUtils.isEmpty(mimeType)) {
                        canOpen = false;
                        mToastHelper.showToast(R.string.msg_unable_open_file);
                    }
                }

				if(selecteItemFileInfo.isEncryptFile()){
					canOpen = false;

					openEncryptedFile(selecteItemFileInfo);
				}

				if (canOpen) {
					final Intent intent = new Intent(Intent.ACTION_VIEW);
					final Uri uri = selecteItemFileInfo.getUri();
					final String canOpenMimeType = mimeType;
					LogUtils.d(TAG, "Open uri file: " + uri);
					String [] openFileType = getResources().getStringArray(R.array.open_file);
					for (int i = 0; i < openFileType.length; i++) {
						if(openFileType[i].equals(canOpenMimeType)){
				        	new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).
			        		setTitle(R.string.dialog_import_title).setMessage(R.string.dialog_import_msg).
			        		setPositiveButton(android.R.string.ok, new OnClickListener() {
			                            public void onClick(DialogInterface dialog, int which) {  
			                                intent.setDataAndType(uri, canOpenMimeType);
			                                try {
			                                    startActivity(intent);
			                                } catch (ActivityNotFoundException e) {
			                                	mToastHelper.showToast(R.string.msg_unable_open_file);
			                                    Log.e(TAG, "can't be open");
			                                }
			                            }
			                }).setNegativeButton(android.R.string.cancel, null).create().show();
			        	return;
						}
					}
					if (!canOpenMimeType.equals("image/gif") && canOpenMimeType.startsWith("image/") && !canOpenMimeType.equals("image/tiff")) {
						intent.setDataAndType(uri, "image/*");
					} else {
						intent.setDataAndType(uri, canOpenMimeType);
					}
					try {
						startActivity(intent);
					} catch (Exception e) {
						mToastHelper.showToast(R.string.msg_unable_open_file);
						LogUtils.w(TAG, "Cannot open file: "
								+ selecteItemFileInfo.getFileAbsolutePath());
					}
				}
            }
        } else {
            // edit view
            CheckBox checkBox = (CheckBox) view
                    .findViewById(R.id.edit_checkbox);

            boolean state = checkBox.isChecked();
            checkBox.setChecked(!state);
            mAdapter.setChecked(position, !state);
            updateEditBarWidgetState();
            invalidateOptionsMenu();
            mAdapter.notifyDataSetChanged();
        }
    }
    
    private void updateEditBarWidgetState() {
        int selectedCount = mAdapter.getCheckedItemsCount();
        String selected = getResources().getString(R.string.selected);
        selected = "" + selectedCount + " " + selected;
        mTextSelect.setText(selected);
    }

    @Override
    public void onClick(View view) {
        if (mService.isBusy(this.getClass().getName())) {
            return;
        }
        int id = view.getId();
        LogUtils.d(TAG, "onClick: " + id);

        boolean isMounted = mMountPointManager.isRootPathMount(mCurrentPath);
        if (mAdapter.isMode(FileInfoAdapter.MODE_EDIT) && isMounted) {
            updateEditBarWidgetState();
            invalidateOptionsMenu();
            return;
        }
        super.onClick(view);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        LogUtils.d(TAG, "onPrepareOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        menu.clear();
        if (mService == null) {
            return true;
        }

        if (mAdapter.isMode(FileInfoAdapter.MODE_NORMAL)) {
            return onPrepareNormalOptionsMenu(menu, inflater);
        } else {
            // edit view
            return onPrepareEditOptionsMenu(menu, inflater);
        }
    }

    private boolean onPrepareNormalOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.navigation_view_menu, menu);
        if (mCurrentPath != null
                && mMountPointManager.isRootPath(mCurrentPath)) {
            menu.findItem(R.id.create_folder).setEnabled(false);
            menu.findItem(R.id.paste).setVisible(false);
            menu.findItem(R.id.paste).setEnabled(false);
            menu.findItem(R.id.search).setEnabled(true);
            menu.findItem(R.id.popup_menu).setEnabled(true);
            return true;
        }
        if (mFileInfoManager.getPasteCount() > 0) {
            menu.findItem(R.id.paste).setVisible(true);
            menu.findItem(R.id.paste).setEnabled(true);
        } else {
            menu.findItem(R.id.paste).setVisible(false);
            menu.findItem(R.id.paste).setEnabled(false);
        }
        if (mCurrentPath != null && !(new File(mCurrentPath)).canWrite()) {
            menu.findItem(R.id.create_folder).setEnabled(false);
            menu.findItem(R.id.paste).setVisible(false);
        } else {
            menu.findItem(R.id.create_folder).setEnabled(true);
        }

        if (mAdapter.getCount() == 0) {
            menu.findItem(R.id.search).setEnabled(false);
        } else {
            menu.findItem(R.id.search).setEnabled(true);
        }
        return true;
    }

    private boolean onPrepareEditOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_view_menu, menu);
        int selectedCount = mAdapter.getCheckedItemsCount();

        // enable(disable) copy, cut, and delete icon
        if (selectedCount == 0) {
            menu.findItem(R.id.copy).setEnabled(false);
            menu.findItem(R.id.delete).setEnabled(false);
            menu.findItem(R.id.cut).setEnabled(false);
            menu.findItem(R.id.popup_menu).setEnabled(false);
        } else {
            menu.findItem(R.id.copy).setEnabled(true);
            menu.findItem(R.id.delete).setEnabled(true);
            menu.findItem(R.id.cut).setEnabled(true);
        }

        if (mAdapter.getCount() == 0) {
            menu.findItem(R.id.select).setEnabled(false);
        } else {
            menu.findItem(R.id.select).setEnabled(true);
            if (mAdapter.getCount() != selectedCount) {
                menu.findItem(R.id.select)
                        .setIcon(R.drawable.fm_select_all).setTitle(
                                R.string.select_all);
                mSelectedAll = true;
            } else {
                menu.findItem(R.id.select).setIcon(
                        R.drawable.fm_clear_select).setTitle(
                        R.string.deselect_all);
                mSelectedAll = false;
            }
        }

		/*if(isIrisIntentRegistered(getApplicationContext())){
			LogUtils.d(TAG, "find iris intent");
			menu.findItem(R.id.encrypt).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			if (selectedCount == 0) {
				menu.findItem(R.id.encrypt).setEnabled(false);
			} else {
				if(mAdapter.checkEncryptFile() != null){				
					menu.findItem(R.id.encrypt).setIcon(R.drawable.decrypt);				
				} else {
					menu.findItem(R.id.encrypt).setEnabled(true);
				}
			}
		}*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtils.d(TAG, "onOptionsItemSelected: " + item.getItemId());

        if (mService != null && mService.isBusy(this.getClass().getName())) {
            return true;
        }
        if (mAdapter.isMode(FileInfoAdapter.MODE_NORMAL)) {
            return onNormalItemSelected(item);
        } else {
            // edit view
            return onEditItemSelected(item);
        }
    }

	/**
     * Returns true if there is a IRIS on the device
	 * add by wenjs for detect IRIS intent
     */
    public static boolean isIrisIntentRegistered(Context context) {
        final Intent intent = new Intent(requestEncryptAction);
		final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> receiverList = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return receiverList.size() > 0;        
    }

    private boolean onNormalItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.create_folder:
            showCreateFolderDialog();
            break;
        case R.id.search:
             /*modified by zhangjiaquan for SW00019775 can not search sd card in root path 2013-11-20 begin*/
	     String mCurrentPathForSearch = mCurrentPath;
            Intent intent = new Intent();
            intent.setClass(this, FileManagerSearchActivity.class);
	      if(mMountPointManager.isRootPath(mCurrentPath))
             {
                  mCurrentPathForSearch = "/storage/";//it is a bad  , so you should modify for other platform
             }
            intent.putExtra(FileManagerSearchActivity.CURRENT_PATH,
                    mCurrentPathForSearch);
             /*modified by zhangjiaquan for SW00019775 can not search sd card in root path 2013-11-20 end*/
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("isShowHidenFile", getPrefsShowHidenFile());//QRT  add by yanlei 20131216  do not search the hidden file when set don't show hidden file
            startActivity(intent);

            break;
        case R.id.paste:
            if (mService != null) {
                mService.pasteFiles(this.getClass().getName(), mFileInfoManager
                        .getPasteList(), mCurrentPath, mFileInfoManager
                        .getPasteType(), new HeavyOperationListener(
                        R.string.paste));
            }
            break;
        case R.id.popup_menu:
            showNavigationPopupMenu();
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private boolean onEditItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.popup_menu:
            showEditPopupMenu();
            break;
        case R.id.copy:
            mFileInfoManager.savePasteList(FileInfoManager.PASTE_MODE_COPY,
                    mAdapter.getCheckedFileInfoItemsList());
            switchToNavigationView();
            break;
        case R.id.cut:
            mFileInfoManager.savePasteList(FileInfoManager.PASTE_MODE_CUT,
                    mAdapter.getCheckedFileInfoItemsList());
            switchToNavigationView();
            break;

		/*case R.id.encrypt:			
			showEncryptDialog();
			break;*/
			
        case R.id.delete:
            showDeleteDialog();
            break;
        case R.id.select:
            if (mSelectedAll) {
                mAdapter.setAllItemChecked(true);
            } else {
                mAdapter.setAllItemChecked(false);
            }
            updateEditBarWidgetState();
            invalidateOptionsMenu();
            break;

        default:
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showEditPopupMenu() {
        View popupMenuBaseLine;
        if (mOrientationConfig == Configuration.ORIENTATION_LANDSCAPE) {
            popupMenuBaseLine = (View) findViewById(R.id.popup_menu_base_line_landscape);
        } else {
            popupMenuBaseLine = (View) findViewById(R.id.popup_menu_base_line_portrait);
        }
        mEditPopupMenu = createEditPopupMenu(popupMenuBaseLine);
        mEditPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {

            @Override
            public void onDismiss(PopupMenu arg0) {
                if (mIsConfigChanged) {
                    mIsConfigChanged = false;
                    showEditPopupMenu();
                }
            }
        });
        if (mEditPopupMenu != null) {
            updateEditPopupMenu();
            mIsConfigChanged = false;
            mEditPopupMenu.show();
        }
    }

    private void showNavigationPopupMenu() {
        View popupMenuBaseLine;
        if (mOrientationConfig == Configuration.ORIENTATION_LANDSCAPE) {
            popupMenuBaseLine = (View) findViewById(R.id.popup_menu_base_line_landscape);
        } else {
            popupMenuBaseLine = (View) findViewById(R.id.popup_menu_base_line_portrait);
        }
        mNavigationPopupMenu = createNaviPopupMenu(popupMenuBaseLine);
        mNavigationPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {

            @Override
            public void onDismiss(PopupMenu arg0) {
                if (mIsConfigChanged) {
                    mIsConfigChanged = false;
                    showNavigationPopupMenu();
                }
            }
        });
        if (mNavigationPopupMenu != null) {
            updateNaviPopupMenu();
            mIsConfigChanged = false;
            mNavigationPopupMenu.show();
        }
    }

    /**
     * This method switches edit view to navigation view
     * 
     * @param refresh whether to refresh the screen after the switch is done
     */
    private void sortFileInfoList() {
        LogUtils.d(TAG, "Start sortFileInfoList()");

        int selection = mListView.getFirstVisiblePosition(); // save current
        // visible position

        // refresh only when paste or delete operation is performed
        mFileInfoManager.sort(mSortType);
        mAdapter.notifyDataSetChanged();
        mListView.setSelection(selection);
        // restore the selection in the navigation view

        LogUtils.d(TAG, "End sortFileInfoList()");
    }

    /**
     * This method sets the sorting type in the preference
     * 
     * @param sort the sorting type
     */
    private void setPrefsSortBy(int sort) {
        mSortType = sort;
        Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putInt(PREF_SORT_BY, sort);
        editor.commit();
    }

    private boolean changePrefsShowHidenFile() {
        boolean hide = getPrefsShowHidenFile();
        Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putBoolean(PREF_SHOW_HIDEN_FILE, !hide);
        editor.commit();
        return hide;
    }

    /**
     * This method gets the sorting type from the preference
     * 
     * @return the sorting type
     */
    private int getPrefsSortBy() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getInt(PREF_SORT_BY, 0);
    }

    private boolean getPrefsShowHidenFile() {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        return prefs.getBoolean(PREF_SHOW_HIDEN_FILE, false);
    }

    protected void showForbiddenDialog() {
        AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
        AlertDialogFragment forbiddenDialogFragment = builder
                .setTitle(R.string.ok
                        /**com.mediatek.internal.R.string.drm_forwardforbidden_title*/)
                .setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setMessage(
                        /**com.mediatek.internal.R.string.drm_forwardforbidden_message*/R.string.ok)
                .setCancelable(false).setCancelTitle(R.string.ok).create();
        forbiddenDialogFragment.show(getFragmentManager(),
                AlertDialogFragment.TAG);
    }

	DecryptListener decryptListener = null;

	protected void openEncryptedFile(FileInfo openFile){
		int alertMsgId = R.string.msg_open_encrypted_file;
		int titleStr = R.string.alert;
		AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
		
		List<FileInfo> fileInfo = new ArrayList<FileInfo>();
		fileInfo.add(openFile);

		AlertDialogFragment decryptDialogFragment = builder.setMessage(
            alertMsgId).setDoneTitle(R.string.ok).setCancelTitle(
            R.string.cancel).setIcon(R.drawable.ic_dialog_alert_holo_light).setTitle(titleStr).create();
		decryptListener = new DecryptListener(fileInfo);
    	decryptDialogFragment.setOnDoneListener(decryptListener);
		decryptDialogFragment.show(getFragmentManager(), ENCRYPT_DIALOG_TAG);
	}

	protected void showEncryptDialog() {
		List<FileInfo> fileInfo = mAdapter.getCheckedFileInfoItemsList();
        int alertMsgId = R.string.alert_encrypt_file;
		AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
		int titleStr = R.string.encrypt;

        if (mAdapter.checkEncryptFile() == null) {
            alertMsgId = R.string.alert_encrypt_file;
			titleStr = R.string.encrypt;
			
			AlertDialogFragment encryptDialogFragment = builder.setMessage(
                alertMsgId).setDoneTitle(R.string.ok).setCancelTitle(
                R.string.cancel).setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setTitle(titleStr).create();
        	encryptDialogFragment.setOnDoneListener(new EncryptListener(fileInfo));
			encryptDialogFragment.show(getFragmentManager(), ENCRYPT_DIALOG_TAG);
        } else {
            alertMsgId = R.string.alert_decrypt_file;
			titleStr = R.string.decrypt;

			AlertDialogFragment decryptDialogFragment = builder.setMessage(
                alertMsgId).setDoneTitle(R.string.ok).setCancelTitle(
                R.string.cancel).setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setTitle(titleStr).create();
			decryptListener = new DecryptListener(fileInfo);
        	decryptDialogFragment.setOnDoneListener(decryptListener);
			decryptDialogFragment.show(getFragmentManager(), ENCRYPT_DIALOG_TAG);
        }        
    }

	private class EncryptListener implements OnClickListener {		
		List<FileInfo> mSrcfileInfoList;
		
		public EncryptListener(List<FileInfo> srcFileList) {
			mSrcfileInfoList = srcFileList;
		}
		
        @Override
        public void onClick(DialogInterface dialog, int id) {            
			ArrayList<String> paths = new ArrayList<String>();

			for(FileInfo info: mSrcfileInfoList){
				paths.add(info.getFileAbsolutePath());
			}
			
			callIrisEncrypt(paths);	
			switchToNavigationView();            
        }
    }

	private class DecryptListener implements OnClickListener {		
		List<FileInfo> mSrcfileInfoList;
		
		public DecryptListener(List<FileInfo> srcFileList) {
			mSrcfileInfoList = srcFileList;
		}
		
        @Override
        public void onClick(DialogInterface dialog, int id) {            
			ArrayList<String> paths = new ArrayList<String>();

			for(FileInfo info: mSrcfileInfoList){
				paths.add(info.getFileAbsolutePath());
			}
			
			callIrisDecrypt(paths);
			switchToNavigationView(); 
        }
    }

    protected void showDeleteDialog() {
        int alertMsgId = R.string.alert_delete_multiple;
        if (mAdapter.getCheckedItemsCount() == 1) {
            alertMsgId = R.string.alert_delete_single;
        } else {
            alertMsgId = R.string.alert_delete_multiple;
        }

        AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
        AlertDialogFragment deleteDialogFragment = builder.setMessage(
                alertMsgId).setDoneTitle(R.string.ok).setCancelTitle(
                R.string.cancel).setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setTitle(R.string.delete).create();
        deleteDialogFragment.setOnDoneListener(new DeleteListener());
        deleteDialogFragment.show(getFragmentManager(), DELETE_DIALOG_TAG);
    }

    private class DeleteListener implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            LogUtils
                    .d(TAG, "onClick() method for alertDeleteDialog, OK button");
            if (mService != null) {
                mService.deleteFiles(FileManagerOperationActivity.this
                        .getClass().getName(), mAdapter
                        .getCheckedFileInfoItemsList(),
                        new HeavyOperationListener(R.string.deleting));
            }
            switchToNavigationView();
        }
    }

    /**
     * The method creates an alert delete dialog
     * 
     * @param args argument, the boolean value who will indicates whether the selected files just
     *            only one. The prompt message will be different.
     * @return a dialog
     */
    protected void showRenameExtensionDialog(FileInfo srcfileInfo,
            final String newFilePath) {
        AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
        AlertDialogFragment renameExtensionDialogFragment = builder.setTitle(
                R.string.confirm_rename).setIcon(
                R.drawable.ic_dialog_alert_holo_light).setMessage(
                R.string.msg_rename_ext).setCancelTitle(R.string.cancel)
                .setDoneTitle(R.string.ok).create();
        renameExtensionDialogFragment.getArguments().putString(
                NEW_FILE_PATH_KEY, newFilePath);
        renameExtensionDialogFragment
                .setOnDoneListener(new RenameExtensionListener(srcfileInfo,
                        newFilePath));
        renameExtensionDialogFragment.show(getFragmentManager(),
                RENAME_EXTENSION_DIALOG_TAG);
    }

    private class RenameExtensionListener implements OnClickListener {
        private final String mNewFilePath;
        private final FileInfo mSrcFile;

        public RenameExtensionListener(FileInfo fileInfo, String newFilePath) {
            mNewFilePath = newFilePath;
            mSrcFile = fileInfo;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mService != null) {
                switchToNavigationView();
                mService.rename(FileManagerOperationActivity.this.getClass()
                        .getName(), mSrcFile, new FileInfo(mNewFilePath),
                        new LightOperationListener(FileUtils
                                .getFileName(mNewFilePath)));
            }
        }

    }

    /**
     * The method creates an alert sort dialog
     * 
     * @return a dialog
     */
    protected void showSortDialog() {
        ChoiceDialogFragmentBuilder builder = new ChoiceDialogFragmentBuilder();
        builder.setDefault(R.array.sort_by, mSortType).setTitle(
                R.string.sort_by).setCancelTitle(R.string.cancel);
        ChoiceDialogFragment sortDialogFragment = builder.create();
        sortDialogFragment.setItemClickListener(new SortClickListner());
        sortDialogFragment.show(getFragmentManager(), ChoiceDialogFragment.TAG);
    }

    private class SortClickListner implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int id) {
            if (id != mSortType) {
                setPrefsSortBy(id);
                dialog.dismiss();
                sortFileInfoList();
            }
        }

    }

    protected void showRenameDialog() {
        FileInfo fileInfo = mAdapter.getFirstCheckedFileInfoItem();
        int selection = 0;
        if (fileInfo != null) {
            String name = fileInfo.getFileName();
            String fileExtension = FileUtils.getFileExtension(name);
            selection = name.length();
            if (!fileInfo.isDirectory() && fileExtension != null) {
                selection = selection - fileExtension.length() - 1;
            }
            EditDialogFragmentBuilder builder = new EditDialogFragmentBuilder();
            builder.setDefault(name, selection).setDoneTitle(R.string.done)
                    .setCancelTitle(R.string.cancel).setTitle(R.string.rename);
            EditTextDialogFragment renameDialogFragment = builder.create();
            renameDialogFragment
                    .setOnEditTextDoneListener(new RenameDoneListener(fileInfo));
            renameDialogFragment.show(getFragmentManager(), RENAME_DIALOG_TAG);
        }
    }

    protected class RenameDoneListener implements EditTextDoneListener {
        FileInfo mSrcfileInfo;

        public RenameDoneListener(FileInfo srcFile) {
            mSrcfileInfo = srcFile;
        }

        @Override
        public void onClick(String text) {
            String newFilePath = mCurrentPath + MountPointManager.SEPARATOR
                    + text;
            if (null == mSrcfileInfo) {
                LogUtils.w(TAG, "mSrcfileInfo is null.");
                return;
            }
            if (FileUtils.isExtensionChange(newFilePath, mSrcfileInfo
                    .getFileAbsolutePath())) {
                showRenameExtensionDialog(mSrcfileInfo, newFilePath);
            } else {
                if (mService != null) {
                    switchToNavigationView();
                    mService.rename(FileManagerOperationActivity.this
                            .getClass().getName(), mSrcfileInfo, new FileInfo(
                            newFilePath), new LightOperationListener(FileUtils
                            .getFileName(newFilePath)));
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (mAdapter != null && mAdapter.isMode(FileInfoAdapter.MODE_EDIT)) {
            switchToNavigationView();
            return;
        }
        super.onBackPressed();
    }

    private PopupMenu createEditPopupMenu(View anchorView) {
        final PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.setOnMenuItemClickListener(this);
        return popupMenu;
    }

    private PopupMenu createNaviPopupMenu(View anchorView) {
        final PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.inflate(R.menu.navigation_popup_menu);
        popupMenu.setOnMenuItemClickListener(this);
        return popupMenu;
    }

    private void updateNaviPopupMenu() {
        final Menu menu = mNavigationPopupMenu.getMenu();
        if (getPrefsShowHidenFile()) {
            menu.findItem(R.id.hide).setTitle(R.string.hide_file);
        } else {
            menu.findItem(R.id.hide).setTitle(R.string.show_file);
        }
        if ((mAdapter.getCount() == 0)
                || (mCurrentPath != null && mMountPointManager
                        .isRootPath(mCurrentPath))) {
            menu.findItem(R.id.change_mode).setEnabled(false);
        } else {
            menu.findItem(R.id.change_mode).setEnabled(true);
        }
    }

    private boolean canSetAs(File file) {
    	// CAUTION: it is not a public API, tell me if you know some one to achieve it
        String mediaType = MediaFile.getMimeTypeForFile(file.getName());
        int type = MediaFile.getFileTypeForMimeType(mediaType);
        //modefy by qrt laiwugang to support wma (X820) 2013-03-27
    	/*if (type == MediaFile.FILE_TYPE_WMA) {
            return false;
        }*/

        return MediaFile.isAudioFileType(type) || MediaFile.isImageFileType(type);
    }
    
    private void doSetAs(File file) {
        Log.d(TAG, "doSetAs: " + file);
        
        Uri data = FileUtils.getUriFromFile(file);
        String type = mMimeTypes.getMimeType(file.getName());
        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
        intent.setDataAndType(data, type);
        intent.putExtra("mimeType", type);
        
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.menu_set_as)));
        } catch (ActivityNotFoundException e) {
            //showResult(false, R.string.application_not_available);
            Log.e(TAG, "can't be set as");
        }
    }
    
    private void updateEditPopupMenu() {
        final Menu menu = mEditPopupMenu.getMenu();
        int selectedCount = mAdapter.getCheckedItemsCount();

        // remove (disable) protection info icon
        menu.removeItem(R.id.protection_info);

        if (selectedCount == 0) {
            // disable share icon
            menu.findItem(R.id.share).setEnabled(false);
        } else if (selectedCount == 1) {
            // enable details icon
            menu.findItem(R.id.details).setEnabled(true);
            // enable rename icon
            if (mAdapter.getCheckedFileInfoItemsList().get(0).getFile()
                    .canWrite()) {
                menu.findItem(R.id.rename).setEnabled(true);
            }
            // enable protection info icon
            FileInfo fileInfo = mAdapter.getCheckedFileInfoItemsList().get(0);
            if (fileInfo.isDrmFile()) {
                String path = fileInfo.getFileAbsolutePath();
                if (DrmManager.getInstance().checkDrmObjectType(path)) {
                    String mimeType = DrmManager.getInstance()
                            .getOriginalMimeType(path);
                    if (mimeType != null && mimeType.trim().length() != 0) {
                        menu
                                .add(
                                        0,
                                        R.id.protection_info,
                                        0,
                                        /**com.mediatek.internal.R.string.drm_protectioninfo_title*/R.id.protection_info);
                    }
                }
            }
            // enable share icon
            if (fileInfo.isDrmFile()
                    && DrmManager.getInstance().isRightsStatus(
                            fileInfo.getFileAbsolutePath())
                    || fileInfo.isDirectory()) {
                menu.findItem(R.id.share).setEnabled(false);
            } else {
                menu.findItem(R.id.share).setEnabled(true);
            }
            // enable setting icon
            if(canSetAs(fileInfo.getFile())){
            	menu.findItem(R.id.setting).setEnabled(true);
            }else{
            	menu.findItem(R.id.setting).setEnabled(false);
            }
            
        } else {
            // disable details icon
            menu.findItem(R.id.details).setEnabled(false);
            // disable rename icon
            menu.findItem(R.id.rename).setEnabled(false);
            // disable share icon
            menu.findItem(R.id.share).setEnabled(true);
            // disable setting icon
            menu.findItem(R.id.setting).setEnabled(false);
            List<FileInfo> files = mAdapter.getCheckedFileInfoItemsList();
            for (FileInfo info : files) {
                File file = info.getFile();
                if (file.isDirectory()) {
                    // break for loop; disable share icon
                    menu.findItem(R.id.share).setEnabled(false);
                    break;
                }
            }
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.rename:
            showRenameDialog();
            break;
        case R.id.details:
            mService.getDetailInfo(this.getClass().getName(), mAdapter
                    .getCheckedFileInfoItemsList().get(0),
                    new DetailInfoListener(mAdapter
                            .getCheckedFileInfoItemsList().get(0)));
            break;
        case R.id.protection_info:
            // calling framework to show a protection info dialog
            String path = mCurrentPath
                    + MountPointManager.SEPARATOR
                    + mAdapter.getCheckedFileInfoItemsList().get(0)
                            .getFileName();
            DrmManager.getInstance().showProtectionInfoDialog(this, path);
            switchToNavigationView();
            break;
        case R.id.share:
            share();
            break;
        case R.id.sort:
            showSortDialog();
            break;
        case R.id.hide:
            if (mService != null) {
                mService.setListType(changePrefsShowHidenFile() 
                         ? FileManagerService.FILE_FILTER_TYPE_DEFAULT 
                                 : FileManagerService.FILE_FILTER_TYPE_ALL,
                                this.getClass().getName());
                mService.listFiles(this.getClass().getName(), mCurrentPath,
                        new ListListener());
            }
            break;
        case R.id.change_mode:
            switchToEditView();
            break;
        case R.id.setting:
        	FileInfo fileInfo = mAdapter.getCheckedFileInfoItemsList().get(0);
        	doSetAs(fileInfo.getFile());
            break;
        default:
            return false;
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String path = intent.getStringExtra(INTENT_EXTRA_SELECT_PATH);
        if (path != null && mService != null
                && !mService.isBusy(this.getClass().getName())) {
            File file = new File(path);
            if (!file.exists()) {
                mToastHelper
                        .showToast(getString(R.string.path_not_exists, path));
                path = mMountPointManager.getRootPath();
            }
            addToNavigationList(mCurrentPath, null, -1);
            showDirectoryContent(path);
        }
    }

    @Override
    protected String initCurrentFileInfo() {
        String path = getIntent().getStringExtra(INTENT_EXTRA_SELECT_PATH);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                return path;
            }
            mToastHelper.showToast(getString(R.string.path_not_exists, path));
        }
        return mMountPointManager.getRootPath();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapter, View v,
            int position, long id) {
        if (mAdapter.isMode(FileInfoAdapter.MODE_NORMAL)) {
            if (!mMountPointManager.isRootPath(mCurrentPath)
                    && !mService.isBusy(this.getClass().getName())) {
                int top = v.getTop();
                switchToEditView(position, top);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mOrientationConfig) {
            mIsConfigChanged = true;
            mOrientationConfig = newConfig.orientation;
        }
    }

    protected class DetailInfoListener implements
            FileManagerService.OperationEventListener, OnDismissListener {
        public static final String DETAIL_DIALOG_TAG = "detaildialogtag";
        private TextView mDetailsText;
        private final String mName;
        private String mSize;
        private final String mModifiedTime;
        private final String mPermission;
        private final StringBuilder mStringBuilder = new StringBuilder();

        public DetailInfoListener(FileInfo fileInfo) {
            mStringBuilder.setLength(0);
            mName = mStringBuilder.append(getString(R.string.name))
                    .append(": ").append(fileInfo.getFileName()).append("\n")
                    .toString();
            mStringBuilder.setLength(0);
            mSize = mStringBuilder.append(getString(R.string.size))
                    .append(": ").append(FileUtils.sizeToString(0)).append(
                            " \n").toString();

            long time = fileInfo.getFileLastModifiedTime();

            mStringBuilder.setLength(0);
            mModifiedTime = mStringBuilder.append(
                    getString(R.string.modified_time)).append(": ").append(
                    DateFormat.getDateInstance().format(new Date(time)))
                    .append("\n").toString();
            mStringBuilder.setLength(0);
            mPermission = getPermission(fileInfo.getFile());
        }

        private void appendPermission(boolean hasPermission, int title) {
            mStringBuilder.append(getString(title) + ": ");
            if (hasPermission) {
                mStringBuilder.append(getString(R.string.yes));
            } else {
                mStringBuilder.append(getString(R.string.no));
            }
        }

        private String getPermission(File file) {
            appendPermission(file.canRead(), R.string.readable);
            mStringBuilder.append("\n");
            appendPermission(file.canWrite(), R.string.writable);
            mStringBuilder.append("\n");
            appendPermission(file.canExecute(), R.string.executable);

            return mStringBuilder.toString();
        }

        @Override
        public void onTaskPrepare() {
            AlertDialogFragmentBuilder builder = new AlertDialogFragmentBuilder();
            AlertDialogFragment detialFragment = builder.setCancelTitle(
                    R.string.ok).setLayout(R.layout.dialog_details).setTitle(
                    R.string.details).create();

            detialFragment.setDismissListener(this);
            detialFragment.show(getFragmentManager(), DETAIL_DIALOG_TAG);
            getFragmentManager().executePendingTransactions();
            if (detialFragment.getDialog() != null) {
                mDetailsText = (TextView) detialFragment.getDialog()
                        .findViewById(R.id.details_text);
                mStringBuilder.setLength(0);
                if (mDetailsText != null) {
                    mDetailsText.setText(mStringBuilder.append(mName).append(
                            mSize).append(mModifiedTime).append(mPermission)
                            .toString());
                }
            }
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            mSize = getString(R.string.size) + ": "
                    + FileUtils.sizeToString(progressInfo.getTotal()) + " \n";
            if (mDetailsText != null) {
                mStringBuilder.setLength(0);
                mStringBuilder.append(mName).append(mSize)
                        .append(mModifiedTime).append(mPermission);
                mDetailsText.setText(mStringBuilder.toString());
            }
        }

        @Override
        public void onTaskResult(int result) {
            return;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (mService != null) {
                LogUtils.d(this.getClass().getName(), "onDismiss");
                mService.cancel(FileManagerOperationActivity.this.getClass()
                        .getName());
            }
        }
    }

    protected class HeavyOperationListener implements
            FileManagerService.OperationEventListener, View.OnClickListener {
        int mTitle = R.string.deleting;

        private boolean mPermissionToast = false;
        private boolean mOperationToast = false;
        public static final String HEAVY_DIALOG_TAG = "HeavyDialogFragment";

        public HeavyOperationListener(int titleID) {
            mTitle = titleID;
        }

        @Override
        public void onTaskPrepare() {
            ProgressDialogFragment heavyDialogFragment = ProgressDialogFragment
                    .newInstance(ProgressDialog.STYLE_SPINNER, mTitle,                 //modefy by qrt baiwuqiang for SW00032271 2014-02-10
                            R.string.wait, R.string.cancel);
            heavyDialogFragment.setCancelListener(this);
            heavyDialogFragment.show(getFragmentManager(), HEAVY_DIALOG_TAG);
            getFragmentManager().executePendingTransactions();
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            if (progressInfo.isFailInfo()) {
                switch (progressInfo.getErrorCode()) {
                case OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION:
                    if (!mPermissionToast) {
                        mToastHelper.showToast(R.string.copy_deny);
                        mPermissionToast = true;
                    }
                    break;
                case OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION:
                    if (!mPermissionToast) {
                        mToastHelper.showToast(R.string.delete_deny);
                        mPermissionToast = true;
                    }
                    break;
                case OperationEventListener.ERROR_CODE_DELETE_UNSUCCESS:
                    if (!mOperationToast) {
                        mToastHelper.showToast(R.string.some_delete_fail);
                        mOperationToast = true;
                    }
                    break;
                case OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS:
                    if (!mOperationToast) {
                        mToastHelper.showToast(R.string.some_paste_fail);
                        mOperationToast = true;
                    }
                    break;
                default:
                    if (!mPermissionToast) {
                        mToastHelper.showToast(R.string.operation_fail);
                        mPermissionToast = true;
                    }
                    break;
                }

            } else {
                ProgressDialogFragment heavyDialogFragment = (ProgressDialogFragment) 
                        getFragmentManager().findFragmentByTag(HEAVY_DIALOG_TAG);
                if (heavyDialogFragment != null) {
                    heavyDialogFragment.setProgress(progressInfo);
                }
            }
        }

        @Override
        public void onTaskResult(int errorType) {
            LogUtils.d(this.getClass().getSimpleName(),
                    "onTaskResult result = " + errorType);
            switch (errorType) {
            case ERROR_CODE_PASTE_TO_SUB:
                mToastHelper.showToast(R.string.paste_sub_folder);
                break;
            case ERROR_CODE_CUT_SAME_PATH:
                mToastHelper.showToast(R.string.paste_same_folder);
                break;
            case ERROR_CODE_NOT_ENOUGH_SPACE:
                mToastHelper.showToast(R.string.insufficient_memory);
                break;
            case ERROR_CODE_DELETE_FAILS:
                mToastHelper.showToast(R.string.delete_fail);
                break;
            case ERROR_CODE_COPY_NO_PERMISSION:
                mToastHelper.showToast(R.string.copy_deny);
                break;
            default:
                mFileInfoManager.updateFileInfoList(mCurrentPath, mSortType);
                mAdapter.notifyDataSetChanged();
                break;
            }
            ProgressDialogFragment heavyDialogFragment = (ProgressDialogFragment) 
                    getFragmentManager().findFragmentByTag(HEAVY_DIALOG_TAG);
            if (heavyDialogFragment != null) {
                heavyDialogFragment.dismissAllowingStateLoss();
            }
            if (mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_CUT) {
                mFileInfoManager.clearPasteList();
                mAdapter.notifyDataSetChanged();
            } else if (mFileInfoManager.getPasteType() == FileInfoManager.PASTE_MODE_COPY) {
                // Log for calculate performance of operation "copy/paste 10000 files"
                LogUtils.performance("[Copy/Paste]1000files,end time="
                        + System.currentTimeMillis());
            }

            invalidateOptionsMenu();
        }

        @Override
        public void onClick(View v) {
            if (mService != null) {
                LogUtils.i(this.getClass().getName(), "onClick cancel");
                mService.cancel(FileManagerOperationActivity.this.getClass()
                        .getName());
            }
        }
    }
    
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            Intent intent = new Intent();
            intent.setClass(this, FileManagerSearchActivity.class);
            intent.putExtra(FileManagerSearchActivity.CURRENT_PATH,
                    mCurrentPath);
            intent.putExtra("isHidenFile", getPrefsShowHidenFile());//QRT  add by yanlei 20131216  do not search the hidden file when set don't show hidden file
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
			return true;
		} else if(keyCode == KeyEvent.KEYCODE_MENU){
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	ArrayList<String> encryptFilesList = new ArrayList<String>();
	ArrayList<String> decryptFilesList = new ArrayList<String>();

	public void setEncryptFilesList(ArrayList<String> selFileList){
		encryptFilesList = selFileList;
	}

	public void setDecryptFilesList(ArrayList<String> selFileList){
		decryptFilesList = selFileList;
	}

	public ArrayList<String> getEncryptFilesList(){
		return encryptFilesList;
	}

	public ArrayList<String> getDecryptFilesList(){
		return decryptFilesList;
	}

	/**
	* 方法中传入的文件在其文件目录中已存在。
	* @param v
	*/
	public void callIrisEncrypt(ArrayList<String> paths) {
		if(paths == null) return;
		Log.e(TAG, "callIrisEncrypt:" + paths);

		setEncryptFilesList(paths);

		Intent intent = new Intent(requestEncryptAction);
		Bundle b = new Bundle();
		
		b.putStringArrayList(opeFilePaths, paths);
		b.putString(opeFilesBgColor, "#fffff0"); // 自定义背景
		b.putInt(requestCodeVar, REQUEST_CODE_IRIS_ENCRYPT);
		intent.putExtras(b);		
		startActivityForResult(intent, REQUEST_CODE_IRIS_ENCRYPT);
		
	}	

	/**
	*
	* @param v
	*/
	public void callIrisDecrypt(ArrayList<String> paths) {
		if(paths == null) return;
		Log.e(TAG, "callIrisDecrypt:" + paths);

		setDecryptFilesList(paths);

		Intent intent = new Intent(requestEncryptAction);
		Bundle b = new Bundle();
		
		b.putStringArrayList(opeFilePaths, paths); b.putString(opeFilesBgColor, "#fffff0"); // #D9D9D9
		b.putInt(requestCodeVar, REQUEST_CODE_IRIS_DECRYPT);
		intent.putExtras(b);
		startActivityForResult(intent, REQUEST_CODE_IRIS_DECRYPT);
		
	}

	/**
	* 用于接收返回的结果
	* 返回结果的集合列表中存放的是文件的绝对路径。
	*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final StringBuilder mStringBuilder = new StringBuilder();
		StringBuilder resultStringBuilder = new StringBuilder();
		String successFile = "";
		String repeatFile = "";
		String noExistFile = "";
		String exceptionFile = "";
		String encryptedFile = "";
		String unencryptedFile = "";
		String outOfLengthFile = "";
		String quitOperatingFile = "";
		String irisPwdUnmatch = "";

		ArrayList<String> listSameName = null;
		ArrayList<String> listErr = null;
		ArrayList<String> encryptedFs = null;
		ArrayList<String> noExistFs = null;
		ArrayList<String> outOfLengthFs = null;
		ArrayList<String> quitOperatingFilesTmp = null;
		ArrayList<String > unencryptedFs = null;
		ArrayList<String> pwdErrList = null;

		Log.e(TAG, "onActivityResult:" + resultCode);

		if(data == null){
			Log.e(TAG, "onActivityResult(), data is null!");
			return;
		}
		
		switch (resultCode) {
			case RESULT_CODE_IRIS_ENCRYPT:				
				
				if (data.getStringArrayListExtra(existSameNameFiles) != null) {
					listSameName = data.getStringArrayListExtra(existSameNameFiles);
				}
				
				if (data.getStringArrayListExtra(errorFiles) != null) {
					listErr = data.getStringArrayListExtra(errorFiles);
				}
				
				if(data.getStringArrayListExtra(encryptedFiles) != null){
					encryptedFs = data.getStringArrayListExtra(encryptedFiles);
				}
				
				if(data.getStringArrayListExtra(noExsitFiles) != null){
					noExistFs = data.getStringArrayListExtra(noExsitFiles);
				}
				
				if (data.getStringArrayListExtra(outOfLengthFiles) != null) {
					outOfLengthFs = data.getStringArrayListExtra(outOfLengthFiles);
				}

				
				if (data.getStringArrayListExtra(quitOperatingFiles) != null) {
					quitOperatingFilesTmp = data.getStringArrayListExtra(quitOperatingFiles);
				}
				
				if (data.getStringExtra(responseInfos) != null) {						
					String encryptResponse = data.getStringExtra(responseInfos);						
					Log.e(TAG, "encrypt responseInfos:" + encryptResponse);
					
					if (encryptResponse.equals(encryptDone)) {
						resultStringBuilder.setLength(0);
						
						if(getEncryptFilesList().size() > 0){
							mStringBuilder.setLength(0);
				            successFile = mStringBuilder.append(getEncryptFilesList().size() 
								- listSameName.size() -  noExistFs.size() - listErr.size() - encryptedFs.size() 
								- outOfLengthFs.size() - quitOperatingFilesTmp.size() + " ")
								.append(getString(R.string.encrypt_files_success))
				                .toString();
							resultStringBuilder.append(successFile);
						}

						if(listSameName.size() > 0){
							mStringBuilder.setLength(0);
				            repeatFile = mStringBuilder.append(
				                    listSameName.size() + " ").append(getString(R.string.encrypt_files_repeat))
				                    .toString();
							resultStringBuilder.append("\n").append(repeatFile);
						}

						if(noExistFs.size() > 0){
							mStringBuilder.setLength(0);
				            noExistFile = mStringBuilder.append(
				                    noExistFs.size() + " ").append(getString(R.string.encrypt_files_no_exist))
				                    .toString();
							resultStringBuilder.append("\n").append(noExistFile);
						}

						if(listErr.size() > 0){
							mStringBuilder.setLength(0);
				            exceptionFile = mStringBuilder.append(
				                    listErr.size() + " ").append(getString(R.string.encrypt_files_exception))
				                    .toString();
							resultStringBuilder.append("\n").append(exceptionFile);
						}

						if(encryptedFs.size() > 0){
							mStringBuilder.setLength(0);
				            encryptedFile = mStringBuilder.append(
				                    encryptedFs.size() + " ").append(getString(R.string.encrypt_already_encrypted))
				                    .toString();
							resultStringBuilder.append("\n").append(encryptedFile);
						}

						if(outOfLengthFs.size() > 0){
							mStringBuilder.setLength(0);
				            outOfLengthFile = mStringBuilder.append(
				                    outOfLengthFs.size() + " ").append(getString(R.string.outof_length))
				                    .toString();
							resultStringBuilder.append("\n").append(outOfLengthFile);
						}

						if(quitOperatingFilesTmp.size() > 0){
							mStringBuilder.setLength(0);
				            quitOperatingFile = mStringBuilder.append(
				                    quitOperatingFilesTmp.size() + " ").append(getString(R.string.quit_operating))
				                    .toString();
							resultStringBuilder.append("\n").append(quitOperatingFile);
						}

						showEncryptResult(resultCode, resultStringBuilder.toString());

					}
					else if (encryptResponse.equals(opeFilePathsIsNull)) {
						Toast.makeText(this, R.string.null_file_list, Toast.LENGTH_SHORT).show();
					} else if (encryptResponse.equals(noIrisTemplate)) {
						registerIrisTemplate();
						//Toast.makeText(this, R.string.first_register_iris, Toast.LENGTH_SHORT).show();
					}					
				}			
				break;
				
			case RESULT_CODE_IRIS_DECRYPT:
				
				if (data.getStringArrayListExtra(errorFiles) != null) {
					listErr = data.getStringArrayListExtra(errorFiles);
				}
				
				if(data.getStringArrayListExtra(unencryptedFiles) != null){
					unencryptedFs =data.getStringArrayListExtra(unencryptedFiles);
				}
				
				if(data.getStringArrayListExtra(noExsitFiles) != null){
					noExistFs = data.getStringArrayListExtra(noExsitFiles);
				}

				if (data.getStringArrayListExtra(outOfLengthFiles) != null) {
					outOfLengthFs = data.getStringArrayListExtra(outOfLengthFiles);
				}

				if (data.getStringArrayListExtra(quitOperatingFiles) != null) {
					quitOperatingFilesTmp = data.getStringArrayListExtra(quitOperatingFiles);
				}
				
				if (data.getStringArrayListExtra(pwdErr) != null){
					pwdErrList = data.getStringArrayListExtra(pwdErr);
				}
				
				if (data.getStringExtra(responseInfos) != null) {
					String decryptResponse = data.getStringExtra(responseInfos);						
					Log.e(TAG, "decrypt responseInfos:" + decryptResponse);
					
					if (decryptResponse.equals(decryptDone)) {
						resultStringBuilder.setLength(0);
						
						if(getDecryptFilesList().size() > 0){
							mStringBuilder.setLength(0);
				            successFile = mStringBuilder.append(getDecryptFilesList().size() 
								-  noExistFs.size() - listErr.size() - unencryptedFs.size() 
								- outOfLengthFs.size() - quitOperatingFilesTmp.size() - pwdErrList.size() + " ")
								.append(getString(R.string.decrypt_files_success))
				                .toString();
							resultStringBuilder.append(successFile);
						}

						if(noExistFs.size() > 0){
							mStringBuilder.setLength(0);
				            noExistFile = mStringBuilder.append(
				                    noExistFs.size() + " ").append(getString(R.string.decrypt_files_no_exist))
				                    .toString();								
							resultStringBuilder.append("\n").append(noExistFile);
						}

						if(listErr.size() > 0) {
							mStringBuilder.setLength(0);
				            exceptionFile = mStringBuilder.append(
				                    listErr.size() + " ").append(getString(R.string.decrypt_files_exception))
				                    .toString();
							resultStringBuilder.append("\n").append(exceptionFile);
						}

						if(unencryptedFs.size() > 0){
							mStringBuilder.setLength(0);
				            unencryptedFile = mStringBuilder.append(
				                    unencryptedFs.size() + " ").append(getString(R.string.decrypt_unencrypted))
				                    .toString();

							resultStringBuilder.append("\n").append(unencryptedFile);
						}

						if(outOfLengthFs.size() > 0){
							mStringBuilder.setLength(0);
				            outOfLengthFile = mStringBuilder.append(
				                    outOfLengthFs.size() + " ").append(getString(R.string.outof_length))
				                    .toString();
							resultStringBuilder.append("\n").append(outOfLengthFile);
						}

						if(quitOperatingFilesTmp.size() > 0){
							mStringBuilder.setLength(0);
				            quitOperatingFile = mStringBuilder.append(
				                    quitOperatingFilesTmp.size() + " ").append(getString(R.string.quit_operating))
				                    .toString();
							resultStringBuilder.append("\n").append(quitOperatingFile);
						}
						
						if(pwdErrList.size() > 0){
							mStringBuilder.setLength(0);
				            irisPwdUnmatch = mStringBuilder.append(
				                    pwdErrList.size() + " ").append(getString(R.string.iris_pwd_unmatch))
				                    .toString();
							resultStringBuilder.append("\n").append(irisPwdUnmatch);
						}
						
						showEncryptResult(resultCode, resultStringBuilder.toString());

					}
					else if (decryptResponse.equals(opeFilePathsIsNull)) {
						Toast.makeText(this, R.string.null_file_list, Toast.LENGTH_SHORT).show();
					} else if (decryptResponse.equals(noIrisTemplate)) {
						Toast.makeText(this, R.string.first_register_iris, Toast.LENGTH_SHORT).show();
					}else if(decryptResponse.equals(matchTimeout)){
						Toast.makeText(this, R.string.match_timeout, Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case RESULT_CODE_IRIS_REGIST:
				if(data != null){
					if(data.getStringExtra(responseInfos) != null
						&& data.getStringExtra(responseInfos).equals(regOk)){
						Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
					}
				}
				break;
				
			default:
				break;
		}
		
	}

	private void showEncryptResult(int type, String resultString){
		
		ResultDialogFragmentBuilder builder = new ResultDialogFragmentBuilder();
		Context mContext = getApplicationContext();
		Uri uri = Uri.parse("file://"+ mCurrentPath);

		Log.e(TAG, "showEncryptResult:" + mCurrentPath);

		switch(type){
			case RESULT_CODE_IRIS_ENCRYPT:
				builder.setResult(resultString).setTitle(
	                R.string.encrypt_success).setCancelTitle(R.string.ok);
				
		   		mContext.sendBroadcast(new Intent( ACTION_MEDIA_SCANNER_SCAN_ALL, uri));				
				break;

			case RESULT_CODE_IRIS_DECRYPT:
				builder.setResult(resultString).setTitle(
                	R.string.decrypt_success).setCancelTitle(R.string.ok);
				
		   		mContext.sendBroadcast(new Intent( ACTION_MEDIA_SCANNER_SCAN_ALL, uri));				
				break;

			default:

				break;
		}
		ResultDialogFragment resultDialogFragment = builder.create();
        resultDialogFragment.show(getFragmentManager(), TAG);
    }		

	public void registerIrisTemplate(){
		new AlertDialog.Builder(FileManagerOperationActivity.this).setTitle(R.string.register_title)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setMessage(R.string.first_register_iris)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(requestIrisAction);
				Bundle b = new Bundle();
				b.putInt(requestCodeVar, REQUEST_CODE_IRIS_REGIST);
				intent.putExtras(b);
				startActivityForResult(intent, REQUEST_CODE_IRIS_REGIST);
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.show();
	}

}
