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

import com.mediatek.filemanager.R;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.filemanager.utils.LogUtils;
import com.mediatek.filemanager.utils.MimeTypeParser;
import com.mediatek.filemanager.utils.MimeTypes;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;


public final class MediaStoreHelper {

    private static final String TAG = "MediaStoreHelper";
    private final Context mContext;
	/*add by wanli to resolve SW00033127*/
	private static final String ACTION_MEDIA_SCANNER_SCAN_ALL =
            "com.android.fileexplorer.action.MEDIA_SCANNER_SCAN_ALL";

    /**
     * Constructor of MediaStoreHelper
     * 
     * @param context the Application context
     */
    public MediaStoreHelper(Context context) {
        mContext = context;
    }

    public void updateInMediaStore(String newPath, String oldPath) {
        if (mContext != null && !TextUtils.isEmpty(newPath) && !TextUtils.isEmpty(newPath)) {
            IContentProvider mediaProvider = mContext.getContentResolver().acquireProvider(
                    "media");
            Uri uri = MediaStore.Files.getMtpObjectsUri("external");
            String where = MediaStore.Files.FileColumns.DATA + "=?";
            String[] whereArgs = new String[] { oldPath };

            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DATA, newPath);

            MimeTypes mMimeTypes = null;
            try {
                mMimeTypes = new MimeTypeParser()
                        .fromXmlResource(
                                mContext.getResources().getXml(R.xml.mimetypes));
            } catch (XmlPullParserException e) {
                Log.e(TAG, "loadMimeTypes: XmlPullParserException", e);
            } catch (IOException e) {
                Log.e(TAG, "loadMimeTypes: IOException", e);
            } catch (Exception e) {
                Log.e(TAG, "loadMimeTypes: Exception", e);
            }
            
            //modefy by qrt laiwugang to revert wangwenlong (X820) 2013-04-01
            String mimeType = mMimeTypes.getMimeType((new File(newPath)).getName());
            if(mimeType.startsWith("video/") || mimeType.startsWith("image/")  || mimeType.startsWith("audio/")){
            	values.put(MediaStore.Files.FileColumns.TITLE, (new File(newPath)).getName().substring(0, (new File(newPath)).getName().lastIndexOf(".")));
                values.put(MediaStore.Files.FileColumns.DISPLAY_NAME, (new File(newPath)).getName());
            }
            
            try {
                //modified by zhangjiaquan for new interface in android 4.3 2013-8-17
                mediaProvider.update(null,uri, values, where, whereArgs);
            } catch (RemoteException e) {
                LogUtils.e(TAG, "RemoteException in mediaProvider.update", e);
            }
            
            //modefy by qrt laiwugang to restore wangwenlong (X820) 2013-04-20
            String[] mimeTypes = null;
            File file = new File(newPath);
            if (mMimeTypes != null && file.isFile()) {
                String newFileMimeType = mMimeTypes.getMimeType(file.getName());
                mimeTypes = new String[]{newFileMimeType};
            }

            String[] paths = {newPath};
            MediaScannerConnection.scanFile(mContext, paths, mimeTypes, null);
        }
    }

    /**
     * scan Path for new file or folder in MediaStore
     * 
     * @param path the scan path
     */
    public void scanPathforMediaStore(String path) {
        if (mContext != null && !TextUtils.isEmpty(path)) {
            String[] paths = { path };
			/*begin modify by wanli to resolve SW00033127*/
			Uri uri = Uri.parse("file://"+paths[0]);
			mContext.sendBroadcast(new Intent( ACTION_MEDIA_SCANNER_SCAN_ALL, uri));
            //MediaScannerConnection.scanFile(mContext, paths, null, null);
			/*end modify by wanli to resolve SW00033127*/
        }
    }

   public void scanPathforMediaStore(List<String> scanPaths) {
        if (mContext != null && !scanPaths.isEmpty()) {
            String[] paths = new String[scanPaths.size()];
            scanPaths.toArray(paths);
			/*begin modify by wanli to resolve SW00033127*/
            Uri uri = Uri.parse("file://"+paths[0]);
			mContext.sendBroadcast(new Intent( ACTION_MEDIA_SCANNER_SCAN_ALL, uri));
            //MediaScannerConnection.scanFile(mContext, paths, null, null);
			/*end modify by wanli to resolve SW00033127*/
        }
    }

    /**
     * delete the record in MediaStore
     * 
     * @param paths the delete file or folder in MediaStore
     */
    public void deleteFileInMediaStore(List<String> paths) {
        Uri uri = MediaStore.Files.getContentUri("external");
        // dalete by qrt laiwugang to delete mtk_filemanager uri (X820) 2013-04-07
        Log.d(TAG, "uri is : " + uri);
        //uri = uri.buildUpon().appendQueryParameter("mtk_filemanager", "true").build();
        String where = MediaStore.Files.FileColumns.DATA + "=?";

        if (mContext != null && !paths.isEmpty()) {
            ContentResolver cr = mContext.getContentResolver();
            //String[] whereArgs = new String[paths.size()];
            //paths.toArray(whereArgs);
            //cr.delete(uri, where, whereArgs);
            for (String path : paths) {
                String[] whereArgs = new String[] { path };
                cr.delete(uri, where, whereArgs);
            }
        }
    }

    /**
     * delete the record in MediaStore
     * 
     * @param path the delete file or folder in MediaStore
     */
    public void deleteFileInMediaStore(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Uri uri = MediaStore.Files.getContentUri("external");
        String where = MediaStore.Files.FileColumns.DATA + "=?";
        String[] whereArgs = new String[] { path };
        if (mContext != null) {
            ContentResolver cr = mContext.getContentResolver();
            cr.delete(uri, where, whereArgs);
        }
    }

}