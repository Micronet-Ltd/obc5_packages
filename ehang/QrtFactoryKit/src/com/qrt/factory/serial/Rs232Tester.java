package com.qrt.factory.serial;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;


public class Rs232Tester {
    private final String TAG = Rs232Tester.class.getSimpleName();
    private static final int NON_BLOCKING=4000;

    String mPath;
    int mBaudrate;
    int mMaxMillis;

    protected SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;

    byte[] mReceived;
    int mUpToNowReceived;
    int mMaxToReceive;

    public Rs232Tester(String path, int baudrate, int maxMillis) {
        mPath = path;
        mBaudrate = baudrate;
        mMaxMillis = maxMillis;
        openSerialPort(mPath, mBaudrate, mMaxMillis);
    }

    private void openSerialPort(String path, int baudrate, int maxMillis) {
        try {
            mSerialPort = new SerialPort(new File(path), baudrate, NON_BLOCKING);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mMaxMillis = maxMillis;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSerialPort() {
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
    }

    public int readMaxSize(byte[] inBuffer, int maxSize) {

        FutureTask<String> ft = new FutureTask<String>(new ReadTask());
        ExecutorService ex = Executors.newSingleThreadExecutor();
        //Log.d(TAG, "readMaxSize future task created !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        mReceived = inBuffer;
        mMaxToReceive = maxSize;
        mUpToNowReceived = 0;
        //Log.d(TAG, "readMaxSize future task run !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        ex.execute(ft);


        //Log.d(TAG, "readMaxSize future task running !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            //Log.d(TAG, "readMaxSize future task try !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            ft.get(mMaxMillis, TimeUnit.MILLISECONDS);
            //Log.d(TAG, "readMaxSize future task tried !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } catch (Exception e) {
            //e.printStackTrace();
            ft.cancel(true);
            ex.shutdownNow();
        }
        //Log.d(TAG, "readMaxSize future task ended !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return mUpToNowReceived;
    }


    private boolean onDataReceived(final byte[] buffer, int size) {
        boolean isExit = false;
        if (size + mUpToNowReceived >= mMaxToReceive) {
            size = mMaxToReceive - mUpToNowReceived;
            isExit = true;
            //Log.d(TAG, "onDataReceived will interupt read thread size: " + size + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        //Log.d(TAG, "onDataReceived add " + size + " to mReceived !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        for (int i = 0; i < size; i++) {
            mReceived[mUpToNowReceived + i] = buffer[i];
            String received = "VOID";
            try {
                received = new String(mReceived, 0, mUpToNowReceived + i, "UTF-8");
            } catch (Exception e) {

            }
        }

        mUpToNowReceived += size;
        //Log.d(TAG, "onDataReceived mUpToNowReceived: " + mUpToNowReceived +
        //        " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return isExit;
    }

    public void writeToSerial(byte[] inBytes, int inSize) {
        FutureTask<String> ft = new FutureTask<String>(new WriteTask(inBytes, inSize));
        //Log.d(TAG, "writeToSerial future task created !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        ft.run();
        try {
            ft.get(mMaxMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d(TAG, "writeToSerial before return !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return;
    }

    private class ReadTask implements Callable<String> {
        @Override
        public String call() {
            Boolean isExit = false;
            while (!isExit) {
                int size;
                try {
                    byte[] buffer = new byte[16];
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        isExit = onDataReceived(buffer, size);
                        if (isExit) break;
                    }
                } catch (IOException e) {
                    isExit=true;
                }
            }

            return "Done";
        }
    }

    private class WriteTask implements Callable<String> {
        byte[] inBytes;
        int inSize;
        String textToWrite = "";

        WriteTask(byte[] inBytes, int inSize) {
            this.inBytes = inBytes;
            this.inSize = inSize;
            try {
                textToWrite = new String(inBytes, 0, inSize, "UTF-8");
            } catch (Exception e) {

            }
        }

        @Override
        public String call() {
            //Log.d(TAG, "in call() of WriteTask, textToWrite: <" + textToWrite + "> !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            try {
                mOutputStream.write(inBytes, 0, inSize);
                mOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Done";
        }
    }
}
