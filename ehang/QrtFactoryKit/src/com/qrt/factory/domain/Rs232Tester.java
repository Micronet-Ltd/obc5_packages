package com.micronet.serialporttest;

/**
 * Created by daphna.tzur on 28/03/2017.
 */

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by daphna.tzur on 26/03/2017.
 */

public class Rs232Tester {
    private final String TAG = "Rs232Tester";
    private static final String PATH = "/dev/ttyHSL1";
    private static final int BAUDRATE = 9600;
    private static final int MAX_BUFFER = 1024;
    private static final int MAX_MILLIS = 2000;

    String mPath;
    int mBaudrate;
    int mMaxMillis;

    protected SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    byte[] mReceived;
    int mUpToNowReceived;
    int mMaxToReceive;

    Rs232Tester() {
        mPath = PATH;
        mBaudrate = BAUDRATE;
        mMaxMillis = MAX_MILLIS;
        openSerialPort(mPath, mBaudrate, mMaxMillis);
    }

    Rs232Tester(String path, int baudrate, int maxMillis) {
        mPath = path;
        mBaudrate = baudrate;
        mMaxMillis = maxMillis;
        openSerialPort(mPath, mBaudrate, mMaxMillis);
    }

    private void openSerialPort(String path, int baudrate, int maxMillis) {
        try {
            mSerialPort = new SerialPort(new File(path), baudrate, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mMaxMillis = maxMillis;
            Log.d(TAG, "readMaxSize constructor, input/output streams opened !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    public void openSerialPort() throws IOException {
        openSerialPort(mPath, mBaudrate, mMaxMillis);
    }




    public void inverse(byte[] inBuffer, byte[] outBuffer, int inSize) {
        String received = "VOID";
        try {
            received = new String(inBuffer, 0, inSize, "UTF-8");
        } catch (Exception e) {

        }
        Log.d(TAG, "inverse received: " + received + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        //byte[] outBuffer = new byte[inSize];
        for (int i = 0; i < inSize; i++) {
            outBuffer[inSize - 1 - i] = inBuffer[i];
        }
        String inversed = "VOID";
        try {
            inversed = new String(outBuffer, 0, inSize, "UTF-8");
        } catch (Exception e) {

        }
        Log.d(TAG, "inverse inverted: " + inversed + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return;
    }

    protected int readMaxSize(byte[] inBuffer, int maxSize) {

        FutureTask<String> ft = new FutureTask<String>(new ReadTask());
        Log.d(TAG, "readMaxSize future task created !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        mReceived = inBuffer;
        mMaxToReceive = maxSize;
        mUpToNowReceived = 0;
        ft.run();
        try {
            ft.get(mMaxMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "readMaxSize future task ended !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return mUpToNowReceived;
    }


    private boolean onDataReceived(final byte[] buffer, int size) {
        boolean isExit = false;
        if (size + mUpToNowReceived >= mMaxToReceive) {
            size = mMaxToReceive - mUpToNowReceived;
            isExit = true;
            Log.d(TAG, "onDataReceived will interupt read thread size: " + size + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        Log.d(TAG, "onDataReceived add " + size + " to mReceived !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        for (int i = 0; i < size; i++) {
            mReceived[mUpToNowReceived + i] = buffer[i];
            String received = "VOID";
            try {
                received = new String(mReceived, 0, mUpToNowReceived + i, "UTF-8");
            } catch (Exception e) {

            }
            Log.d(TAG, "onDataReceived accumulated: <" + received +
                    "> !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }

        mUpToNowReceived += size;
        Log.d(TAG, "onDataReceived mUpToNowReceived: " + mUpToNowReceived +
                " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return isExit;
    }

    protected void writeToSerial(byte[] inBytes, int inSize) {
        FutureTask<String> ft = new FutureTask<String>(new WriteTask(inBytes, inSize));
        Log.d(TAG, "writeToSerial future task created !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        ft.run();
        try {
            ft.get(mMaxMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "writeToSerial before return !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return;
    }

    private class ReadTask implements Callable<String> {
        @Override
        public String call() {
            Boolean isExit = false;
            while (!isExit) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        isExit = onDataReceived(buffer, size);
                        if (isExit) break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return "blabla";
        }
    }

    private class WriteTask implements Callable<String> {
        byte[] inBytes;
        int inSize;
        String textToWrite = "";

        WriteTask(byte[] inBytes, int inSize) {
            this.inBytes = inBytes;
            this.inSize = inSize;String inversed = "VOID";
            try {
                textToWrite = new String(inBytes, 0, inSize, "UTF-8");
            } catch (Exception e) {

            }
        }

        @Override
        public String call() {
            Log.d(TAG, "in call() of WriteTask, textToWrite: <" + textToWrite + "> !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            try {
                mOutputStream.write(inBytes, 0, inSize);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "blabla";
        }
    }
}