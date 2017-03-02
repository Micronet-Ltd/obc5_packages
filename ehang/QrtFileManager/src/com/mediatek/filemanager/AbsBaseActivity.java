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

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mediatek.filemanager.AlertDialogFragment.EditDialogFragmentBuilder;
import com.mediatek.filemanager.AlertDialogFragment.EditTextDialogFragment;
import com.mediatek.filemanager.AlertDialogFragment.EditTextDialogFragment.EditTextDoneListener;
import com.mediatek.filemanager.FileInfoManager.NavigationRecord;
import com.mediatek.filemanager.MountReceiver.MountListener;
import com.mediatek.filemanager.service.FileManagerService;
import com.mediatek.filemanager.service.FileManagerService.ServiceBinder;
import com.mediatek.filemanager.service.ProgressInfo;
import com.mediatek.filemanager.utils.LogUtils;
import com.mediatek.filemanager.utils.ToastHelper;

import java.util.ArrayList;
import java.util.List;
import android.os.Environment;
import android.util.Log;

/**
 * This is the base activity for FileInfoManager(Activity), SelectFileActivity, SelectPathActivity,
 * SearchActivity. It defines the basic views and interactions for activities.
 */
public abstract class AbsBaseActivity extends Activity implements OnItemClickListener,
        OnClickListener, MountListener {
    private static final String TAG = "FileManagerBaseActivity";
    public static final String SAVED_PATH_KEY = "saved_path";
    private static final long NAV_BAR_AUTO_SCROLL_DELAY = 100;
    /** maximum tab text length */
    private static final int TAB_TEXT_LENGTH = 11;

    protected static final int DIALOG_CREATE_FOLDER = 1;

    /** ListView used for showing Files */
    protected ListView mListView = null;
    protected FileInfoAdapter mAdapter = null;
    protected TabManager mTabManager = null;
    protected HorizontalScrollView mNavigationBar = null;
    protected FileInfo mSelectedFileInfo = null;
    protected MountPointManager mMountPointManager = null;
    protected MountReceiver mMountReceiver = null;
    protected FileManagerService mService = null;
    protected int mTop = -1;
    protected int mSortType = 0;
    protected String mCurrentPath = null;
    protected ToastHelper mToastHelper = null;
    protected FileInfoManager mFileInfoManager = null;
    public static final String CREATE_FOLDER_DIALOG_TAG = "CreateFolderDialog";
    protected Bundle mSavedInstanceState = null;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.disconnected(this.getClass().getName());
            LogUtils.w(TAG, "onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "onServiceConnected");
            mService = ((ServiceBinder) service).getServiceInstance();
            serviceConnected();
        }
    };

    @Override
    public void onMounted() {

        if (mMountPointManager.isRootPath(mCurrentPath)) {
            LogUtils.d(TAG, "Mount SDCard");
            showDirectoryContent(mCurrentPath);
        }
    }

    @Override
    public void onUnmounted(String mountPoint) {

        LogUtils.i(TAG, "mountPoint: " + mountPoint);
        if (mFileInfoManager != null && mFileInfoManager.getPasteCount() > 0) {
            FileInfo fileInfo = mFileInfoManager.getPasteList().get(0);
            if (fileInfo.getFileAbsolutePath().startsWith(
                    mountPoint + MountPointManager.SEPARATOR)) {
                mFileInfoManager.clearPasteList();
                invalidateOptionsMenu();
            }
        }
        if ((mCurrentPath + MountPointManager.SEPARATOR).startsWith(mountPoint
                + MountPointManager.SEPARATOR)
                || mMountPointManager.isRootPath(mCurrentPath)) {
            LogUtils.d(TAG, "onUnmounted");
            if (mService != null && mService.isBusy(this.getClass().getName())) {
                mService.cancel(this.getClass().getName());
            }
            showToastForUnmountCurrentSDCard(mountPoint);

            DialogFragment listFramgent = (DialogFragment) getFragmentManager()
                    .findFragmentByTag(ListListener.LIST_DIALOG_TAG);
            if (listFramgent != null) {
                listFramgent.dismissAllowingStateLoss();
            }
            EditTextDialogFragment createFolderDialogFragment = (EditTextDialogFragment) 
                    getFragmentManager().findFragmentByTag(CREATE_FOLDER_DIALOG_TAG);
            if (createFolderDialogFragment != null) {
                createFolderDialogFragment.dismissAllowingStateLoss();
            }

            backToRootPath();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSavedInstanceState = savedInstanceState;

        LogUtils.d(TAG, "onCreate");
        mToastHelper = new ToastHelper(this);
        // start watching external storage change
        MountPointManager.getInstance().init(getApplicationContext());
        mMountPointManager = MountPointManager.getInstance();
        mMountReceiver = MountReceiver.registerMountReceiver(this);
        mMountReceiver.registerMountListener(this);

        bindService(new Intent(getApplicationContext(),
                FileManagerService.class), mServiceConnection, BIND_AUTO_CREATE);

        setMainContentView();

        // set up a sliding navigation bar for navigation view
        mNavigationBar = (HorizontalScrollView) findViewById(R.id.navigation_bar);
        if (mNavigationBar != null) {
            mNavigationBar.setVerticalScrollBarEnabled(false);
            mNavigationBar.setHorizontalScrollBarEnabled(false);
            mTabManager = new TabManager();
        }

        // set up a list view
        mListView = (ListView) findViewById(R.id.list_view);
        if (mListView != null) {
            mListView.setEmptyView(findViewById(R.id.empty_view));
            mListView.setOnItemClickListener(this);
            mListView.setFastScrollEnabled(true);
            mListView.setVerticalScrollBarEnabled(false);
        }
		
    }

    private void reloadContent() {
        LogUtils.d(TAG, "reloadContent");
        if (mService != null && !mService.isBusy(this.getClass().getName())) {
            if (mFileInfoManager != null
                    && mFileInfoManager.isPathModified(mCurrentPath)) {
                showDirectoryContent(mCurrentPath);
            } else if (mFileInfoManager != null && mAdapter != null) {
                for (int i = 0; i < mAdapter.getCount(); i++) {
                    if (mAdapter.getItem(i).isDrmFile()) {
                        mAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume");
        reloadContent();
    }

    protected void showCreateFolderDialog() {
        LogUtils.d(TAG, "showCreateFolderDialog");
        EditDialogFragmentBuilder builder = new EditDialogFragmentBuilder();
        builder.setDefault("", 0).setDoneTitle(R.string.ok).setCancelTitle(
                R.string.cancel).setTitle(R.string.new_folder);
        EditTextDialogFragment createFolderDialogFragment = builder.create();
        createFolderDialogFragment
                .setOnEditTextDoneListener(new CreateFolderListener());
        createFolderDialogFragment.show(getFragmentManager(),
                CREATE_FOLDER_DIALOG_TAG);
    }

    protected final class CreateFolderListener implements EditTextDoneListener {

        public void onClick(String text) {
            if (mService != null) {
                String dstPath = mCurrentPath + MountPointManager.SEPARATOR
                        + text;
                mService.createFolder(
                        AbsBaseActivity.this.getClass().getName(), dstPath,
                        new LightOperationListener(text));
            }
        }
    }

    /**
     * This method is left for its children class to set main layout
     */
    protected abstract void setMainContentView();

    @Override
    protected void onDestroy() {
        LogUtils.d(TAG, "onDestroy");
        if (mService != null) {
            unbindService(mServiceConnection);
        }

        unregisterReceiver(mMountReceiver);
        super.onDestroy();
    }

    private void backToRootPath() {

        if (mMountPointManager != null
                && mMountPointManager.isRootPath(mCurrentPath)) {
            showDirectoryContent(mCurrentPath);
        } else if (mTabManager != null) {
            mTabManager.updateNavigationBar(0);
        }
        clearNavigationList();
    }

    protected void showToastForUnmountCurrentSDCard(String path) {
        if (isResumed()) {
            String mountPointDescription = MountPointManager.getInstance()
                    .getDescriptionPath(path);
            mToastHelper.showToast(getString(R.string.unmounted,
                    mountPointDescription));
        }
    }

    /**
     * This method add a path into navigation history list
     * 
     * @param dirPath the path that should be added
     */
    protected void addToNavigationList(String path, FileInfo selectedFileInfo,
            int top) {
        mFileInfoManager.addToNavigationList(new NavigationRecord(path,
                selectedFileInfo, top));
    }

    /**
     * This method clear navigation history list
     */
    protected void clearNavigationList() {
        mFileInfoManager.clearNavigationList();
    }

    /**
     * This method used to be inherited by subclass to get a path.
     * 
     * @return path to a folder
     */
    protected abstract String initCurrentFileInfo();

    protected class TabManager {
        private final List<String> mTabNameList = new ArrayList<String>();
        protected LinearLayout mTabsHolder = null;
        private String mCurFilePath = null;
        private final Button mBlankTab;

        public TabManager() {
            mTabsHolder = (LinearLayout) findViewById(R.id.tabs_holder);
            mBlankTab = new Button(AbsBaseActivity.this);
            mBlankTab.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.fm_blank_tab));
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                    new ViewGroup.MarginLayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT));

            mlp.setMargins((int) getResources().getDimension(
                    R.dimen.tab_margin_left), 0, (int) getResources()
                    .getDimension(R.dimen.tab_margin_right), 0);
            mBlankTab.setLayoutParams(mlp);
            mTabsHolder.addView(mBlankTab);
        }

        public void refreshTab(String initFileInfo) {
            int count = mTabsHolder.getChildCount();
            mTabsHolder.removeViews(0, count);
            mTabNameList.clear();

            mCurFilePath = initFileInfo;
            if (mCurFilePath != null) {
                addTab(MountPointManager.HOME);
                if (!mMountPointManager.isRootPath(mCurFilePath)) {
                    String path = mMountPointManager
                            .getDescriptionPath(mCurFilePath);
                    String[] result = path.split(MountPointManager.SEPARATOR);
                    for (String string : result) {
                        addTab(string);
                    }
                }
            }
            updateHomeButton();
        }

        protected void updateHomeButton() {
            ImageButton homeBtn = (ImageButton) mTabsHolder.getChildAt(0);
            if (homeBtn == null) {
                LogUtils.w(TAG, "HomeBtm == null");
                return;
            }
            Resources resources = getResources();
            if (mTabsHolder.getChildCount() == 2) { // two tabs: home tab + blank
                // tab
                homeBtn.setBackgroundDrawable(resources
                        .getDrawable(R.drawable.custom_home_ninepatch_tab));
                homeBtn.setImageDrawable(resources
                        .getDrawable(R.drawable.ic_home_text));
                homeBtn.setPadding((int) resources
                        .getDimension(R.dimen.home_btn_padding), 0,
                        (int) resources.getDimension(R.dimen.home_btn_padding),
                        0);
            } else {
                homeBtn.setBackgroundDrawable(resources
                        .getDrawable(R.drawable.custom_home_ninepatch_tab));
                homeBtn.setImageDrawable(resources
                        .getDrawable(R.drawable.ic_home));
            }
        }

        /**
         * This method updates the navigation view to the previous view when back button is pressed
         * 
         * @param newPath the previous showed directory in the navigation history
         */
        private void showPrevNavigationView(String newPath) {

            refreshTab(newPath);
            showDirectoryContent(newPath);
        }

        /**
         * This method creates tabs on the navigation bar
         * 
         * @param text the name of the tab
         */
        protected void addTab(String text) {
            LinearLayout.LayoutParams mlp = null;

            mTabsHolder.removeView(mBlankTab);
            View btn = null;
            if (mTabNameList.isEmpty()) {
                btn = new ImageButton(AbsBaseActivity.this);
                mlp = new LinearLayout.LayoutParams(
                        new ViewGroup.MarginLayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                mlp.setMargins(0, 0, 0, 0);
                btn.setLayoutParams(mlp);
            } else {
                btn = new Button(AbsBaseActivity.this);
                ((Button) btn).setTextColor(Color.BLACK);
                btn.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.custom_tab));
					//zhoukai modified
					((Button) btn).setText(text);
               /* if (text.length() <= TAB_TEXT_LENGTH) {
                    ((Button) btn).setText(text);
                } else {
                    ((Button) btn).setText(text.substring(0, TAB_TEXT_LENGTH));
                }*/
                mlp = new LinearLayout.LayoutParams(
                        new ViewGroup.MarginLayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.MATCH_PARENT));
                mlp.setMargins((int) getResources().getDimension(
                        R.dimen.tab_margin_left), 0, 0, 0);
                btn.setLayoutParams(mlp);

            }
            btn.setOnClickListener(AbsBaseActivity.this);
            btn.setId(mTabNameList.size());
            mTabsHolder.addView(btn);
            mTabNameList.add(text);

            // add blank tab to the tab holder

            mTabsHolder.addView(mBlankTab);

            // scroll horizontal view to the right
            mNavigationBar.postDelayed(new Runnable() {
                public void run() {
                    mNavigationBar.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                }
            }, NAV_BAR_AUTO_SCROLL_DELAY);
        }

        /**
         * The method updates the navigation bar
         * 
         * @param id the tab id that was clicked
         */
        protected void updateNavigationBar(int id) {
            // click current button do not response
            if (id < mTabNameList.size() - 1) {
                int count = mTabNameList.size() - id;
                mTabsHolder.removeViews(id + 1, count);

                for (int i = 1; i < count; i++) {
                    // update mTabNameList
                    mTabNameList.remove(mTabNameList.size() - 1);
                }
                mTabsHolder.addView(mBlankTab);

                if (id == 0) {
                    mCurFilePath = mMountPointManager.getRootPath();
                } else {
                    String[] result = mCurFilePath
                            .split(MountPointManager.SEPARATOR);
                    StringBuilder sb = new StringBuilder();
                    // add by qrt laiwugang to modefy mInternalPath split to 3 (X825A) 2013-10-09
                    String mInternalPath = Environment.getExternalStorageDirectory().toString();
                    if(mCurFilePath.startsWith(mInternalPath)){
                    	String[] mInternalPathSplit = mInternalPath.split(MountPointManager.SEPARATOR);
                    	id = id + mInternalPathSplit.length -3;
                    }
                    for (int i = 0; i <= id; i++) {
                        sb.append(MountPointManager.SEPARATOR);
                        sb.append(result[i + 1]);
                    }
                    mCurFilePath = sb.toString();
                }

                int top = -1;
                FileInfo selectedFileInfo = null;
                if (mListView.getCount() > 0) {
                    View view = mListView.getChildAt(0);
                    selectedFileInfo = mAdapter.getItem(mListView
                            .getPositionForView(view));
                    top = view.getTop();
                }
                addToNavigationList(mCurrentPath, selectedFileInfo, top);
                showDirectoryContent(mCurFilePath);
				Log.e("aaaaa", "mCurFilePath=   "+mCurFilePath);//addd
                updateHomeButton();
            }
        }

    }

    @Override
    public void onClick(View view) {
        if (mService.isBusy(this.getClass().getName())) {
            return;
        }
        int id = view.getId();
        LogUtils.d(TAG, "onClick() id=" + id);
        mTabManager.updateNavigationBar(id);
    }

    private int restoreSelectedPosition() {
        if (mSelectedFileInfo == null) {
            return -1;
        } else {
            int curSelectedItemPosition = mAdapter
                    .getPosition(mSelectedFileInfo);
            mSelectedFileInfo = null;
            return curSelectedItemPosition;
        }
    }

    /**
     * This method gets all files/folders from a directory and displays them in the list view
     * 
     * @param dirPath the directory path
     */
    protected void showDirectoryContent(String path) {
        LogUtils.d(TAG, "Get files/folders in the directory " + path);
        if (isFinishing()) {
            LogUtils.d(TAG, "isFinishing: true, do not loading again");
            return;
        }
        mCurrentPath = path;
        if (mService != null) {
            mService.listFiles(this.getClass().getName(), mCurrentPath,
                    new ListListener());
        }
    }

    protected void onPathChanged() {
        if (mTabManager != null) {
            mTabManager.refreshTab(mCurrentPath);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        LogUtils.d(this.getClass().getName(), "onBackPressed");
        if (mService != null && mService.isBusy(this.getClass().getName())) {
            return;
        }
        if (mCurrentPath != null
                && !mMountPointManager.isRootPath(mCurrentPath)) {
            NavigationRecord navRecord = mFileInfoManager.getPrevNavigation();
            String prevPath = null;
            if (navRecord != null) {
                prevPath = navRecord.getRecordPath();
                mSelectedFileInfo = navRecord.getSelectedFile();
                mTop = navRecord.getTop();
                if (prevPath != null) {
                    mTabManager.showPrevNavigationView(prevPath);
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    protected void serviceConnected() {
        LogUtils.d(TAG, "serviceInitSuccess");

        mFileInfoManager = mService.initFileInfoManager(this.getClass()
                .getName());
        mAdapter = new FileInfoAdapter(AbsBaseActivity.this, mService,
                mFileInfoManager);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);

            if (mSavedInstanceState == null) {
                mCurrentPath = initCurrentFileInfo();
                if (mCurrentPath != null) {
                    showDirectoryContent(mCurrentPath);
                }
            } else {
                String savePath = mSavedInstanceState.getString(SAVED_PATH_KEY);
                if (savePath != null
                        && mMountPointManager.isMounted(mMountPointManager
                                .getRealMountPointPath(savePath))) {
                    mCurrentPath = savePath;
                } else {
                    mCurrentPath = initCurrentFileInfo();
                }

                if (mCurrentPath != null) {
                    mTabManager.refreshTab(mCurrentPath);
                    reloadContent();
                }
                restoreDialog();

            }
            mAdapter.notifyDataSetChanged();
        }
    }

    protected void restoreDialog() {
        DialogFragment listFramgent = (DialogFragment) getFragmentManager()
                .findFragmentByTag(ListListener.LIST_DIALOG_TAG);
        if (listFramgent != null) {
            LogUtils.i(TAG, "listFramgent != null");
            if (mService.isBusy(this.getClass().getName())) {
                LogUtils.i(TAG, "list reconnected mService");
                mService.reconnected(this.getClass().getName(),
                        new ListListener());
            } else {
                LogUtils
                        .i(TAG, "the list is complete dismissAllowingStateLoss");
                listFramgent.dismissAllowingStateLoss();
            }
        }
        EditTextDialogFragment createFolderDialogFragment = (EditTextDialogFragment) 
                getFragmentManager().findFragmentByTag(CREATE_FOLDER_DIALOG_TAG);
        if (createFolderDialogFragment != null) {
            createFolderDialogFragment
                    .setOnEditTextDoneListener(new CreateFolderListener());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mCurrentPath != null) {
            outState.putString(SAVED_PATH_KEY, mCurrentPath);
        }
        super.onSaveInstanceState(outState);
    }

    protected class ListListener implements
            FileManagerService.OperationEventListener {

        public static final String LIST_DIALOG_TAG = "ListDialogFragment";

        protected void dismissDialogFragment() {
            LogUtils.d(TAG, "ListListener dismissDialogFragment");
            DialogFragment listDialogFragment = (DialogFragment) getFragmentManager()
                    .findFragmentByTag(LIST_DIALOG_TAG);
            if (listDialogFragment != null) {
                LogUtils.d(TAG,
                        "ListListener listDialogFragment != null dismiss");
                listDialogFragment.dismissAllowingStateLoss();
            }
        }

        @Override
        public void onTaskResult(int result) {
            LogUtils.d(TAG, "List Linstener on TaskResult result = " + result);
            mFileInfoManager.loadFileInfoList(mCurrentPath, mSortType);
            mAdapter.notifyDataSetChanged();
            int seletedItemPosition = restoreSelectedPosition();
            if (seletedItemPosition == -1) {
                mListView.setSelectionAfterHeaderView();
            } else if (seletedItemPosition >= 0
                    && seletedItemPosition < mAdapter.getCount()) {
                if (mTop == -1) {
                    mListView.setSelection(seletedItemPosition);
                } else {
                    mListView.setSelectionFromTop(seletedItemPosition, mTop);
                    mTop = -1;
                }
            }
            dismissDialogFragment();
            onPathChanged();
            LogUtils.performance("[Loading]10000files,end time="
                    + System.currentTimeMillis());
        }

        @Override
        public void onTaskPrepare() {
            return;
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            ProgressDialogFragment listDialogFragment = (ProgressDialogFragment) 
                    getFragmentManager().findFragmentByTag(LIST_DIALOG_TAG);
            if (isResumed()) {
                if (listDialogFragment == null) {
                    listDialogFragment = ProgressDialogFragment
                            .newInstance(ProgressDialog.STYLE_SPINNER, -1,          //modefy by qrt baiwuqiang for SW00032271 2014-02-10
                                    R.string.loading,
                                    AlertDialogFragment.INVIND_RES_ID);

                    listDialogFragment.show(getFragmentManager(),
                            LIST_DIALOG_TAG);
                    getFragmentManager().executePendingTransactions();
                }
                listDialogFragment.setProgress(progressInfo);
            }
        }
    }

    protected class LightOperationListener implements
            FileManagerService.OperationEventListener {

        String mDstName = null;

        LightOperationListener(String dstName) {
            mDstName = dstName;
        }

        @Override
        public void onTaskResult(int errorType) {
            switch (errorType) {
            case ERROR_CODE_SUCCESS:
            case ERROR_CODE_USER_CANCEL:
                FileInfo fileInfo = mFileInfoManager.updateOneFileInfoList(
                        mCurrentPath, mSortType);
                mAdapter.notifyDataSetChanged();
                if (fileInfo != null) {
                    int postion = mAdapter.getPosition(fileInfo);
                    LogUtils.d(TAG, "LightOperation postion = " + postion);
                    mListView.setSelection(postion);
                    invalidateOptionsMenu();
                }

                break;
            case ERROR_CODE_FILE_EXIST:
                if (mDstName != null) {
                    mToastHelper.showToast(getResources().getString(
                            R.string.already_exists, mDstName));
                }
                break;
            case ERROR_CODE_NAME_EMPTY:
                mToastHelper.showToast(R.string.invalid_empty_name);
                break;
            case ERROR_CODE_NAME_TOO_LONG:
                mToastHelper.showToast(R.string.file_name_too_long);
                break;
            case ERROR_CODE_NOT_ENOUGH_SPACE:
                mToastHelper.showToast(R.string.insufficient_memory);
                break;
            case ERROR_CODE_UNSUCCESS:
                mToastHelper.showToast(R.string.operation_fail);
                break;
            default:
                LogUtils.e(TAG, "wrong errorType for LightOperationListener");
                break;
            }
        }

        @Override
        public void onTaskPrepare() {
            return;
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            return;
        }
    }
    
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
