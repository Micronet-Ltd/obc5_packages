package com.micronet.mcontrol;

import android.os.Build;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by brigham.diaz on 6/7/2016.
 */
public class Logger {
    private static final long K = 1024L;
    private static final long M = 1024L * K;
    private static final String TAG = "MCTL - Logger";

    private static final String LOG_DIR = "/sdcard/mcontrol/";
    private static String logpath = null;

    private static StringBuilder logLines = new StringBuilder();

    /**
     * Read the first line to get the current file path
     */
    private static String getLogPath() {
        return logpath;
    }

    public static void log(String line) {
        logLines.append(formatDate(System.currentTimeMillis()) + " " + line + "\n");
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
        String serial = Build.SERIAL;
        logpath = LOG_DIR + formatDateShort(System.currentTimeMillis()) + "_" + serial +  "_mctl.log";
    }

    private static File createNewLogFile() {
        setLogFilePath();
        File logFile = new File(logpath);
        try {
            // create the new log directory
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdir();
            }

            // create the new log file
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

        } catch (IOException ex) {
            Log.d(TAG, ex.getMessage());
        } finally {
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
                out.println(logLines.substring(0, length));
                logLines.delete(0, length);
            } catch (FileNotFoundException ex) {
                return false;
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
        return true;
    }
}