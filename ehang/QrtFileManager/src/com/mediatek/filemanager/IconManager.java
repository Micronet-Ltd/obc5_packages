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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.drm.DrmUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.Log;

import com.mediatek.filemanager.ext.DefaultIconExtension;
import com.mediatek.filemanager.ext.IIconExtension;
import com.mediatek.filemanager.service.FileManagerService;
import com.mediatek.filemanager.utils.DrmManager;
import com.mediatek.filemanager.utils.LogUtils;
import com.mediatek.filemanager.utils.MimeTypeParser;
import com.mediatek.filemanager.utils.MimeTypes;
//import com.mediatek.pluginmanager.Plugin;
//import com.mediatek.pluginmanager.PluginManager;

import java.io.IOException;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

public final class IconManager {
    public static final String TAG = "IconManager";

    private static IconManager sInstance = new IconManager();
    /** the sdcard2 head image */
    // -------------- Bitmaps Cache for creating files ICON ---------------------

    private static final int OFFX = 4;
    /** Cache the default icons */
    protected HashMap<Integer, Bitmap> mDefIcons = null;
    /** Cache the sdcard2 icons, <all the icons has a sdcard2 head> */
    protected HashMap<Integer, Bitmap> mSdcard2Icons = null;
    private Resources mRes;
    protected Bitmap mIconsHead = null;

    private IIconExtension mExt = null;

    private IconManager() {

    }

    /**
     * This method gets instance of IconManager
     * 
     * @return instance of IconManager
     */
    public static IconManager getInstance() {
        return sInstance;
    }

    /**
     * This method gets the drawable id based on the mimetype
     * 
     * @param mimeType the mimeType of a file/folder
     * @return the drawable icon id based on the mimetype
     */
    public static int getDrawableId(String mimeType) {
    	//modefy by qrt laiwugang to add file drawable (X820H) 2013-05-16
    	String mProjet = android.os.SystemProperties.get("ro.config.custom");
        if (TextUtils.isEmpty(mimeType)) {
            return R.drawable.fm_unknown;
        } else if (mimeType.startsWith("application/vnd.android.package-archive")) {
            // TODO change "application/vnd.android.package-archive" to static final string
            return R.drawable.fm_apk;
        } else if (mimeType.startsWith("application/zip")) {
        	if(mProjet.endsWith("x820h")){
        		return R.drawable.ic_app_zip;	
        	}else{
        		return R.drawable.fm_zip;	
        	}
        } else if (mimeType.startsWith("application/ogg")) {
            return R.drawable.fm_audio;
        } else if (mimeType.startsWith("audio/")) {
		/*add by baiwuqiang to wma type show SW00060815 begin*/
			if(mimeType.endsWith("wma")){
			return R.drawable.fm_unknown;
			}
		/*add by baiwuqiang to wma type show SW00060815 end*/
            return R.drawable.fm_audio;
        } else if (mimeType.startsWith("image/")) {
            return R.drawable.fm_picture;
        } else if (mimeType.startsWith("text/")) {
        	if(mProjet.endsWith("x820h")){
        		return R.drawable.ic_app_txt;
        	}else{
        		return R.drawable.fm_doc;
        	}
        } else if (mimeType.startsWith("video/")) {
            return R.drawable.fm_video;
        } else if((mimeType.startsWith("application/msword") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) && mProjet.endsWith("x820h")){
        	return R.drawable.ic_app_doc;
        } else if((mimeType.startsWith("application/vnd.ms-excel") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) && mProjet.endsWith("x820h")){
        	return R.drawable.ic_app_xls;
        } else if((mimeType.startsWith("application/vnd.ms-powerpoint") || mimeType.startsWith("application/vnd.openxmlformats-officedocument.presentationml.presentation")) && mProjet.endsWith("x820h")){
        	return R.drawable.ic_app_ppt;
        } else if(mimeType.startsWith("application/pdf") && mProjet.endsWith("x820h")){
        	return R.drawable.ic_app_pdf;
        } else if(mimeType.startsWith("application/rar") && mProjet.endsWith("x820h")){
        	return R.drawable.ic_app_rar;
        } else if(mimeType.startsWith("application/zip") && mProjet.endsWith("x820h")){
        	return R.drawable.ic_app_zip;
        } else if(mimeType.startsWith("application/iom")){
        	return R.drawable.fm_lock;
        }else {
            return R.drawable.fm_unknown;
        }
    }

    /**
     * This method gets icon from resources according to file's information.
     * 
     * @param res Resources to use
     * @param fileInfo information of file
     * @param service FileManagerService, which will provide function to get file's Mimetype
     * @return bitmap(icon), which responds the file
     */
    public Bitmap getIcon(Resources res, FileInfo fileInfo,
            FileManagerService service) {
        Bitmap icon = null;
        
        MimeTypeParser mtp = new MimeTypeParser();   
        XmlResourceParser in = res.getXml(R.xml.mimetypes); 
        MimeTypes mMimeTypes = null;
        try {
            mMimeTypes = mtp.fromXmlResource(in);
        } catch (XmlPullParserException e) {
            Log.e(TAG, "loadMimeTypes: XmlPullParserException", e);
        } catch (IOException e) {
            Log.e(TAG, "loadMimeTypes: IOException", e);
        }
        
        boolean isExternal = MountPointManager.getInstance().isExternalFile(
                fileInfo);
        if (fileInfo.isDirectory()) {
            icon = getFolderIcon(fileInfo, isExternal);
        } else {
            //String mimeType = fileInfo.getFileMimeType(service);
        	String mimeType = mMimeTypes.getMimeType(fileInfo.getFile().getName());

            int iconId = getDrawableId(mimeType);

            if (fileInfo.isDrmFile()) {

                int actionId = -1;//DrmUtils.getAction(mimeType);
                LogUtils.d(TAG, "setIcon isDrmFile & actionId=" + actionId);
                if (actionId != DrmManager.ACTIONID_NOT_DRM) {
                    // try to get the DRM file icon.
                    icon = DrmManager.getInstance().overlayDrmIconSkew(res,
                            fileInfo.getFileAbsolutePath(), actionId, iconId);
                    if (icon != null && isExternal) {
                        icon = createExternalIcon(icon);
                    }
                }
            }
            if (icon == null) {
                icon = getFileIcon(iconId, isExternal);
            }
        }
        //LogUtils.v(TAG, "setIcon isSdcard2File=" + isExternal + " icon=" + icon);
        return icon;
    }
    
    private Bitmap getFileIcon(int iconId, boolean isExternal) {
        if (isExternal) {
            return getExternalIcon(iconId);
        } else {
            return getDefaultIcon(iconId);
        }
    }

    private Bitmap getFolderIcon(FileInfo fileInfo, boolean isExternal) {
        String path = fileInfo.getFileAbsolutePath();
        if (MountPointManager.getInstance().isInternalMountPath(path)) {
            return getDefaultIcon(R.drawable.phone_storage);
        } else if (MountPointManager.getInstance().isExternalMountPath(path)) {
            return getDefaultIcon(R.drawable.sdcard);
        } else if (mExt != null && mExt.isSystemFolder(path)) {
            Bitmap icon = mExt.getSystemFolderIcon(path);
            if (icon != null) {
                if (isExternal) {
                    return createExternalIcon(icon);
                } else {
                    return icon;
                }
            }
        }
        return getFileIcon(R.drawable.fm_folder, isExternal);
    }

    /**
     * This method initializes variable mExt of IIconExtension type, and create system folder.
     * 
     * @param context Context to use
     * @param path create system folder under this path
     */
    public void init(Context context, String path) {
        mRes = context.getResources();
        /**
        try {
            mExt = (IIconExtension) PluginManager.createPluginObject(context,
                    IIconExtension.class.getName());
        } catch (Plugin.ObjectCreationException e) {
            mExt = new DefaultIconExtension();
        }*/
        mExt = new DefaultIconExtension();
        mExt.createSystemFolder(path);
    }

    /**
     * This method checks weather certain file is system folder.
     * 
     * @param fileInfo certain file to be checked
     * @return true for system folder, and false for not system folder
     */
    public boolean isSystemFolder(FileInfo fileInfo) {
        if (fileInfo == null || mExt == null) {
            return false;
        }
        return mExt.isSystemFolder(fileInfo.getFileAbsolutePath());
    }

    /**
     * Get the sdcard2 icon . icon.
     * 
     * @param resId resource ID for external icon
     * @return external icon for certain item
     */
    public Bitmap getExternalIcon(int resId) {
        Bitmap icon = null;
        if (mSdcard2Icons == null) {
            mSdcard2Icons = new HashMap<Integer, Bitmap>();
        }
        if (mSdcard2Icons.containsKey(resId)) {
            icon = mSdcard2Icons.get(resId);
        } else {
            icon = createExternalIcon(getDefaultIcon(resId));
            mSdcard2Icons.put(resId, icon);
        }
        return icon;
    }

    /**
     * Merge the {@link mIconsHead} with bitmap together to get the SDCard2 icon.
     * 
     * @param bitmap base icon for external icon
     * @return created external icon
     */
    public Bitmap createExternalIcon(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("parameter bitmap is null");
        }
        if (mIconsHead == null) {
            mIconsHead = BitmapFactory.decodeResource(mRes,
                    R.drawable.fm_sdcard2_header);
        }
        int offx = mIconsHead.getWidth() / OFFX;
        int width = offx + bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap icon = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas c = new Canvas(icon);
        c.drawBitmap(bitmap, offx, 0, null);
        //c.drawBitmap(mIconsHead, 0, 0, null);
        return icon;
    }

    /**
     * Get the default bitmap and cache it in memory.
     * 
     * @param resId resource ID for default icon
     * @return default icon
     */
    public Bitmap getDefaultIcon(int resId) {
        Bitmap icon = null;
        if (mDefIcons == null) {
            mDefIcons = new HashMap<Integer, Bitmap>();
        }
        if (mDefIcons.containsKey(resId)) {
            icon = mDefIcons.get(resId);
        } else {
            icon = BitmapFactory.decodeResource(mRes, resId);
            if (icon == null) {
                throw new IllegalArgumentException(
                        "decodeResource()fail, or invalid resId");
            }
            mDefIcons.put(resId, icon);
        }
        return icon;
    }
}
