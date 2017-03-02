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

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.mediatek.filemanager.FileInfo;
import com.mediatek.filemanager.FileInfoManager;
import com.mediatek.filemanager.service.FileManagerService.OperationEventListener;
import com.mediatek.filemanager.utils.LogUtils;


class SearchTask extends BaseAsyncTask {

    private final String mSearchName;
    private final String mPath;
    private final ContentResolver mContentResolver;
    
    private boolean isShowHidenFile;//QRT add by yanlei 20131214  for do not search the hidden file when set don't show hidden file

    /**
     * Constructor for SearchTask
     * 
     * @param fileInfoManager a instance of FileInfoManager, which manages information of files in
     *            FileManager.
     * @param operationEvent a instance of OperationEventListener, which is a interface doing things
     *            before/in/after the task.
     * @param searchName the String, which need search
     * @param path the limitation, which limit the search just in the file represented by the path
     * @param contentResolver the contentResolver for query(search).
     */
    /*QRT Begin modified by yanlei 20131214  do not search the hidden file when set don't show hidden file*/
    public SearchTask(FileInfoManager fileInfoManager, OperationEventListener operationEvent,
            String searchName, String path, ContentResolver contentResolver,boolean isShowHidenFile) {
        super(fileInfoManager, operationEvent);
        mContentResolver = contentResolver;
        mPath = path;
        mSearchName = searchName;
        this.isShowHidenFile = isShowHidenFile;
    }
    /*QRT End modified by yanlei 20131214  do not search the hidden file when set don't show hidden file*/

    @Override
    protected Integer doInBackground(Void... params) {
        Uri uri = MediaStore.Files.getContentUri("external");

        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        String[] projection = { MediaStore.Files.FileColumns.DATA, };
        StringBuilder sb = new StringBuilder();

        sb.append(MediaStore.Files.FileColumns.DATA + " like ");//modify by wenjs for search file from Column _data
        DatabaseUtils.appendEscapedSQLString(sb, "%" + mSearchName + "%");
        sb.append(" and ").append(MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%" + mPath + "%");
        
        /*QRT Begin add by yanlei 20131216  do not search the hidden file when set don't show hidden file*/
        //if (!isShowHidenFile){
	       // sb.append(" and ").append(MediaStore.Files.FileColumns.TITLE + " not like ");
	        //DatabaseUtils.appendEscapedSQLString(sb, ".%");
       // }
        /*QRT End add by yanlei 20131216  do not search the hidden file when set don't show hidden file*/
        
        String selection = sb.toString();
        Cursor cursor = null;
        try{
          cursor = mContentResolver.query(uri, projection, selection, null, null);
        }catch(Exception e){
           LogUtils.d(this.getClass().getName(), e.getMessage());
        }
        LogUtils.d(this.getClass().getName(), "projection = " + projection[0]);
        LogUtils.d(this.getClass().getName(), "selection = " + selection);
        if (cursor == null) {
            return OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
		//add by wenjs for count search result begin
        long total = 0;// cursor.getCount();//modify by wenjs for count search result
        cursor.moveToFirst();
		try {
            while (!cursor.isAfterLast()) {
                if (isCancelled()) {
                    ret = OperationEventListener.ERROR_CODE_USER_CANCEL;
                    break;
                }
                String name = (String) cursor.getString(cursor
                        .getColumnIndex(MediaStore.Files.FileColumns.DATA));
				
				int findIndex = name.lastIndexOf("/");
				
				if(findIndex != -1){
					if (!isShowHidenFile){
						if(name.indexOf("/.", findIndex) != -1){
							cursor.moveToNext();
							continue;
						}
					}
					if((name.toUpperCase()).indexOf(mSearchName.toUpperCase(), findIndex) != -1){
						total++;						
					}
				}
                cursor.moveToNext();
                
            }

			publishProgress(new ProgressInfo("", 0, total));
	        int progress = 0;
	        cursor.moveToFirst();
	        try {
	            while (!cursor.isAfterLast()) {
	                if (isCancelled()) {
	                    ret = OperationEventListener.ERROR_CODE_USER_CANCEL;
	                    break;
	                }
	                String name = (String) cursor.getString(cursor
	                        .getColumnIndex(MediaStore.Files.FileColumns.DATA));
	//                 mFileInfoManager.addItem(new FileInfo(name));

					//modify by wenjs for search file from Column _data begin
					int findIndex = name.lastIndexOf("/");
					
					if(findIndex != -1){
						if (!isShowHidenFile){
							if(name.indexOf("/.", findIndex) != -1){
								cursor.moveToNext();
								continue;
							}
						}
						if((name.toUpperCase()).indexOf(mSearchName.toUpperCase(), findIndex) != -1){								
							publishProgress(new ProgressInfo(new FileInfo(name), progress++, total));						
						}
					}
					//modify by wenjs for search file from Column _data end
	                cursor.moveToNext();
	                
	            }
	        } finally {
	            cursor.close();				
	        }
			
        } finally {
            cursor.close();
        }
		//add by wenjs for count search result end
        
        return ret;
    }

}
