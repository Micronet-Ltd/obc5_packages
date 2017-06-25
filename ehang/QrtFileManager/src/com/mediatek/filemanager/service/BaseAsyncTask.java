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

import android.os.AsyncTask;

import com.mediatek.filemanager.FileInfoManager;
import com.mediatek.filemanager.service.FileManagerService.OperationEventListener;
import com.mediatek.filemanager.utils.LogUtils;


abstract class BaseAsyncTask extends AsyncTask<Void, ProgressInfo, Integer> {

    protected OperationEventListener mListener = null;
    protected FileInfoManager mFileInfoManager = null;

    /**
     * Constructor of BaseAsyncTask
     * 
     * @param fileInfoManager a instance of FileInfoManager, which manages information of files in
     *            FileManager.
     * @param listener a instance of OperationEventListener, which is a interface doing things
     *            before/in/after the task.
     */
    public BaseAsyncTask(FileInfoManager fileInfoManager, OperationEventListener listener) {
        if (fileInfoManager == null) {
            throw new IllegalArgumentException();
        }
        mFileInfoManager = fileInfoManager;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null) {
            LogUtils.d(this.getClass().getName(), "onPreExecute");
            mListener.onTaskPrepare();
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        if (mListener != null) {
            LogUtils.d(this.getClass().getName(), "onPostExecute");
            mListener.onTaskResult(result);
            mListener = null;
        }
    }

    @Override
    protected void onCancelled() {
        if (mListener != null) {
            LogUtils.d(this.getClass().getName(), "onCancelled()");
            mListener.onTaskResult(OperationEventListener.ERROR_CODE_USER_CANCEL);
            mListener = null;
        }
    };

    @Override
    protected void onProgressUpdate(ProgressInfo... values) {
        if (mListener != null && values != null && values[0] != null) {
            LogUtils.v(this.getClass().getName(), "onProgressUpdate");
            mListener.onTaskProgress(values[0]);
        }
    }

    /**
     * This method remove listener from task. Set listener associate with task to be null.
     */
    protected void removeListener() {
        if (mListener != null) {
            LogUtils.d(this.getClass().getName(), "removeListener");
            mListener = null;
        }
    }

    /**
     * This method set mListener with certain listener.
     * 
     * @param listener the certain listener, which will be set to be mListener.
     */
    public void setListener(OperationEventListener listener) {
        mListener = listener;
    }

}
