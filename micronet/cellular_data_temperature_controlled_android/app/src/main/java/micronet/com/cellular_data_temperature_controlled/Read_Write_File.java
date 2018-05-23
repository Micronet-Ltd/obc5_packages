package micronet.com.cellular_data_temperature_controlled;

import android.content.ContentResolver;
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
    public static File micronetServiceDir;
    public static File serviceDir;
    public static BufferedWriter bufferedWriter = null;
    public static FileWriter fileWriter = null;
    private static String configDisabled = "Override_Cellular_Data_Configuration = false";
    private static String configEnabled = "Override_Cellular_Data_Configuration = true";


    public static void writeManagedStateToFile(String handlerValue, Context context){
        File file = new File(context.getFilesDir(), "Managed-MobileDataState.txt"); //Created a Text File for storing the enabled count
        if(!file.exists()) {
            //If Managed-MobileDataState.txt is not found, reset the state to 0
            //If the state is 0 - Service hasn't disabled cellular data else service has disabled it.
            handlerValue = "0";
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(handlerValue.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "Managed-MobileDataState.txt: File write failed: " + e.toString());
        }
    }

    //Read Function
    public static String readManagedStateFromFile(Context context) {

        String ret = "";
        //Creating a Text File to store the state of cellular data if the service managed it at any point.
        File file = new File(context.getFilesDir(), "Managed-MobileDataState.txt");
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
            Log.e(TAG, "Managed-MobileDataState.txt: File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "Managed-MobileDataState.txt: Can not read file: " + e.toString());
        }
        return ret;
    }

    public static void writeConfigurationToFile(Boolean override, Context context){
        // Text file that stores the settings configuration parameters
        String handlerValue = override ? configEnabled : configDisabled;
        File file = new File(serviceDir, "CellularDataServiceConfig.txt");
        if(!file.exists()) {
            handlerValue = configDisabled;
            Log.d(TAG, "CellularDataServiceConfig.txt not found, write the default setting: " + configDisabled);
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(handlerValue.getBytes());
            fileOutputStream.close();
        }
        catch (IOException e) {
            Log.e("Exception", "CellularDataServiceConfig.txt: File write failed: " + e.toString());
        }
    }

    //Read Function
    public static String readConfigurationFromFile(Context context) {
        //Creating a Text File to store the settings configuration
        String rawData = "";
        String ret = "";
        File file = new File(serviceDir, "CellularDataServiceConfig.txt");
        if(!file.exists()) {
            Log.e(TAG, "Read Failed: CellularDataServiceConfig.txt doesn't exist!");
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
            rawData = stringBuilder.toString();
           // Log.d(TAG, "Configuration Returned = " + rawData);

            if(rawData.equalsIgnoreCase(configDisabled)){
                ret = "false";
            }
            else if (rawData.equalsIgnoreCase(configEnabled)){
                ret = "true";
            }
            else{
                ret = "Invalid Value";
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "CellularDataServiceConfig.txt: File not found: " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "CellularDataServiceConfig.txt: Can not read file: " + e.toString());
        }
        Log.d(TAG, "Read Config: " + ret);
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
    public static void LogToFile(String disabledCount, String enabledCount , Context context, String additionalMessage, ContentResolver contentResolver) throws IOException {
        Boolean isNewFile = false;
        String serviceActivityHeader = "Time Stamp, Message Information, Temperature Zones, Override Cell State Configuration, isMobileDataEnabled, isAirplaneModeEnabled, Enabled Count, Disabled Count,";
        String timestamp=Utils.formatDate(System.currentTimeMillis()); //Getting current time stamp
        TemperatureValues.getThermalZoneTemp();
        String coreTemps = TemperatureValues.temperaturevalues;
        String delim = ",";

        File file = new File(serviceDir, "CellularServiceLog.csv");//Created a Text File to maintain the service activity log
        if(!file.exists()) {
            isNewFile = true;
            Log.d(TAG, "CellularServiceLog.csv: File Doesn't exist");
            file.createNewFile();
        }
        else{
            isNewFile = false;
        }
        try {
            fileWriter = new FileWriter(file.getAbsoluteFile(), true);
            bufferedWriter = new BufferedWriter(fileWriter);
            if(isNewFile){
                bufferedWriter.write(serviceActivityHeader);
                bufferedWriter.newLine();
            }
            bufferedWriter.write(timestamp + delim);
            bufferedWriter.write(additionalMessage + delim);
            bufferedWriter.write(coreTemps + delim);
            bufferedWriter.write(String.valueOf(ServiceConfiguration.getCellularDataConfig(context)) + delim);
            bufferedWriter.write(String.valueOf(MobileDataManager.getMobileDataState(context)) + delim);
            bufferedWriter.write(String.valueOf(MobileDataManager.isAirplaneMode(contentResolver)) + delim);
            bufferedWriter.write(enabledCount + delim);
            bufferedWriter.write(disabledCount + delim);
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
        File file = new File(serviceDir, "ServiceActivityLog.txt");//Created a Text File to maintain the service activity log
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