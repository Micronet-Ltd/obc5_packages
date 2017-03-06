package micronet.com.cellular_data_temperature_controlled;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import static android.content.ContentValues.TAG;

/**
 * Created by eemaan.siddiqi on 12/20/2016.
 */

public class Read_Write_File {
    //Declaring the Directory
    public static File Dir;
    public static BufferedWriter bufferedWriter = null;
    public static FileWriter fileWriter = null;

    //Write function


    public static void writeToFile(String handlerValue, Context context){
        File file = new File(Dir, "MobileDataEnabled.txt"); //Created a Text File for storing the enabled count
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
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    //Read Function
    public static String readFromFile(Context context) {

        String ret = "";
        File file = new File(Dir, "MobileDataEnabled.txt"); //Created a Text File for enabled count
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
            Log.e(TAG, "File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }
    //Write function for disabled count
    public static void writeDisabledCountToFile(String DisabledValue, Context context){

        File fileDis = new File(Dir, "MobileDataDisabled.txt"); //Created a Text File for storing the disabled count
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
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    //Read Function for disabled Count
    public static String readDisabledCountFromFile(Context context) {

        String ret = "";
        File fileDis=new File(Dir, "MobileDataDisabled.txt"); // Created a Text for disabled Count
        if(!fileDis.exists()){return ret;} //If the disabled file does not exist return an empty string
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
            Log.e(TAG, "File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }
    //Logging the Service activity
    public static void LogToFile(String handlerValue2, String handlerValue1 , Context context){
        String timestamp=("Timestamp: ")+Utils.formatDate(System.currentTimeMillis())+("   "); //Getting current time stamp
        String ec= "    EnabledCount:   ";
        String dc= "    DisabledCount:   ";
        File file = new File(Dir, "ServiceLog.txt");//Created a Text File to maintain the service activity log
        if(!file.exists()) {
            Log.d(TAG, "File Doesn't exist");
        }
        try {
            fileWriter = new FileWriter(file.getAbsoluteFile(), true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(timestamp);
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
        File file = new File(Dir, "ServiceActivityLog.txt");//Created a Text File to maintain the service activity log
        if(!file.exists()) {
            Log.d(TAG, "File Doesn't exist");
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