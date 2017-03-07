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

import android.content.Context;

import com.mediatek.filemanager.FileInfo;
import com.mediatek.filemanager.FileInfoManager;
import com.mediatek.filemanager.MountPointManager;
import com.mediatek.filemanager.service.FileManagerService.OperationEventListener;
import com.mediatek.filemanager.service.MultiMediaStoreHelper.CopyMediaStoreHelper;
import com.mediatek.filemanager.service.MultiMediaStoreHelper.DeleteMediaStoreHelper;
import com.mediatek.filemanager.utils.FileUtils;
import com.mediatek.filemanager.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.net.Uri;
import android.content.Intent;
import android.os.PowerManager;

abstract class FileOperationTask extends BaseAsyncTask {
    protected static final int BUFFER_SIZE = 256 * 1024;
    protected static final int TOTAL = 100;
    private static final String ACTION_MEDIA_SCANNER_SCAN_ALL =
            "com.android.fileexplorer.action.MEDIA_SCANNER_SCAN_ALL";
    private static PowerManager.WakeLock mLock = null;//added by zhangjiaquan for SW00043518 14-4-16

    protected MediaStoreHelper mMediaProviderHelper;

    public FileOperationTask(FileInfoManager fileInfoManager,
            OperationEventListener operationEvent, Context context) {
        super(fileInfoManager, operationEvent);
        if (context == null) {
            throw new IllegalArgumentException();
        } else {
            mMediaProviderHelper = new MediaStoreHelper(context);
        }
    }

    //added by zhangjiaquan for SW00043518 set wack lock when operate files 14-4-16 begin
    public static void setWakelock(Context context) {
        if (null != mLock) {
            mLock.release();
            mLock = null;
        }
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Lock");
        mLock.setReferenceCounted(true);
        mLock.acquire();
    }

    public static void unlock() {
        if (null != mLock) {
            mLock.release();
            mLock = null;
        }
    }
    //added by zhangjiaquan for SW00043518 set wack lock when operate files 14-4-16 end

    protected File getDstFile(HashMap<String, String> pathMap, File file, String defPath) {

        String curPath = pathMap.get(file.getParent());
        if (curPath == null) {
            curPath = defPath;
        }
        File dstFile = new File(curPath, file.getName());

        return checkFileNameAndRename(dstFile);
    }

    protected boolean deleteFile(File file) {
        if (file == null) {
            publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_DELETE_UNSUCCESS,
                    true));
        } else {
            if (file.canWrite() && file.delete()) {
                return true;
            } else {
                publishProgress(new ProgressInfo(
                        OperationEventListener.ERROR_CODE_DELETE_NO_PERMISSION, true));
            }
        }
        return false;
    }

    protected boolean mkdir(HashMap<String, String> pathMap, File srcFile, File dstFile) {
        if (srcFile.exists() && srcFile.canRead() && dstFile.mkdirs()) {
            pathMap.put(srcFile.getAbsolutePath(), dstFile.getAbsolutePath());
            return true;
        } else {
            publishProgress(new ProgressInfo(OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS,
                    true));
            return false;
        }
    }

    private long calcNeedSpace(List<File> fileList) {
        long need = 0;
        for (File file : fileList) {
            need += file.length();
        }
        return need;
    }

    protected boolean isEnoughSpace(List<File> fileList, String dstFolder) {
        long needSpace = calcNeedSpace(fileList);
        File file = new File(dstFolder);
        long freeSpace = file.getFreeSpace();
        if (needSpace > freeSpace) {
            return false;
        }
        return true;
    }

    protected int getAllDeleteFiles(List<FileInfo> fileInfoList, List<File> deleteList) {

        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        for (FileInfo fileInfo : fileInfoList) {
            ret = getAllDeleteFile(fileInfo.getFile(), deleteList);
            if (ret < 0) {
                break;
            }
        }
        return ret;
    }

    protected int getAllDeleteFile(File deleteFile, List<File> deleteList) {
        if (isCancelled()) {
            return OperationEventListener.ERROR_CODE_USER_CANCEL;
        }
        if (deleteFile.isDirectory()) {
            deleteList.add(0, deleteFile);
            if (deleteFile.canWrite()) {
                File[] files = deleteFile.listFiles();
                if (files == null) {
                    return OperationEventListener.ERROR_CODE_UNSUCCESS;
                }
                for (File file : files) {
                    getAllDeleteFile(file, deleteList);
                }
            }
        } else {
            deleteList.add(0, deleteFile);
        }
        return OperationEventListener.ERROR_CODE_SUCCESS;
    }

    protected int getAllFileList(List<FileInfo> srcList, List<File> resultList,
            UpdateInfo updateInfo) {

        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        for (FileInfo fileInfo : srcList) {
            ret = getAllFile(fileInfo.getFile(), resultList, updateInfo);
            if (ret < 0) {
                break;
            }
        }
        return ret;
    }

    protected int getAllFile(File srcFile, List<File> fileList, UpdateInfo updateInfo) {
        if (isCancelled()) {
            return OperationEventListener.ERROR_CODE_USER_CANCEL;
        }
        fileList.add(srcFile);
        updateInfo.updateTotal(srcFile.length());
        if (srcFile.isDirectory() && srcFile.canRead()) {
            File[] files = srcFile.listFiles();
            if (files == null) {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
            for (File file : files) {
                int ret = getAllFile(file, fileList, updateInfo);
                if (ret < 0) {
                    return ret;
                }
            }
        }
        return OperationEventListener.ERROR_CODE_SUCCESS;
    }

    protected int copyFile(byte[] buffer, File srcFile, File dstFile, UpdateInfo updateInfo) {
        FileInputStream in = null;
        FileOutputStream out = null;
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        try {
            if (!dstFile.createNewFile()) {
                return OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }
            if (!srcFile.exists()) {
                return OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);

            int len = 0;
            while ((len = in.read(buffer)) > 0) {
                // Copy data from in stream to out stream
                if (isCancelled()) {
                    LogUtils.d(this.getClass().getName(), "commit copy file cancelled; "
                            + "break while loop " + "thread id: " + Thread.currentThread().getId());
                    if (!dstFile.delete()) {
                        LogUtils.w(this.getClass().getName(), "delete fail in copyFile()");
                    }
                    return OperationEventListener.ERROR_CODE_USER_CANCEL; // break for loop if
                    // cancel is
                }
                out.write(buffer, 0, len);
                updateInfo.updateProgress(len);

                updateProgressWithTime(updateInfo, srcFile);
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
            ret = OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
        } finally {
            try {
                if (in != null) {
                    in.close();
                    LogUtils.d(this.getClass().getName(), " in.close() finish");
                }
                if (out != null) {
                    out.close();
                    LogUtils.d(this.getClass().getName(), " out.close() finish");
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
                ret = OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }
        }
        return ret;
    }

    public File checkFileNameAndRename(File conflictFile) {
        File retFile = conflictFile;
        while (true) {
            if (isCancelled()) {
                return null;
            }
            if (!retFile.exists()) {
                return retFile;
            }
            retFile = FileUtils.genrateNextNewName(retFile);
            if (retFile == null) {
                return null;
            }
        }
    }

    protected void updateProgressWithTime(UpdateInfo updateInfo, File file) {
        if (updateInfo.needUpdate()) {
            int progress = (int) (updateInfo.getProgress() * TOTAL / updateInfo.getTotal());
            publishProgress(new ProgressInfo(file.getName(), progress, TOTAL));
        }
    }

    protected void addItem(HashMap<File, FileInfo> fileInfoMap, File file, File addFile) {
        if (fileInfoMap.containsKey(file)) {
            FileInfo fileInfo = new FileInfo(addFile);
            mFileInfoManager.addItem(fileInfo);
        }
    }

    protected void removeItem(HashMap<File, FileInfo> fileInfoMap, File file, File removeFile) {
        if (fileInfoMap.containsKey(file)) {
            FileInfo fileInfo = new FileInfo(removeFile);
            mFileInfoManager.removeItem(fileInfo);
        }
    }

    static class DeleteFilesTask extends FileOperationTask {
        private final List<FileInfo> mDeletedFilesInfo;
        private Context mContext = null;
        public DeleteFilesTask(FileInfoManager fileInfoManager,
                OperationEventListener operationEvent, Context context, List<FileInfo> fileInfoList) {
            super(fileInfoManager, operationEvent, context);
            mDeletedFilesInfo = fileInfoList;
            mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            List<File> deletefileList = new ArrayList<File>();
            UpdateInfo updateInfo = new UpdateInfo();
            int ret = getAllDeleteFiles(mDeletedFilesInfo, deletefileList);
            if (ret < 0) {
                return ret;
            }
            DeleteMediaStoreHelper deleteMediaStoreHelper = new DeleteMediaStoreHelper(
                    mMediaProviderHelper);
            HashMap<File, FileInfo> deleteFileInfoMap = new HashMap<File, FileInfo>();
            for (FileInfo fileInfo : mDeletedFilesInfo) {
                deleteFileInfoMap.put(fileInfo.getFile(), fileInfo);
            }
            updateInfo.updateTotal(deletefileList.size());

            publishProgress(new ProgressInfo("", (int) updateInfo.getProgress(), updateInfo
                    .getTotal()));
            setWakelock(mContext);//added by zhangjiaquan for SW00043518 14-4-16
            for (File file : deletefileList) {
                if (isCancelled()) {
                    deleteMediaStoreHelper.updateRecords();
                    unlock();//added by zhangjiaquan for SW00043518 14-4-16
                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                if (deleteFile(file)) {
                    deleteMediaStoreHelper.addRecord(file.getAbsolutePath());
                    removeItem(deleteFileInfoMap, file, file);
                }
                updateInfo.updateProgress(1);
                if (updateInfo.needUpdate()) {
                    publishProgress(new ProgressInfo(file.getName(),
                            (int) updateInfo.getProgress(), updateInfo.getTotal()));
                }
            }
            unlock();//added by zhangjiaquan for SW00043518 14-4-16
            deleteMediaStoreHelper.updateRecords();
            return OperationEventListener.ERROR_CODE_SUCCESS;
        }
    }

    static class UpdateInfo {
        protected static final int NEED_UPDATE_TIME = 200;
        private long mStartOperationTime = 0;
        private long mProgressSize = 0;
        private long mTotalSize = 0;

        public UpdateInfo() {
            mStartOperationTime = System.currentTimeMillis();
        }

        public long getProgress() {
            return mProgressSize;
        }

        public long getTotal() {
            return mTotalSize;
        }

        public void updateProgress(long addSize) {
            mProgressSize += addSize;
        }

        public void updateTotal(long addSize) {
            mTotalSize += addSize;
        }

        public boolean needUpdate() {
            long operationTime = System.currentTimeMillis() - mStartOperationTime;
            if (operationTime > NEED_UPDATE_TIME) {
                mStartOperationTime = System.currentTimeMillis();
                return true;
            }
            return false;
        }

    }

    static class CutPasteFilesTask extends FileOperationTask {

        private final List<FileInfo> mSrcList;
        private final String mDstFolder;
        private Context mContext = null;//added by zhangjiaquan for SW00043518 14-4-16
        /** Buffer size for data read and write. */

        public CutPasteFilesTask(FileInfoManager fileInfoManager,
                OperationEventListener operationEvent, Context context, List<FileInfo> src,
                String destFolder) {
            super(fileInfoManager, operationEvent, context);
            mSrcList = src;
            mDstFolder = destFolder;
            mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (mSrcList.isEmpty()) {
                return OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS;
            }
//            if (isSameRoot(mSrcList.get(0).getFileAbsolutePath(), mDstFolder)) {
//                return cutPasteInSomeCard();
//            } else {
                return cutPasteInDiffCard();
//            }
        }
//
//        private boolean isSameRoot(String srcPath, String dstPath) {
//            MountPointManager mpm = MountPointManager.getInstance();
//            String srcMountPoint = mpm.getRealMountPointPath(srcPath);
//            String dstMountPoint = mpm.getRealMountPointPath(dstPath);
//            if (srcMountPoint != null && dstMountPoint != null
//                    && srcMountPoint.equals(dstMountPoint)) {
//                return true;
//            }
//            return false;
//        }
//
//        private Integer cutPasteInSomeCard() {
//
//            UpdateInfo updateInfo = new UpdateInfo();
//            updateInfo.updateTotal(mSrcList.size());
//            publishProgress(new ProgressInfo("", 0, TOTAL));
//
//            for (FileInfo fileInfo : mSrcList) {
//                File newFile = new File(mDstFolder + MountPointManager.SEPARATOR
//                        + fileInfo.getFileName());
//                newFile = checkFileNameAndRename(newFile);
//                if (isCancelled()) {
//                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
//                }
//
//                if (newFile == null) {
//                    publishProgress(new ProgressInfo(
//                            OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
//                    continue;
//                }
//
//                if (fileInfo.getFile().renameTo(newFile)) {
//                    updateInfo.updateProgress(1);
//                    FileInfo newFileInfo = new FileInfo(newFile);
//                    mFileInfoManager.addItem(newFileInfo);
//                    mMediaProviderHelper.updateInMediaStore(newFile.getAbsolutePath(), fileInfo
//                            .getFileAbsolutePath());
//                } else {
//                    publishProgress(new ProgressInfo(
//                            OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
//                }
//                updateProgressWithTime(updateInfo, fileInfo.getFile());
//            }
//            return OperationEventListener.ERROR_CODE_SUCCESS;
//        }

        private Integer cutPasteInDiffCard() {
            int ret = OperationEventListener.ERROR_CODE_SUCCESS;
            List<File> fileList = new ArrayList<File>();
            UpdateInfo updateInfo = new UpdateInfo();
            ret = getAllFileList(mSrcList, fileList, updateInfo);
            if (ret < 0) {
                return ret;
            }
            if (!isEnoughSpace(fileList, mDstFolder)) {
                return OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }

            List<File> romoveFolderFiles = new LinkedList<File>();
            publishProgress(new ProgressInfo("", 0, TOTAL));
            byte[] buffer = new byte[BUFFER_SIZE];
            HashMap<String, String> pathMap = new HashMap<String, String>();
            if (!fileList.isEmpty()) {
                pathMap.put(fileList.get(0).getParent(), mDstFolder);
            }

            CopyMediaStoreHelper copyMediaStoreHelper = new CopyMediaStoreHelper(
                    mMediaProviderHelper);
            DeleteMediaStoreHelper deleteMediaStoreHelper = new DeleteMediaStoreHelper(
                    mMediaProviderHelper);
            HashMap<File, FileInfo> cutFileInfoMap = new HashMap<File, FileInfo>();
            for (FileInfo fileInfo : mSrcList) {
                cutFileInfoMap.put(fileInfo.getFile(), fileInfo);
            }
            setWakelock(mContext);//added by zhangjiaquan for SW00043518 14-4-16
            for (File file : fileList) {
                File dstFile = getDstFile(pathMap, file, mDstFolder);
                if (isCancelled()) {
                    copyMediaStoreHelper.updateRecords();
                    deleteMediaStoreHelper.updateRecords();
                    unlock();//added by zhangjiaquan for SW00043518 14-4-16
                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                if (dstFile == null) {
                    publishProgress(new ProgressInfo(
                            OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                    continue;
                }

                if (file.isDirectory()) {
                    if (mkdir(pathMap, file, dstFile)) {
                        copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                        addItem(cutFileInfoMap, file, dstFile);
                        updateInfo.updateProgress(file.length());
                        romoveFolderFiles.add(0, file);
                        updateProgressWithTime(updateInfo, file);
                    }
                } else {
                    ret = copyFile(buffer, file, dstFile, updateInfo);
                    if (ret == OperationEventListener.ERROR_CODE_USER_CANCEL) {
                        copyMediaStoreHelper.updateRecords();
                        deleteMediaStoreHelper.updateRecords();
                        unlock();//added by zhangjiaquan for SW00043518 14-4-16
                        return ret;
                    } else if (ret < 0) {
                        publishProgress(new ProgressInfo(
                                OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                        updateInfo.updateProgress(file.length());
                    } else {
                        addItem(cutFileInfoMap, file, dstFile);
                        copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                        if (deleteFile(file)) {
                            deleteMediaStoreHelper.addRecord(file.getAbsolutePath());
                        }
                    }
                }
            }

            for (File file : romoveFolderFiles) {
                if (file.delete()) {
                    deleteMediaStoreHelper.addRecord(file.getAbsolutePath());
                }
            }
            unlock();//added by zhangjiaquan for SW00043518 14-4-16
            copyMediaStoreHelper.updateRecords();
            deleteMediaStoreHelper.updateRecords();
            return OperationEventListener.ERROR_CODE_SUCCESS;
        }
    }

    static class CopyPasteFilesTask extends FileOperationTask {

        List<FileInfo> mSrcList = null;
        String mDstFolder = null;
        Context mContext = null;//added by zhangjiaquan for SW00043518 14-4-16
        /** Buffer size for data read and write. */
        public static final int BUFFER_SIZE = 256 * 1024;

        public CopyPasteFilesTask(FileInfoManager fileInfoManager,
                OperationEventListener operationEvent, Context context, List<FileInfo> src,
                String destFolder) {
            super(fileInfoManager, operationEvent, context);
            mSrcList = src;
            mDstFolder = destFolder;
            mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            List<File> fileList = new ArrayList<File>();
            UpdateInfo updateInfo = new UpdateInfo();
            int ret = getAllFileList(mSrcList, fileList, updateInfo);
            if (ret < 0) {
                return ret;
            }

            CopyMediaStoreHelper copyMediaStoreHelper = new CopyMediaStoreHelper(
                    mMediaProviderHelper);
            HashMap<File, FileInfo> copyFileInfoMap = new HashMap<File, FileInfo>();
            for (FileInfo fileInfo : mSrcList) {
                copyFileInfoMap.put(fileInfo.getFile(), fileInfo);
            }

            if (!isEnoughSpace(fileList, mDstFolder)) {
                return OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }

            publishProgress(new ProgressInfo("", 0, TOTAL));

            byte[] buffer = new byte[BUFFER_SIZE];
            HashMap<String, String> pathMap = new HashMap<String, String>();
            if (!fileList.isEmpty()) {
                pathMap.put(fileList.get(0).getParent(), mDstFolder);
            }
            setWakelock(mContext);//added by zhangjiaquan for SW00043518 14-4-16
            for (File file : fileList) {
                File dstFile = getDstFile(pathMap, file, mDstFolder);
                if (isCancelled()) {
                    copyMediaStoreHelper.updateRecords();
                    unlock();//added by zhangjiaquan for SW00043518 14-4-16
                    return OperationEventListener.ERROR_CODE_USER_CANCEL;
                }
                if (dstFile == null) {
                    publishProgress(new ProgressInfo(
                            OperationEventListener.ERROR_CODE_PASTE_UNSUCCESS, true));
                    continue;
                }
                if (file.isDirectory()) {
                    if (mkdir(pathMap, file, dstFile)) {
                        copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                        addItem(copyFileInfoMap, file, dstFile);
                        updateInfo.updateProgress(file.length());
                        updateProgressWithTime(updateInfo, file);
                    }
                } else {
                    if (FileInfo.isDrmFile(file.getName()) || !file.canRead()) {
                        publishProgress(new ProgressInfo(
                                OperationEventListener.ERROR_CODE_COPY_NO_PERMISSION, true));
                        updateInfo.updateProgress(file.length());
                        continue;
                    }
                    ret = copyFile(buffer, file, dstFile, updateInfo);
                    if (ret == OperationEventListener.ERROR_CODE_USER_CANCEL) {
                        copyMediaStoreHelper.updateRecords();
                        unlock();//added by zhangjiaquan for SW00043518 14-4-16
                        return ret;
                    } else if (ret < 0) {
                        publishProgress(new ProgressInfo(ret, true));
                        updateInfo.updateProgress(file.length());
                    } else {
                        copyMediaStoreHelper.addRecord(dstFile.getAbsolutePath());
                        addItem(copyFileInfoMap, file, dstFile);
                    }
                }
            }
            unlock();//added by zhangjiaquan for SW00043518 14-4-16
            copyMediaStoreHelper.updateRecords();
            return OperationEventListener.ERROR_CODE_SUCCESS;
        }
    }

    static class CreateFolderTask extends FileOperationTask {
        public static final String TAG = "CreateFolderTask";
        private final String mDstFolder;
        int mFilterType;
	 Context mContext;

        public CreateFolderTask(FileInfoManager fileInfoManager,
                OperationEventListener operationEvent, Context context, String dstFolder,
                int filterType) {
            super(fileInfoManager, operationEvent, context);
            mDstFolder = dstFolder;
            mFilterType = filterType;
	     mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int ret = OperationEventListener.ERROR_CODE_UNSUCCESS;

            LogUtils.d(TAG, "Create a new folder");
            ret = FileUtils.checkFileName(FileUtils.getFileName(mDstFolder));
            if (ret < 0) {
                return ret;
            }
            LogUtils.d(TAG, "Create a new folder = " + mDstFolder);

            File dir = new File(mDstFolder.trim());
            LogUtils.d(TAG, "The folder to be created exist: " + dir.exists());
            if (dir.exists()) {
                return OperationEventListener.ERROR_CODE_FILE_EXIST;
            }
            File path = new File(FileUtils.getFilePath(mDstFolder));
            if (path.getFreeSpace() <= 0) {
                return OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }
            if (dir.mkdirs()) {
                FileInfo fileInfo = new FileInfo(dir);

                if (!fileInfo.isHideFile()
                        || mFilterType == FileManagerService.FILE_FILTER_TYPE_ALL) {
                    mFileInfoManager.addItem(fileInfo);
                }
                 /*modified by zhangjiaquan for SW00014444 to scan folder for MTP show 2013-10-9 begin*/
                 //mMediaProviderHelper.scanPathforMediaStore("file://"+fileInfo.getFileAbsolutePath());
		   Uri uri = Uri.parse("file://"+fileInfo.getFileAbsolutePath());
	          LogUtils.d(TAG, "zjq scanfolder new folder uri= " + uri);
		   mContext.sendBroadcast(new Intent( ACTION_MEDIA_SCANNER_SCAN_ALL, uri));
                 /*modified by zhangjiaquan for SW00014444 to scan folder for MTP show 2013-10-9 end*/
                return OperationEventListener.ERROR_CODE_SUCCESS;
            } else {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
        }
    }

    static class RenameTask extends FileOperationTask {
        public static final String TAG = "RenameTask";
        private final FileInfo mDstFileInfo;
        private final FileInfo mSrcFileInfo;
        int mFilterType = 0;
	 Context mContext;

        public RenameTask(FileInfoManager fileInfoManager, OperationEventListener operationEvent,
                Context context, FileInfo srcFile, FileInfo dstFile, int filterType) {
            super(fileInfoManager, operationEvent, context);
            mDstFileInfo = dstFile;
            mSrcFileInfo = srcFile;
            mFilterType = filterType;
	     mContext = context;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int ret = OperationEventListener.ERROR_CODE_UNSUCCESS;

            String dstFile = mDstFileInfo.getFileAbsolutePath();
            dstFile = dstFile.trim();
            LogUtils.d(TAG, "rename dstFile = " + dstFile);
            ret = FileUtils.checkFileName(FileUtils.getFileName(dstFile));
            if (ret < 0) {
                return ret;
            }

            File newFile = new File(dstFile);
            File oldFile = new File(mSrcFileInfo.getFileAbsolutePath());

            if (newFile.exists()) {
                return OperationEventListener.ERROR_CODE_FILE_EXIST;
            } else if (oldFile.isFile() && dstFile.endsWith(".")) {
                while (dstFile.endsWith(".")) {
                    dstFile = dstFile.substring(0, dstFile.length() - 1);
                }
                newFile = new File(dstFile);
            }

            if (oldFile.renameTo(newFile)) {
                FileInfo newFileInfo = new FileInfo(newFile);
                mFileInfoManager.removeItem(mSrcFileInfo);
                if (!newFileInfo.isHideFile()
                        || mFilterType == FileManagerService.FILE_FILTER_TYPE_ALL) {
                    mFileInfoManager.addItem(newFileInfo);
                }
                /*modified by zhangjiaquan for SW00014444 to scan folder for MTP show 2013-10-9 begin*/
                //mMediaProviderHelper.updateInMediaStore(newFileInfo.getFileAbsolutePath(),
                //        mSrcFileInfo.getFileAbsolutePath());
                 Uri uri = Uri.parse("file://"+newFileInfo.getFileAbsolutePath());
	          LogUtils.d(TAG, "zjq scanfolder rename uri= " + uri);
		   mContext.sendBroadcast(new Intent( ACTION_MEDIA_SCANNER_SCAN_ALL, uri));
                 /*modified by zhangjiaquan for SW00014444 to scan folder for MTP show 2013-10-9 end*/
                return OperationEventListener.ERROR_CODE_SUCCESS;
            } else {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
        }
    }
}
