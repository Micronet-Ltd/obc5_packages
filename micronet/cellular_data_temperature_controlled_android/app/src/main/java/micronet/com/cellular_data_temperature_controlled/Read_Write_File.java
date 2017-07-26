package micronet.com.cellular_data_temperature_controlled;

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
 * Created by eemaan.siddiqi on 12/20/2016.
 */

public class Read_Write_File {

    public static final String TAG = "Read_Write_File";
    //Declaring the Directory
    public static File logDir;
    public static BufferedWriter bufferedWriter = null;
    public static FileWriter fileWriter = null;


    public static void writeStateToFile(String handlerValue, Context context){
        File file = new File(context.getFilesDir(), "MobileDataState.txt"); //Created a Text File for storing the enabled count
        if(!file.exists()) {
            //If MobileDataState.txt is not found, reset the state to 0
            //If the state is 0 - Service hasn't disabled cellular data else service has disabled it.
            handlerValue = "0";
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(handlerValue.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "MobileDataState.txt: File write failed: " + e.toString());
        }
    }

    //Read Function
    public static String readStateFromFile(Context context) {

        String ret = "";
        //Creating a Text File to store the state of cellular data if the service managed it at any point.
        File file = new File(context.getFilesDir(), "MobileDataState.txt");
        if(!file.exists()) {
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
        } catch (FileNotFoundException e) {
            Log.e(TAG, "MobileDataState.txt: File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "MobileDataState.txt: Can not read file: " + e.toString());
        }
        return ret;
    }

    //Write function
    public static void writeToFile(String handlerValue, Context context){
        //Created a Text File for storing the enabled count
        File file = new File(context.getFilesDir(), "MobileDataEnabled.txt");
        if(!file.exists()) {
            //If MobileDataEnabled.txt is not found, reset the count to 0
            handlerValue = "0";
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(handlerValue.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "MobileDataEnabled.txt: File write failed: " + e.toString());
        }
    }

    //Read Function
    public static String readFromFile(Context context) {

        String ret = "";
        File file = new File(context.getFilesDir(), "MobileDataEnabled.txt");
        if(!file.exists()) { return ret;}
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
        } catch (FileNotFoundException e) {
            Log.e(TAG, "MobileDataEnabled.txt: File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "MobileDataEnabled.txt: Can not read file: " + e.toString());
        }
        return ret;
    }
    //Write function for disabled count
    public static void writeDisabledCountToFile(String DisabledValue, Context context){
        //Created a Text File for storing the disabled count
        File fileDis = new File(context.getFilesDir(), "MobileDataDisabled.txt");
        if (!fileDis.exists()){
            //If MobileDataDisabled.txt is not found, reset the count to 0
            DisabledValue="0";
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileDis);
            fileOutputStream.write(DisabledValue.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "MobileDataDisabled.txt: File write failed: " + e.toString());
        }
    }
    //Read Function for disabled Count
    public static String readDisabledCountFromFile(Context context) {

        String ret = "";
        File fileDis=new File(context.getFilesDir(), "MobileDataDisabled.txt");

        if(!fileDis.exists()){
            //If the disabled file does not exist return an empty string
            return ret;
        }
        try {
            FileReader fileReader = new FileReader(fileDis);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String receiveString = "";
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString);
            }

            fileReader.close();
            ret = stringBuilder.toString();

        } catch (FileNotFoundException e) {
            Log.e(TAG, "MobileDataDisabled.txt: File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "MobileDataDisabled.txt: Can not read file: " + e.toString());
        }
        return ret;
    }
    //Logging the Service activity
    public static void LogToFile(String handlerValue2, String handlerValue1 , Context context, String additionalMessage){
        String timestamp=("Timestamp: ")+Utils.formatDate(System.currentTimeMillis())+("   "); //Getting current time stamp
        String infoMessage="Message Info:   ";
        String ec= "    EnabledCount:   ";
        String dc= "    DisabledCount:   ";
        File file = new File(logDir, "ServiceLog.txt");//Created a Text File to maintain the service activity log
        if(!file.exists()) {
            Log.d(TAG, "ServiceLog.txt: File Doesn't exist");
        }
        try {
            fileWriter = new FileWriter(file.getAbsoluteFile(), true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(timestamp);
            bufferedWriter.write(infoMessage);
            bufferedWriter.write(additionalMessage);
            bufferedWriter.write(TemperatureValues.temperaturevalues);
            bufferedWriter.write(ec);
            bufferedWriter.write(handlerValue1);
            bufferedWriter.write(dc);
            bufferedWriter.write(handlerValue2);
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
    public static void serviceActivityLog(Boolean pauseStatus, Context context){
        String String_pauseStatus=String.valueOf(pauseStatus);
        String timestamp=("Timestamp:   ")+Utils.formatDate(System.currentTimeMillis())+("   "); //Getting current time stamp
        String paused="     Paused Status:  ";
        File file = new File(logDir, "ServiceActivityLog.txt");//Created a Text File to maintain the service activity log
        if(!file.exists()) {
            Log.d(TAG, "ServiceActivityLog.txt: File Doesn't exist");
        }
        try {
            fileWriter = new FileWriter(file.getAbsoluteFile(), true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(timestamp);
            bufferedWriter.write(TemperatureValues.temperaturevalues);
            bufferedWriter.write(paused);
            bufferedWriter.write(String_pauseStatus);
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