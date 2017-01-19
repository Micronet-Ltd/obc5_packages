package com.micronet.mcontrol;

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
     * Get the current file path
     */
    private static String getLogPath() {
        return logpath;
    }

    /**
     * Log a new line with timestamp to the buffer.
     * Call createNewLogFile and saveLog to write buffer to file.
     * @param line line that will get logged
     */
    public static void log(String line) {
        log(line, true);
    }

    /**
     * Log a new line to the buffer.
     * Call createNewLogFile and saveLog to write buffer to file.
     * @param line line that will get logged
     * @param timestamp true to prepend timestamp and comma, false for no timestamp
     */
    public static void log(String line, boolean timestamp) {
        if(timestamp) {
            logLines.append(Utils.formatDate(System.currentTimeMillis()) + "," + line + CRLF);
        } else {
            logLines.append(line + CRLF);
        }
    }

    /**
     * Build the full file path (directory + file name + ext)
     *
     * @param filename The file name + ext. Example: String.format("%s_%s_mctl.csv", formatDateShort(System.currentTimeMillis(), Build.SERIAL)
     */
    public static void setLogFilePath(String filename) {
        LOG_DIR = ExternalStorage.getSdCardPath() + "mcontrol/";
        logpath = LOG_DIR + filename;
    }

    /**
     * Create a new log file in the log directory /sdcard/mcontrol/
     *
     * @param filename The file name + ext. Example: String.format("%s_%s_mctl.csv", formatDateShort(System.currentTimeMillis(), Build.SERIAL)
     * @param overwrite Overwrite the existing log file, erasing any existing file content. False to append to any existing content.
     */
    public static File createNewLogFile(String filename, boolean overwrite) {
        setLogFilePath(filename);
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

            if(logFile.exists() && overwrite) {
                logFile.delete();
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
     * Writes the log buffer to file and clears the log buffer.
     * Must call createNewLogFile first in order to create file.
     */
    public static synchronized boolean saveLog() {
        if (logLines == null || logLines.length() == 0) {
            return false;
        }


        if(logpath == null) {
            Log.d(TAG, "Log directory must be created before it can be saved!");
            return false;
        }

        // open the existing log file
        File logFile = new File(logpath);
        if(!logFile.exists()) {
            Log.d(TAG, String.format("Failed to create %s", logpath));
            return false;
        }

        // writes data to file
        while(logLines.length() > 0) {
            PrintStream out = null;
            try {
                // FileOutputStream, append = true
                out = new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile, true)));
                // copy strings from builder and them delete them
                out.print(logLines.toString());
                logLines.setLength(0);
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

        // sanity check
        File logFile = new File(logpath);
        if(logFile == null) { return false; }

        return logFile.delete();
    }
}