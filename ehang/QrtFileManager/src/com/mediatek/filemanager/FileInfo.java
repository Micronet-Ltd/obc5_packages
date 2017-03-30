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

import android.media.MediaFile;
import android.net.Uri;
import android.text.TextUtils;

import com.mediatek.filemanager.service.FileManagerService;
import com.mediatek.filemanager.utils.DrmManager;
import com.mediatek.filemanager.utils.FileUtils;
import com.mediatek.filemanager.utils.LogUtils;
import com.mediatek.filemanager.utils.OptionsUtils;

import java.io.File;


public class FileInfo {
    private static final String TAG = "FileInfo";

    public static final String MIMETYPE_EXTENSION_NULL = "unknown_ext_null_mimeType";
    public static final String MIMETYPE_EXTENSION_UNKONW = "unknown_ext_mimeType";
    public static final String MIMETYPE_3GPP_VIDEO = "video/3gpp";
    public static final String MIMETYPE_3GPP2_VIDEO = "video/3gpp2";
    public static final String MIMETYPE_3GPP_UNKONW = "unknown_3gpp_mimeType";
    public static final String MIMETYPE_UNRECOGNIZED = "application/zip";

    public static final String MIME_HAED_IMAGE = "image/";
    public static final String MIME_HEAD_VIDEO = "video/";

	public static final String ENCRYPT_EXTENSION = "iom";
	

    /** File name's max length */
    public static final int FILENAME_MAX_LENGTH = 255;

    private final File mFile;
    private String mParentPath = null;
    private String mMimeType = null;
    private String mName = null;
    private final String mAbsolutePath;
    private String mFileSizeStr = null;
    private final boolean mIsDir;
    private long mLastModifiedTime = -1;
    private long mSize = -1;

    /** Used in FileInfoAdapter to indicate whether the file is selected */
    private boolean mIsChecked = false;

    /**
     * Constructor of FileInfo, which restore details of a file.
     * 
     * @param file the file associate with the instance of FileInfo.
     * @throws IllegalArgumentException when the parameter file is null, will throw the Exception.
     */
    public FileInfo(File file) throws IllegalArgumentException {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        mFile = file;
        mAbsolutePath = mFile.getAbsolutePath();
        mLastModifiedTime = mFile.lastModified();
        mIsDir = mFile.isDirectory();
        /*Modified by yanlei to get the folder size 2014-1-14 begin*/
        if (!mIsDir) {
            mSize = mFile.length();
        }
		/*begin delete by wanli to resolve [SW00039283]
		else {
        	mSize = getFolderSize(mFile);
		}
		end delete by wanli to resolve [SW00039283]*/
        /*Modified by yanlei to get the folder size 2014-1-14 end*/
    }

    /**
     * Constructor of FileInfo, which restore details of a file.
     * 
     * @param absPath the absolute path of a file which associated with the instance of FileInfo.
     */
    public FileInfo(String absPath) {
        if (absPath == null) {
            throw new IllegalArgumentException();
        }
        mAbsolutePath = absPath;
        mFile = new File(absPath);
        mLastModifiedTime = mFile.lastModified();
        mIsDir = mFile.isDirectory();
        /*Modified by yanlei to get the folder size 2014-1-14 begin*/
        if (!mIsDir) {
            mSize = mFile.length();
        }
		/*begin delete by wanli to resolve [SW00039283]
		else {
        	mSize = getFolderSize(mFile);
		}
		end delete by wanli to resolve [SW00039283]*/
        /*Modified by yanlei to get the folder size 2014-1-14 end*/
    }

    /**
     * This method gets a file's parent path
     * 
     * @return file's parent path.
     */
    public String getFileParentPath() {
        if (mParentPath == null) {
            mParentPath = FileUtils.getFilePath(mAbsolutePath);
            //LogUtils.d(TAG, "getFileParentPath = null" + mParentPath);
        }
        return mParentPath;
    }

    /**
     * This method gets a file's parent path's description, which will be shown on the
     * NavigationBar.
     * 
     * @return the path's parent path's description path.
     */
    public String getShowParentPath() {
        return MountPointManager.getInstance().getDescriptionPath(getFileParentPath());
    }

    /**
     * This method gets a file's description path, which will be shown on the NavigationBar.
     * 
     * @return the path's description path.
     */
    public String getShowPath() {
        return MountPointManager.getInstance().getDescriptionPath(getFileAbsolutePath());
    }

    /**
     * This method gets a file's real name.
     * 
     * @return file's name on FileSystem.
     */
    public String getFileName() {
        if (mName == null) {
            mName = FileUtils.getFileName(mAbsolutePath);
            LogUtils.d(TAG, "getFileName:" + mName);
        }
        return mName;
    }

    /**
     * This method gets the file's description name.
     * 
     * @return file's description name for show.
     */
    public String getShowName() {
        return FileUtils.getFileName(getShowPath());
    }

    /**
     * This method gets the file's size(including its contains).
     * 
     * @return file's size in long format.
     */
    public long getFileSize() {
        return mSize;
    }

    /**
     * This method gets transform the file's size from long to String.
     * 
     * @return file's size in String format.
     */
    public String getFileSizeStr() {
        if (mFileSizeStr == null) {
            mFileSizeStr = FileUtils.sizeToString(mSize);
        }
        return mFileSizeStr;
    }

    /**
     * This method check the file is directory, or not.
     * 
     * @return true for directory, false for not directory.
     */
    public boolean isDirectory() {
        return mIsDir;
    }

    /**
     * This method get the file's MIME type.
     * 
     * @param service the FileManager Service for update the 3gpp File
     * @return the file's MIME type.
     */
    public String getFileMimeType(FileManagerService service) {
        if (TextUtils.isEmpty(mMimeType) && !isDirectory()) {
            mMimeType = FileInfo.MIMETYPE_EXTENSION_UNKONW;
            if (isDrmFile()) {
                mMimeType = DrmManager.getInstance()
                        .getOriginalMimeType(this.getFileAbsolutePath());
            } else {
                mMimeType = getMimeType(mFile);
            }
            LogUtils.d(TAG, "Get mimeType for file: " + mMimeType);
        }
        if (mMimeType == FileInfo.MIMETYPE_3GPP_UNKONW) {
            mMimeType = service.update3gppMimetype(this);
        }
        return mMimeType;
    }

    /**
     * The method check the file is DRM file, or not.
     * 
     * @return true for DRM file, false for not DRM file.
     */
    public boolean isDrmFile() {
        if (mIsDir) {
            return false;
        }
        return isDrmFile(mAbsolutePath);
    }

    /**
     * This static method check a file is DRM file, or not.
     * 
     * @param fileName the file which need to be checked.
     * @return true for DRM file, false for not DRM file.
     */
    public static boolean isDrmFile(String fileName) {
        if (OptionsUtils.isMtkDrmApp()) {
            String extension = FileUtils.getFileExtension(fileName);
            if (extension != null && extension.equalsIgnoreCase(DrmManager.EXT_DRM_CONTENT)) {
                return true; // all drm files cannot be copied
            }
        }
        return false;
    }

	public boolean isEncryptFile() {
        if (mIsDir) {
            return false;
        }
        return isEncryptFile(mAbsolutePath);
    }

    public static boolean isEncryptFile(String fileName) {
        String extension = FileUtils.getFileExtension(fileName);
        if (extension != null && extension.equalsIgnoreCase(ENCRYPT_EXTENSION)) {
            return true;
        }
        return false;
    }

    /**
     * This method gets the MIME type based on the extension of a file
     * 
     * @param file the target file
     * @return the MIME type of the file
     */
    private String getMimeType(File file) {
        String fileName = file.getName();
        String extension = FileUtils.getFileExtension(fileName);
        LogUtils.d(TAG, "getMimeTypeForFile fileName=" + fileName);

        if (extension == null) {
            return FileInfo.MIMETYPE_EXTENSION_NULL;
        }

        String mimeType = MediaFile.getMimeTypeForFile(fileName);
        if (mimeType == null) {
            return FileInfo.MIMETYPE_EXTENSION_UNKONW;
        }

        // special solution for checking 3gpp original mimetype
        // 3gpp extension could be video/3gpp or audio/3gpp
        if (mimeType.equalsIgnoreCase(FileInfo.MIMETYPE_3GPP_VIDEO)
                || mimeType.equalsIgnoreCase(FileInfo.MIMETYPE_3GPP2_VIDEO)) {
            LogUtils.d(TAG, "getMimeTypeForFile, a 3gpp or 3g2 file");
            return FileInfo.MIMETYPE_3GPP_UNKONW;
        }
        return mimeType;
    }

    /**
     * This method sets the MIME type of a file.
     * 
     * @param mimeType MIME type which will be set
     */
    public void setFileMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    /**
     * This method gets last modified time of the file.
     * 
     * @return last modified time of the file.
     */
    public long getFileLastModifiedTime() {
        return mLastModifiedTime;
    }

    /**
     * This method update mLastModifiedTime(the file's last modified time).
     * 
     * @return updated mLastModifiedTime(the file's last modified time).
     */
    public long getNewModifiedTime() {
        mLastModifiedTime = mFile.lastModified();
        return mLastModifiedTime;
    }

    /**
     * This method gets the file's absolute path.
     * 
     * @return the file's absolute path.
     */
    public String getFileAbsolutePath() {
        return mAbsolutePath;
    }

    /**
     * This method gets the file packaged in FileInfo.
     * 
     * @return the file packaged in FileInfo.
     */
    public File getFile() {
        return mFile;
    }

    /**
     * This method gets the file packaged in FileInfo.
     * 
     * @return the file packaged in FileInfo.
     */
    public Uri getUri() {
        return Uri.fromFile(mFile);
    }

    @Override
    public int hashCode() {
        return getFileAbsolutePath().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return true;
        } else {
            if (o instanceof FileInfo) {
                if (((FileInfo) o).getFileAbsolutePath().equals(this.getFileAbsolutePath())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * This method checks that the file is selected, or not.
     * 
     * @return true for selected, false for not selected.
     */
    public boolean isChecked() {
        return mIsChecked;
    }

    /**
     * This method sets variable mIsChecked, which present the file is selected or not.
     * 
     * @param checked the checked flag for the file.
     */
    public void setChecked(boolean checked) {
        mIsChecked = checked;
    }

    /**
     * This method checks that weather the file is hide file, or not.
     * 
     * @return true for hide file, and false for not hide file
     */
    public boolean isHideFile() {
        if (getFileName().startsWith(".")) {
            return true;
        }
        return false;
    }

    /**
     * @name:Qrt_get_folder_size
     * @author: yanlei
     * @date:2014-1-14
     * @param: file
     * @return: long 
     */
    private long getFolderSize(File file){
  	  long size = 0;
  	  File flist[] = file.listFiles();
  	  for (int i = 0; i < flist.length; i++){
		  if (flist[i].isDirectory()){
			  size = size + getFolderSize(flist[i]);
		  }else{
			  size = size + flist[i].length();
		  }
  	  }
  	  return size;
  }

}
