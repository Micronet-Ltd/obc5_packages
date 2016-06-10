package com.micronet.mcontrol;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by brigham.diaz on 6/7/2016.
 */
public class Logger {
    private static final long K = 1024L;
    private static final long M = 1024L * K;
    private static final String TAG = "MCTL - Logger";

    private static String LOG_DIR = "/sdcard/mcontrol/";
    private static String logpath = null;
    private static final String CRLF = "\r\n";

    private static StringBuilder logLines = new StringBuilder();

    /**
     * Read the first line to get the current file path
     */
    private static String getLogPath() {
        return logpath;
    }

    /**
     * Log a newline with timestamp
     * @param line line that will get logged
     */
    public static void log(String line) {
        log(line, true);
    }

    /**
     * Log a new line
     * @param line line that will get logged
     * @param timestamp true to prepend timestamp and comma, false for no timestamp
     */
    public static void log(String line, boolean timestamp) {
        if(timestamp) {
            logLines.append(formatDate(System.currentTimeMillis()) + "," + line + CRLF);
        } else {
            logLines.append(line + CRLF);
        }
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String formatDateShort(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static String formatDateShort(long time) {
        return formatDateShort(new Date(time));
    }

    public static String formatDate(long time) {
        return formatDate(new Date(time));
    }

    private static void setLogFilePath() {
        LOG_DIR = ExternalStorage.getSdCardPath() + "mcontrol/";
        logpath = LOG_DIR + formatDateShort(System.currentTimeMillis()) + "_" +  Build.SERIAL +  "_mctl.csv";
    }

    private static File createNewLogFile() {
        setLogFilePath();
        File logFile = new File(logpath);
        try {
            // create the new log directory
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                if(logDir.mkdirs()) {
                    Log.d(TAG, "Created log folder");
                } else {
                    Log.d(TAG, "Created log folder failed");
                }
            }

            // create the new log file
            if (!logFile.exists()) {

                if(logFile.createNewFile()) {
                    Log.d(TAG, "Created log file");
                } else {
                    Log.d(TAG, "Created log file failed");
                }
            }

        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        }

        return logFile;
    }

    public static String getLogFilePath() {
        return logpath;
    }

    /**
     *
     */
    public static synchronized boolean saveLog() {
        if (logLines == null || logLines.length() == 0) {
            return false;
        }

        // make sure file exists
        File logFile;
        if(logpath == null) {
            logFile = createNewLogFile();
        } else {
            logFile = new File(logpath);
        }

        if(!logFile.exists()) {
            logFile = createNewLogFile();
        }


        // writes data to file
        while(logLines.length() > 0) {
            int length = logLines.length();

            PrintStream out = null;
            try {
                out = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true), (int)(5*M)));
                // copy strings from builder and them delete them
                out.print(logLines.substring(0, length));
                logLines.delete(0, length);
            } catch (FileNotFoundException ex) {
                Log.d(TAG, ex.getMessage());
                return false;
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        return true;
    }

    public static synchronized boolean deleteLog() {
       // make sure file exists
        if(logpath == null) {
            return false;
        }

        File logFile = new File(logpath);
        return logFile.delete();
    }
}