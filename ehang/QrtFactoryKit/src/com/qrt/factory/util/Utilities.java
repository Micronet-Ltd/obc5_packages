/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.qrt.factory.util;

import com.qrt.factory.R;
import com.qrt.factory.TestSettings;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    public static final String TAG = "FactoryKit";

    public static final String RESULT_PASS = "Pass";

    public static final String RESULT_FAIL = "Failed";
/*modified by bwq for 810 add log 2014.10.06 begin*/
    private static final String CURRENT_FILE_NAME = "S_MMI.log";

    public static final String FILE_PATH = File.separator + "persist"
            + File.separator +  CURRENT_FILE_NAME;                           //modiy by bwq for 810 log path change 20141014
/*modified by bwq for 810 add log 2014.10.06 end*/
    public synchronized static void writeCurMessage(String Tag, String result,
            String info) {

        StringBuffer resultBuffer = new StringBuffer(Tag + "**************\n");
        resultBuffer.append("[" + Tag + "] " + result + "\n");
        if (info != null) {
            resultBuffer.append("\n");
            resultBuffer.append(info);
            if (!info.endsWith("\n")) {
                resultBuffer.append("\n");
            }
        }

        FileOutputStream mFileOutputStream = null;
        try {
            mFileOutputStream = new FileOutputStream(getCurrentFile(), true);
            byte[] buffer = resultBuffer.toString().getBytes();
            mFileOutputStream.write(buffer);
            mFileOutputStream.flush();
        } catch (Exception e) {
            loge(TAG, e);
        } finally {
            try {
                if (null != mFileOutputStream) {
                    mFileOutputStream.close();
                }
                mFileOutputStream = null;
            } catch (IOException e) {
                mFileOutputStream = null;
                loge(TAG, e);
            }
        }
        logd(TAG, "Writed result= [" + Tag + "] : " + result);
    }

    public static File getCurrentFile() throws IOException {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static void createNewCurrentFile() {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
        if (file.exists()) {
            file.delete();
        }
    }

    public static void logd(String tag, Object d) {

        if (d == null || !TestSettings.LOG) {
            return;
        }
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        d = "[" + mMethodName + "] " + d;
        Log.d(tag, d + "");
    }

    public static void loge(String tag, Object e) {

        if (e == null || !TestSettings.LOG) {
            return;
        }
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(tag, e + "");
    }

    public static String getFileInfo(String path) {

        File mFile;
        FileReader mFileReader = null;
        if(path==null)
        {
        	return null;
        }
        mFile = new File(path);

        try {
            mFileReader = new FileReader(mFile);
            char data[] = new char[128];
            int charCount;
            String status[];
            try {
                charCount = mFileReader.read(data);
                status = new String(data, 0, charCount).trim().split("\n");
                loge(TAG, "*** status[] = "+ charCount + "  &&& status =  "+ status);
                return status[0];
            } catch (IOException e) {
                loge(TAG, e);
            }
        } catch (FileNotFoundException e) {
            loge(TAG, e);
        } finally {
            if (mFileReader != null) {
                try {
                    mFileReader.close();
                } catch (IOException e) {

                }
            }
            mFileReader = null;
        }
        return null;
    }

    public static String getSensorInfoByCode(Context context,
            String lightSensorCode) {
        if ("0029".equals(lightSensorCode)) {
            return "TMD277113";
        } else if ("0005".equals(lightSensorCode)) {
            return "LTR558";
        }
        return context.getString(R.string.unknown);
    }

    public static List loadXmlForClass(Context context, int xmlResourceId,
            Class ItemClass)
            throws XmlPullParserException, IllegalAccessException,
            InstantiationException, IOException, InvocationTargetException {
        List itemList = new ArrayList();
        XmlPullParser mXmlPullParser = context.getResources()
                .getXml(xmlResourceId);

        int mEventType = mXmlPullParser.getEventType();

        while (mEventType != XmlPullParser.END_DOCUMENT) {
            Object item = null;
            if (mEventType == XmlPullParser.START_TAG) {

                String tagName = mXmlPullParser.getName();

                if (tagName.equals(ItemClass.getSimpleName())) {

                    item = ItemClass.newInstance();

                    for (Field field : ItemClass.getDeclaredFields()) {
                        String fieldName = field.getName();
                        String arg = mXmlPullParser
                                .getAttributeValue(null, fieldName);
//                        field.set(item, arg);
                        Method method = null;
                        try {
                            method = ItemClass
                                    .getMethod("set" + upperFirst(fieldName),
                                            String.class);
                        } catch (NoSuchMethodException e) {
                            continue;
                        }
                        method.invoke(item, arg);
                    }

                    if (item != null) {
                        itemList.add(item);
                    }
                }
            }
            mEventType = mXmlPullParser.next();
        }
        return itemList;
    }

    public static String upperFirst(String str) {
        String first = str.substring(0, 1);
        String last = str.substring(1);
        return first.toUpperCase() + last;
    }

    public static synchronized String synchronizedNV()
            throws IOException {
        FileInputStream inputStream = new FileInputStream(
                "/sys/devices/platform/rs300000a7.65536/qrt_force_sync");
        inputStream.read();
        inputStream.close();
        return "";
    }

    public static synchronized void exec(final List<String> cmdList) {

        try {
            logd(TAG, "exec cmdList = " + cmdList);             //add by bwq for close mic test cmd 201400905
            Process mProcess = Runtime.getRuntime().exec("sh");
            DataOutputStream localDataOutputStream = new DataOutputStream(mProcess.getOutputStream());

            for (String cmd : cmdList) {
                localDataOutputStream.writeBytes(cmd + "\n");
                localDataOutputStream.flush();
            }
            localDataOutputStream.writeBytes("exit\n");
            localDataOutputStream.flush();

            InputStream errorStream = mProcess.getErrorStream();
            InputStreamReader errorInReader = new InputStreamReader(errorStream);
            BufferedReader errorInBuffer = new BufferedReader(errorInReader);
            String errorStr;
            String errorData = "Shell Error : ";
            while ((errorStr = errorInBuffer.readLine()) != null) {
                errorData += errorStr + "\n";
            }
            loge(TAG, errorData);

            InputStream inStream = mProcess.getInputStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader inBuffer = new BufferedReader(inReader);
            String s;
            String data = "Shell out : ";
            while ((s = inBuffer.readLine()) != null) {
                data += s + "\n";
            }
            logd(TAG, data);

            int waitFor = mProcess.waitFor();
            logd(TAG, "waitFor = " + waitFor);

            int result = mProcess.exitValue();
            logd(TAG, "result = " + result);

            localDataOutputStream.close();

            errorInBuffer.close();
            errorInReader.close();
            errorStream.close();

            inBuffer.close();
            inReader.close();
            inStream.close();
        } catch (Exception e) {
            logd(TAG, e);
        }
    }

    public static boolean writeToFile(String path, String value) {
        if (value == null || value.length() == 0) {
            return false;
        }

        if (path == null || path.length() == 0) {
            return false;
        }

        Log.d(TAG, path + "****writeToFile begin****"+value);      //Modify by bwq for Gadjust slowly 20140912

        FileOutputStream outputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file, false);
            byte[] bytes = value.getBytes();
            outputStream.write(bytes, 0, bytes.length);
            outputStream.flush();

            outputStream.getFD().sync();
            outputStream.close();
            Log.d(TAG, path + " write success");
            return true;
        } catch (IOException e) {
            Log.w(TAG, "writeToFile error : ", e);
        } catch (Exception ex) {
            Log.w(TAG, "writeToFile error : ", ex);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    outputStream = null;
                }
            }
        }
        return false;
    }
}


