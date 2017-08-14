package com.micronet.alwaysonwifihotspot;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by eemaan.siddiqi on 8/11/2017.
 */

public class ReadWriteFile {

    public static File Dir;
    private static String TAG = "AOWHS - Service";

    //Write function
    public static void writeToFile(String handlerValue, Context context){

        File file = new File(context.getFilesDir(), "HotspotEnabledCount.txt"); //Created a Text File
        if(!file.exists()) {
            handlerValue = "0";
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(handlerValue.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    //Read Function
    public static String readFromFile(Context context) {

        String ret = "";
        File file = new File(context.getFilesDir(), "HotspotEnableCount.txt"); //Created a Text File
        if(!file.exists()){
            return ret;
        }
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            fileReader.close();
            ret = stringBuilder.toString();

        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        }
        catch (Exception e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }

    //Logging the Service activity
    public static void LogToFile(Context context, String handlerValue1, String additionalMessage){
        BufferedWriter bufferedWriter = null;
        FileWriter fileWriter = null;
        String timestamp=("Timestamp: ")+Utils.formatDate(System.currentTimeMillis())+("   ");//Getting current time stamp
        String infoMessage="Message Info:   ";
        String ec= "    EnabledCount:   ";
        File file = new File(Dir, "AOWFS-ServiceLog.txt");//Created a Text File to maintain the service activity log
        if(!file.exists()) {
            Log.d(TAG, "ServiceLog.txt: File Doesn't exist");
        }
        try {
            fileWriter = new FileWriter(file.getAbsoluteFile(), true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(timestamp);
            bufferedWriter.write(infoMessage);
            bufferedWriter.write(additionalMessage);
            bufferedWriter.write(ec);
            bufferedWriter.write(handlerValue1);
            bufferedWriter.newLine();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            e.printStackTrace();
        }
        finally {
            try {
                if (bufferedWriter!=null)
                    bufferedWriter.close();
                if (fileWriter!=null)
                    fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
