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

import java.io.IOException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.TextView;

import com.mediatek.filemanager.service.FileManagerService;
import com.mediatek.filemanager.service.ProgressInfo;
import com.mediatek.filemanager.utils.LogUtils;
import com.mediatek.filemanager.utils.MimeTypeParser;
import com.mediatek.filemanager.utils.MimeTypes;


public class FileManagerSearchActivity extends AbsBaseActivity {
    private static final String TAG = "SearchActivity";
    private TextView mResultView = null;
    private String mSearchPath = null;
    private SearchView mSearchView = null;
    private MenuItem mSearchItem;
    private long mTotal = 0;
    private String mSearchText = null;
    public static final String CURRENT_PATH = "current_path";
    public static final String SEARCH_TEXT = "search_text";
    public static final String SEARCH_TOTAL = "search_total";
    
    private boolean isShowHidenFile;//QRT  add by yanlei 20131216  do not search the hidden file when set don't show hidden file

    @Override
    protected void onDestroy() {
        if (mService != null) {
            mService.cancel(this.getClass().getName());
        }
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mSearchText != null) {
            outState.putString(SEARCH_TEXT, mSearchText);
            outState.putLong(SEARCH_TOTAL, mTotal);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void serviceConnected() {
        super.serviceConnected();
        Intent intent = getIntent();
        mSearchPath = intent.getStringExtra(CURRENT_PATH);
        if (mSearchPath == null) {
            mSearchPath = mMountPointManager.getRootPath();
        }
        if (!mSearchPath.endsWith(MountPointManager.SEPARATOR)) {
            mSearchPath = mSearchPath + MountPointManager.SEPARATOR;
        }

        if (mSavedInstanceState != null && mResultView != null) {
            mSearchText = mSavedInstanceState.getString(SEARCH_TEXT);
            if (!TextUtils.isEmpty(mSearchText)) {
                mTotal = mSavedInstanceState.getLong(SEARCH_TOTAL);
                mResultView.setVisibility(View.VISIBLE);
                mResultView.setText(getResources().getString(R.string.search_result, mSearchText,
                        mTotal));
            }
        } else {
            mAdapter.changeMode(FileInfoAdapter.MODE_SEARCH);
        }
        handleIntent(intent);
    }

    /**
     * The method handles received intent.
     * 
     * @param intent the intent FileManagerSearchActivity received.
     */
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String path = null;
            if (intent.getData() != null) {
                path = intent.getData().toString();
            }
            if (TextUtils.isEmpty(path)) {
                LogUtils.w(TAG, "handleIntent intent uri path == null");
                return;
            }
            onItemClick(new FileInfo(path));
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            requestSearch(query);
        }
    }

    /**
     * The method start search task.
     * 
     * @param query the search target.
     */
    private void requestSearch(String query) {

        if (query != null && !query.isEmpty()) {
            if (mService != null) {
                mService.search(this.getClass().getName(), query, mSearchPath, new SearchListener(
                        query),isShowHidenFile);//QRT modified by yanlei 20131216  do not search the hidden file when set don't show hidden file*/
                if (mSearchView != null) {
                    mSearchView.setQuery(query, false);
                    mSearchView.clearFocus();
                }
            }
        } else {
            mToastHelper.showToast(R.string.search_text_empty);
        }
    }

    protected class SearchListener implements FileManagerService.OperationEventListener {
        private static final int FRIST_UPDATE_COUNT = 20;
        private static final int NEED_UPDATE_LIST = 6;
        private boolean mIsResultSet = false;
        private int mCount = 0;

        /**
         * Constructor of SearchListener.
         * 
         * @param text the search target(String), which will be shown on searchResult TextView..
         */
        public SearchListener(String text) {
            if (text == null) {
                throw new IllegalArgumentException();
            }
            mSearchText = text;
        }

        @Override
        public void onTaskResult(int result) {
            mFileInfoManager.updateSearchList();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onTaskPrepare() {
            mAdapter.changeMode(FileInfoAdapter.MODE_SEARCH);
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo) {
            if (!progressInfo.isFailInfo()) {
                if (mResultView != null && !mIsResultSet) {
                    mTotal = progressInfo.getTotal();
                    mResultView.setVisibility(View.VISIBLE);
                    mResultView.setText(getResources().getString(
                            R.string.search_result, mSearchText, mTotal));
                    mIsResultSet = true;
                }
                if (progressInfo.getFileInfo() != null) {
                    mFileInfoManager.addItem(progressInfo.getFileInfo());
                }
                mCount++;
                if (mCount > FRIST_UPDATE_COUNT) {
                    if (mListView.getLastVisiblePosition() + NEED_UPDATE_LIST > mAdapter
                            .getCount()) {
                        mFileInfoManager.updateSearchList();
                        mAdapter.notifyDataSetChanged();
                        mCount = 0;
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_options_menu, menu);
        mSearchItem = menu.findItem(R.id.search);
        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }

        mSearchItem.expandActionView();
        mSearchItem.setOnActionExpandListener(new OnActionExpandListener() {

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });
        if (!TextUtils.isEmpty(mSearchText)) {
            mSearchView.setQuery(mSearchText, false);
            mSearchView.clearFocus();
        }
        return true;
    }

    @Override
    protected String initCurrentFileInfo() {
        return null;
    }

    @Override
    protected void setMainContentView() {
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            finish();
            handleIntent(getIntent());
            return;
        }
        setTheme(R.style.FileManagerOperTheme);
        setContentView(R.layout.search_main);
        mResultView = (TextView) findViewById(R.id.search_result);
        
        isShowHidenFile = getIntent().getBooleanExtra("isShowHidenFile", false);//QRT  add by yanlei 20131216  do not search the hidden file when set don't show hidden file
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {

        LogUtils.d(TAG, "Selected position: " + position);

        if (position >= mAdapter.getCount() || position < 0) {
            LogUtils.e(TAG, "click events error");
            LogUtils.e(TAG, "mFileInfoList.size(): " + mAdapter.getCount());
            return;
        }
        FileInfo selectedFileInfo = (FileInfo) mAdapter.getItem(position);
        onItemClick(selectedFileInfo);
    }

    /**
     * The method deal with the event that a certain childView of listView in searchActivity is
     * clicked.
     * 
     * @param selectedFileInfo The FileInfo associate with the selected childView of listView.
     */
    private void onItemClick(FileInfo selectedFileInfo) {
        if (mService != null) {
            if (selectedFileInfo.isDirectory()) {

                Intent intent = new Intent(this, FileManagerOperationActivity.class);
                intent.putExtra(FileManagerOperationActivity.INTENT_EXTRA_SELECT_PATH,
                        selectedFileInfo.getFileAbsolutePath());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } else {
                // open file here
                boolean canOpen = true;
                MimeTypeParser mtp = new MimeTypeParser();
                XmlResourceParser in = getResources().getXml(R.xml.mimetypes);
                MimeTypes mMimeTypes = null;
                try {
                    mMimeTypes = mtp.fromXmlResource(in);
                } catch (XmlPullParserException e) {
                    Log.e(TAG, "loadMimeTypes: XmlPullParserException", e);
                } catch (IOException e) {
                    Log.e(TAG, "loadMimeTypes: IOException", e);
                }
                final String mimeType = mMimeTypes.getMimeType(selectedFileInfo.getFile().getName());
                //String mimeType = selectedFileInfo.getFileMimeType(mService);

                if (selectedFileInfo.isDrmFile()) {
                    if (TextUtils.isEmpty(mimeType)) {
                        canOpen = false;
                        mToastHelper.showToast(R.string.msg_unable_open_file);
                    }
                }

                if (canOpen) {
                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                    final Uri uri = selectedFileInfo.getUri();
                    LogUtils.d(TAG, "Open uri file: " + uri);
					String [] openFileType = getResources().getStringArray(R.array.open_file);
					for (int i = 0; i < openFileType.length; i++) {
						if(openFileType[i].equals(mimeType)){
				        	new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).
			        		setTitle(R.string.dialog_import_title).setMessage(R.string.dialog_import_msg).
			        		setPositiveButton(android.R.string.ok, new OnClickListener() {
			                            public void onClick(DialogInterface dialog, int which) {  
			                                intent.setDataAndType(uri, mimeType);
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
					if (!mimeType.equals("image/gif") && mimeType.startsWith("image/") && !mimeType.equals("image/tiff")) {
						intent.setDataAndType(uri, "image/*");
					}else{
						intent.setDataAndType(uri, mimeType);	
					}
                    
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        mToastHelper.showToast(R.string.msg_unable_open_file);
                        LogUtils.w(TAG, "Cannot open file: "
                                + selectedFileInfo.getFileAbsolutePath());
                    }
                }
            }
            finish();
        }
    }
}
