package com.micronet.canbus;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class FlexCANCanbusSocket extends CanbusSocket implements CanbusListener{
    private static final String TAG = "CanbusSocket";

    private int mSocket;
    //TODO: how many elements ??
    // Needs enough to prevent blocking insertion.
    BlockingQueue<CanbusFramePort1> mQueuej1939Port1 = new LinkedBlockingQueue<CanbusFramePort1>(2000);
    BlockingQueue<CanbusFramePort2> mQueuej1939Port2 = new LinkedBlockingQueue<CanbusFramePort2>(2000);
    BlockingQueue<J1708Frame> mQueueJ1708 = new LinkedBlockingQueue<J1708Frame>(2000);

    /**
     * Creates Canbus socket.
     */
    protected FlexCANCanbusSocket(int fd) {
        mSocket = fd;
    }

    /**
     * Reads Canbus frame. Will block the calling thread until data
     * is written to Canbus socket.
     */
    public CanbusFramePort1 readPort1(){

        CanbusFramePort1 frame = null;
        try {
            frame = mQueuej1939Port1.take();
        } catch (InterruptedException e) {
            Log.e(TAG, "readPort1 queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    /**
     * Reads Canbus frame. Will block the calling thread until data
     * is written to Canbus socket or timeout has elapsed.
     *
     * @param timeout how long to wait before giving up, in units of milliseconds
     *
     * @return the head Canbus frame, or null if th specified waiting time elapses before Canbus frame is available
     */
    public CanbusFramePort1 readPort1(long timeout) {

        CanbusFramePort1 frame = null;

        try {
            frame = mQueuej1939Port1.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "readPort1 queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    /**
     * Reads Canbus frame. Will block the calling thread until data
     * is written to Canbus socket.
     */
    public CanbusFramePort2 readPort2(){

        CanbusFramePort2 frame = null;
        try {
            frame = mQueuej1939Port2.take();
        } catch (InterruptedException e) {
            Log.e(TAG, "readPort2 queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    /**
     * Reads Canbus frame. Will block the calling thread until data
     * is written to Canbus socket or timeout has elapsed.
     *
     * @param timeout how long to wait before giving up, in units of milliseconds
     *
     * @return the head Canbus frame, or null if th specified waiting time elapses before Canbus frame is available
     */
    public CanbusFramePort2 readPort2(long timeout) {

        CanbusFramePort2 frame = null;

        try {
            frame = mQueuej1939Port2.poll(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "readPort2 queue error !!");
            e.printStackTrace();
        }

        return frame;
    }

    @Override
    public void onPacketReceive1939Port1(CanbusFramePort1 frame) {
        if(!mQueuej1939Port1.offer(frame))
            Log.e(TAG, "Unable to put frame, dropping.");

    }

    @Override
    public void onPacketReceive1939Port2(CanbusFramePort2 frame) {
        if(!mQueuej1939Port2.offer(frame))
            Log.e(TAG, "Unable to put frame, dropping.");
    }

    @Override
    public void onPacketReceiveJ1708(J1708Frame frame) {
        if(!mQueueJ1708.offer(frame))
            Log.e(TAG, "Unable to put frame, dropping.");
    }

    /**
     * Sends Canbus frame to port 1 through socket.
     */
    public void write1939Port1(CanbusFramePort1 frame){
        sendJ1939Port1(mSocket, frame);
    }

    /**
     * Sends Canbus frame to port 2 through socket.
     */
    public void write1939Port2(CanbusFramePort2 frame){
        sendJ1939Port2(mSocket, frame);
    }

    /**
     * Sends J1708 frame through socket.
     */
    public void writeJ1708(J1708Frame frame){
        sendJ1708(mSocket, frame);
    }

    /**
     * Opens Canbus socket for readPort1/write1939Port1 operations.
     */
    public void open() {
        setPacketListener(this);
    }

    /**
     * Closes Canbus socket for port 1
     */
    public void close1939Port1(){
        closeSocketJ1939Port1();
    }

    /**
     * Closes Canbus socket for port 1
     */
    public void close1939Port2(){
        closeSocketJ1939Port2();
    }

    /**
     * Closes Canbus socket for port 1
     */
    public void close1708(){closeSocketJ1939Port2();}


    /**
     * Returns Canbus socket id.
     */
    public int getId(){
        return mSocket;
    }

    private void setPacketListener(CanbusListener listener) {
        registerCallback(listener);
    }

    private native int sendJ1939Port1(int socket, CanbusFramePort1 frame);
    private native int sendJ1939Port2(int socket, CanbusFramePort2 frame);
    private native int sendJ1708(int socket, J1708Frame frame);
    private native int registerCallback(CanbusListener listener);
    private native int closeSocketJ1939Port1();
    private native int closeSocketJ1939Port2();
    private native int closeSocketJ1708();




    static
    {
        System.loadLibrary("canbus");
    }

}
