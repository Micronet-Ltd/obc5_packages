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

package com.mediatek.filemanager.service;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.mediatek.filemanager.FileInfo;
import com.mediatek.filemanager.FileInfoManager;
import com.mediatek.filemanager.FileManagerApplication;
import com.mediatek.filemanager.MountPointManager;
import com.mediatek.filemanager.service.FileManagerService.OperationEventListener;
import com.mediatek.filemanager.utils.LogUtils;
import com.mediatek.filemanager.utils.MimeTypeParser;
import com.mediatek.filemanager.utils.MimeTypes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;
import com.mediatek.filemanager.R;

class ListFileTask extends BaseAsyncTask {

    private final String mPath;
    private final int mFilterType;
    private static final int FIRST_NEED_PROGRESS = 250;
    private static final int NEXT_NEED_PROGRESS = 200;
    private MimeTypes mMimeTypes;
    private static final String TAG = "ListFileTask";

    /**
     * Constructor for ListFileTask, construct a ListFileTask with certain parameters
     * 
     * @param fileInfoManager a instance of FileInfoManager, which manages information of files in
     *            FileManager.
     * @param operationEvent a instance of OperationEventListener, which is a interface doing things
     *            before/in/after the task.
     * @param path ListView will list files included in this path.
     * @param filterType to determine which files will be listed.
     */
    public ListFileTask(FileInfoManager fileInfoManager, OperationEventListener operationEvent,
            String path, int filterType) {
        super(fileInfoManager, operationEvent);
        mPath = path;
        mFilterType = filterType;
        loadMimeTypes();
    }

    @Override
    protected Integer doInBackground(Void... params) {

        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        File[] files = null;
        int total = 0;
        int progress = 0;
        long startLoadTime = System.currentTimeMillis();
        LogUtils.d(this.getClass().getName(), "doInBackground path = " + mPath);
        if (MountPointManager.getInstance().isRootPath(mPath)) {
            List<FileInfo> mountFileList = MountPointManager.getInstance().getMountPointFileInfo();
            if (mountFileList != null) {
                fileInfoList.addAll(mountFileList);
            }
            mFileInfoManager.addItemList(fileInfoList);
            return OperationEventListener.ERROR_CODE_SUCCESS;
        }

        File dir = new File(mPath);
        if (dir.exists()) {
            files = dir.listFiles();
            if (files == null) {
                LogUtils.e(TAG, "files is null");//added by zhangjiaquan for SW00010245 x820 russia to catch log 2013-7-12
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
        } else {
            LogUtils.d(this.getClass().getName(), "doInBackground ERROR_CODE_UNSUCCESS");//added by zhangjiaquan for SW00010245 x820 russia to catch log 2013-7-12
            return OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
        total = files.length;
        long loadTime = 0;
        int nextUpdateTime = FIRST_NEED_PROGRESS;
        LogUtils.d(this.getClass().getName(), "doInBackground total = " + total);
        for (int i = 0; i < files.length; i++) {
            if (isCancelled()) {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }

            if (mFilterType == FileManagerService.FILE_FILTER_TYPE_DEFAULT) {
                if (files[i].getName().startsWith(".")) {
                    continue;
                }
            }

            if (mFilterType == FileManagerService.FILE_FILTER_TYPE_FOLDER) {
                if (!files[i].isDirectory()) {
                    continue;
                }
            }
            
            if (mFilterType == FileManagerService.FILE_FILTER_TYPE_VCARD) {
                if (files[i].getName().startsWith(".")) {
                    continue;
                }
                if (!files[i].isDirectory()) {
                	String type = mMimeTypes.getMimeType(files[i].getName());
                	if(!type.equals("text/x-vCard") && !type.equals("text/x-vcard")){
                		continue;	
                	}
                }
            }
            
            if (mFilterType == FileManagerService.FILE_FILTER_TYPE_VIDEO) {
                if (files[i].getName().startsWith(".")) {
                    continue;
                }
                if (!files[i].isDirectory()) {
                	String type = mMimeTypes.getMimeType(files[i].getName());
                	if(!type.startsWith("video/")){
                		continue;	
                	}
                }
            }

            if (mFilterType == FileManagerService.FILE_FILTER_TYPE_AUDIO) {
                if (files[i].getName().startsWith(".")) {
                    continue;
                }
                if (!files[i].isDirectory()) {
                	String type = mMimeTypes.getMimeType(files[i].getName());
                	if(!type.startsWith("audio/")){
                		continue;	
                	}
                }
            }
            
            mFileInfoManager.addItem(new FileInfo(files[i]));
            loadTime = System.currentTimeMillis() - startLoadTime;
            progress++;

            if (loadTime > nextUpdateTime) {
                startLoadTime = System.currentTimeMillis();
                nextUpdateTime = NEXT_NEED_PROGRESS;
                publishProgress(new ProgressInfo("", progress, total));

            }
        }
        LogUtils.d(this.getClass().getName(), "doInBackground ERROR_CODE_SUCCESS");
        return OperationEventListener.ERROR_CODE_SUCCESS;
    }
    
    private void loadMimeTypes() {
        MimeTypeParser mtp = new MimeTypeParser();
        Resources ress = FileManagerApplication.app.getResources();
        XmlResourceParser in = ress.getXml(R.xml.mimetypes);
        try {
            mMimeTypes = mtp.fromXmlResource(in);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "loadMimeTypes: XmlPullParserException", e);
        } catch (IOException e) {
            Log.e(TAG, "loadMimeTypes: IOException", e);
        }
    }
}
