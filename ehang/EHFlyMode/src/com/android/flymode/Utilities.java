/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by Ehang Engineering team.
 */
package com.android.flymode;

import android.content.Context;

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
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;  

import org.apache.http.util.EncodingUtils;

import android.util.Log;

public class Utilities {

    public static final String TAG = "PrivacyModeService";

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
                return status[0];
            } catch (IOException e) {
                Log.d(TAG,"error:" + e);
            }
        } catch (FileNotFoundException e) {
        	 Log.d(TAG,"error:" + e);
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

    public static String readFile(String path) {
		String data;
		try{   
			 FileInputStream fin = new FileInputStream(path);   
			
			 int length = fin.available();	
			
			 byte [] buffer = new byte[length];	 
			 fin.read(buffer);		
			
			 data = EncodingUtils.getString(buffer, "UTF-8");   
			
			 fin.close();  

		 }	
		 catch(Exception e){   
			  e.printStackTrace();	 
			  return null;
		 }	

        return data;
    }


    public static synchronized void exec(final List<String> cmdList) {

        try {
            Log.d(TAG, "exec cmdList = " + cmdList);    
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
            Log.d(TAG, errorData);

            InputStream inStream = mProcess.getInputStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader inBuffer = new BufferedReader(inReader);
            String s;
            String data = "Shell out : ";
            while ((s = inBuffer.readLine()) != null) {
                data += s + "\n";
            }
            Log.d(TAG, data);

            int waitFor = mProcess.waitFor();
            Log.d(TAG, "waitFor = " + waitFor);

            int result = mProcess.exitValue();
            Log.d(TAG, "result = " + result);

            localDataOutputStream.close();

            errorInBuffer.close();
            errorInReader.close();
            errorStream.close();

            inBuffer.close();
            inReader.close();
            inStream.close();
        } catch (Exception e) {
        	 Log.d(TAG,"error:" + e);
        }
    }

	private static boolean waitForProcess(Process p) {  
		boolean isSuccess = false;	
		int returnCode;  
		try {  
			returnCode = p.waitFor(); 
			Log.i(TAG, "waitForProcess returnCode:" + returnCode);
			switch (returnCode) {  
			case 0:  
				isSuccess = true;  
				break;	
				  
			case 1:  
				break;	
				  
			default:  
				break;	
			}  
		} catch (InterruptedException e) {	
			e.printStackTrace();  
			Log.i(TAG, "waitForProcess error!");
		}  
	
		return isSuccess;  
	}	   

	public static synchronized boolean execCmd(String cmd) {  
		boolean isSuccess = false;	
		Process process = null;  
		OutputStream out = null;  
		try {  
			process = Runtime.getRuntime().exec(cmd);  
			out = process.getOutputStream();  
			DataOutputStream dataOutputStream = new DataOutputStream(out);	
	
			dataOutputStream.flush(); 
			dataOutputStream.close();
			out.close();  
			  
			isSuccess = waitForProcess(process);  
		} catch (IOException e) {  
			e.printStackTrace();  
			Log.i(TAG,"execCmd error!");
		}	
	
		return isSuccess;  
	}  

    public static boolean writeToFile(String path, String value) {
        if (value == null || value.length() == 0) {
            return false;
        }

        if (path == null || path.length() == 0) {
            return false;
        }

        Log.d(TAG, path + "*****"+value);

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


