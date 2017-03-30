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

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.mediatek.filemanager.service.FileManagerService;
import com.mediatek.filemanager.utils.LogUtils;


/**
 * The activity is used by other application to select a file. It will return the selected file's
 * Uri.
 * 
 * <pre>
 * Uri uri = selecteItemFileInfo.getUri();
 * intent.setData(uri);
 * setResult(RESULT_OK, intent);
 * </pre>
 */
public class FileManagerSelectFileActivity extends AbsBaseActivity {
    private static final String TAG = "FileManagerSelectFileActivity";
    private boolean vcfMimeType;
    private boolean videoMimeType;
    private boolean audioMimeType;
    
    @Override
    protected void setMainContentView() {
        setContentView(R.layout.select_file_main);
        vcfMimeType = false;
        videoMimeType = false;
        audioMimeType = false;
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (type != null && type.length() > 0) {
            if(action.equals(Intent.ACTION_GET_CONTENT) &&( "text/x-vCard".equals(type) || "text/x-vcard".equals(type))){
            	vcfMimeType = true;
            }
            if(action.equals(Intent.ACTION_GET_CONTENT) && "video/*".equals(type)){
            	videoMimeType = true;
            }
            if(action.equals(Intent.ACTION_PICK) && "audio/*".equals(type)){
            	audioMimeType = true;
            }
        }
        Button btnCancel = (Button) findViewById(R.id.select_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.d(TAG, "click 'Cancel' to quit directly ");
                finish();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mService != null && mService.isBusy(this.getClass().getName())) {
            return;
        }

        FileInfo selecteItemFileInfo = (FileInfo) parent.getItemAtPosition(position);
        if (selecteItemFileInfo.isDirectory()) {
            int top = view.getTop();
            LogUtils.v(TAG, "onItemClick directory top = " + top);
            addToNavigationList(mCurrentPath, selecteItemFileInfo, top);
            showDirectoryContent(selecteItemFileInfo.getFileAbsolutePath());
        } else {
            Intent intent = new Intent();
            Uri uri = selecteItemFileInfo.getUri();
            LogUtils.d(TAG, "onItemClick RESULT_OK, uri : " + uri);
            intent.setData(uri);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected String initCurrentFileInfo() {
        String defaultSDCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //if (mMountPointManager.isMounted(defaultSDCardPath)) {
          //  LogUtils.i(TAG, "initCurrentDirPath SDCardMount path: " + defaultSDCardPath);
            //return defaultSDCardPath;
        //} else {
            return mMountPointManager.getRootPath();
        //}
    }

    protected void serviceConnected() {
        super.serviceConnected();
        if(vcfMimeType){
        	mService.setListType(FileManagerService.FILE_FILTER_TYPE_VCARD,this.getClass().getName());	
        	return;
        }
        if(videoMimeType){
        	mService.setListType(FileManagerService.FILE_FILTER_TYPE_VIDEO,this.getClass().getName());
        	return;
        }
        if(audioMimeType){
        	mService.setListType(FileManagerService.FILE_FILTER_TYPE_AUDIO,this.getClass().getName());
        	return;
        }
        mService.setListType(FileManagerService.FILE_FILTER_TYPE_DEFAULT,this.getClass().getName());
    }
}
